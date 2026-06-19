package com.teco.bolao.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.teco.bolao.dto.SeedParticipantDto;
import com.teco.bolao.dto.SeedPredictionDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfPredictionSeedGenerator {

    private static final int EXPECTED_MATCHES = 72;
    private static final String BLOCKED_REASON = "BLOCKED_OR_LATE_ENTRY";
    private static final Pattern PREDICTION_PATTERN = Pattern.compile(
            "^(?:\\S\\s+)?(.+?)\\s+(\\d+)\\s+(\\d+)\\s+(.+?)(?:\\s+(\\d+))?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern BLOCKED_PATTERN = Pattern.compile(
            "^(?:\\S\\s+)?(.+?)\\s+xxx\\s+xxx\\s+(.+?)(?:\\s+xxx)?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Map<String, String> EXTRA_TEAM_ALIASES = Map.ofEntries(
            Map.entry("QATAR", "QAT"),
            Map.entry("REP TCHECA", "CZE"),
            Map.entry("BOSNIA", "BIH"),
            Map.entry("HOLANDA", "NED"),
            Map.entry("ARABIA SAUDITA", "KSA"),
            Map.entry("IRA", "IRN"),
            Map.entry("REP CONGO", "COD")
    );

    private final ObjectMapper objectMapper;

    public PdfPredictionSeedGenerator() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }

    public static void main(String[] args) throws Exception {
        Path projectRoot = resolveProjectRoot(Path.of("").toAbsolutePath());
        Path catalogPath = args.length > 0 ? Path.of(args[0]) : projectRoot.resolve("backend/src/main/resources/import/group-stage-seed.json");
        Path pdfDirectory = args.length > 1 ? Path.of(args[1]) : projectRoot.resolve("data/pdfs");
        Path outputDirectory = args.length > 2 ? Path.of(args[2]) : projectRoot.resolve("data/output");

        GenerationResult result = new PdfPredictionSeedGenerator().generate(catalogPath, pdfDirectory, outputDirectory);

        System.out.printf(
                Locale.ROOT,
                "participants=%d predictions=%d blockedMatches=%d pdfsWithErrors=%d%n",
                result.seedImport().participants().size(),
                result.seedImport().predictions().size(),
                result.blockedMatchesCount(),
                result.report().pdfsFailed()
        );
    }

    public GenerationResult generate(Path catalogPath, Path pdfDirectory, Path outputDirectory) throws IOException {
        Catalog catalog = loadCatalog(catalogPath);
        Files.createDirectories(outputDirectory);

        List<Path> pdfFiles;
        try (Stream<Path> pdfStream = Files.list(pdfDirectory)) {
            pdfFiles = pdfStream
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        List<FileReport> fileReports = new ArrayList<>();
        Map<String, SeedParticipantDto> participants = new LinkedHashMap<>();
        List<SeedPredictionDto> predictions = new ArrayList<>();

        for (Path pdfFile : pdfFiles) {
            ParsedPdfResult parsedPdfResult = parsePdf(pdfFile, catalog);
            fileReports.add(parsedPdfResult.report());

            if (parsedPdfResult.report().status() == ParsingStatus.SUCCESS
                    || parsedPdfResult.report().status() == ParsingStatus.SUCCESS_WITH_BLOCKED_MATCH) {
                participants.putIfAbsent(parsedPdfResult.report().participantName(), new SeedParticipantDto(parsedPdfResult.report().participantName()));
                predictions.addAll(parsedPdfResult.predictions());
            }
        }

        List<SeedParticipantDto> sortedParticipants = participants.values().stream()
                .sorted(Comparator.comparing(SeedParticipantDto::name))
                .toList();
        List<SeedPredictionDto> sortedPredictions = predictions.stream()
                .sorted(Comparator.comparing(SeedPredictionDto::participantName)
                        .thenComparing(SeedPredictionDto::matchNumber))
                .toList();

        SeedImportOutput seedImportOutput = new SeedImportOutput(sortedParticipants, sortedPredictions);
        ParsingReportOutput parsingReportOutput = buildParsingReport(catalog.matches().size(), pdfFiles.size(), fileReports, sortedParticipants.size(), sortedPredictions.size());

        objectMapper.writeValue(outputDirectory.resolve("seed-import.json").toFile(), seedImportOutput);
        objectMapper.writeValue(outputDirectory.resolve("parsing-report.json").toFile(), parsingReportOutput);

        return new GenerationResult(seedImportOutput, parsingReportOutput);
    }

    static Path resolveProjectRoot(Path start) {
        Path current = start.toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("PROJECT_STATUS.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not resolve project root from " + start);
    }

    private Catalog loadCatalog(Path catalogPath) throws IOException {
        GroupStageSeedCatalog seedCatalog = objectMapper.readValue(catalogPath.toFile(), GroupStageSeedCatalog.class);

        Map<String, String> teamNameToCode = new LinkedHashMap<>();
        for (CatalogTeam team : seedCatalog.teams()) {
            teamNameToCode.put(normalizeTeamKey(team.name()), team.fifaCode());
        }
        teamNameToCode.putAll(EXTRA_TEAM_ALIASES);

        Map<String, CatalogMatch> matchIndex = new LinkedHashMap<>();
        for (CatalogSeedMatch match : seedCatalog.matches()) {
            matchIndex.put(matchKey(match.homeTeamCode(), match.awayTeamCode()), new CatalogMatch(match.matchNumber(), match.homeTeamCode(), match.awayTeamCode()));
        }

        return new Catalog(teamNameToCode, matchIndex, seedCatalog.matches());
    }

    private ParsedPdfResult parsePdf(Path pdfFile, Catalog catalog) throws IOException {
        List<String> lines = extractLines(pdfFile);
        String participantName = extractParticipantName(lines, pdfFile);

        List<SeedPredictionDto> predictions = new ArrayList<>();
        List<BlockedMatchReport> blockedMatches = new ArrayList<>();
        List<ParsingError> errors = new ArrayList<>();
        Set<Integer> seenMatches = new LinkedHashSet<>();

        for (String line : lines) {
            if (shouldIgnoreLine(line)) {
                continue;
            }

            Optional<ParsedPredictionLine> blockedLine = parseBlockedLine(line);
            if (blockedLine.isPresent()) {
                resolveMatch(blockedLine.get(), catalog).ifPresentOrElse(
                        match -> {
                            seenMatches.add(match.matchNumber());
                            blockedMatches.add(new BlockedMatchReport(
                                    match.matchNumber(),
                                    blockedLine.get().confrontation(),
                                    BLOCKED_REASON
                            ));
                        },
                        () -> errors.add(new ParsingError(
                                line,
                                "Blocked match could not be mapped to catalog"
                        ))
                );
                continue;
            }

            Optional<ParsedPredictionLine> predictionLine = parsePredictionLine(line);
            if (predictionLine.isPresent()) {
                resolveMatch(predictionLine.get(), catalog).ifPresentOrElse(
                        match -> {
                            seenMatches.add(match.matchNumber());
                            predictions.add(new SeedPredictionDto(
                                    participantName,
                                    match.matchNumber(),
                                    predictionLine.get().homeScore(),
                                    predictionLine.get().awayScore()
                            ));
                        },
                        () -> errors.add(new ParsingError(
                                line,
                                "Prediction line could not be mapped to catalog"
                        ))
                );
                continue;
            }

            errors.add(new ParsingError(line, "Line could not be parsed"));
        }

        int validPredictions = predictions.size();
        ParsingStatus status = determineStatus(validPredictions, blockedMatches.size(), errors);
        if (status == ParsingStatus.ERROR && errors.isEmpty()) {
            errors.add(new ParsingError(
                    "PDF summary",
                    "Expected 72 matches, found " + validPredictions + " predictions and " + blockedMatches.size() + " blocked matches"
            ));
        }

        FileReport report = new FileReport(
                pdfFile.getFileName().toString(),
                participantName,
                status,
                EXPECTED_MATCHES,
                validPredictions,
                blockedMatches,
                errors
        );

        return new ParsedPdfResult(predictions, report);
    }

    private List<String> extractLines(Path pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document).lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();
        }
    }

    private String extractParticipantName(List<String> lines, Path pdfFile) {
        return lines.stream()
                .filter(line -> line.startsWith("Apostador:"))
                .findFirst()
                .map(line -> line.substring("Apostador:".length()).trim())
                .orElseThrow(() -> new IllegalStateException("Participant name not found in " + pdfFile.getFileName()));
    }

    private boolean shouldIgnoreLine(String line) {
        String normalized = normalizeText(line);
        if (normalized.startsWith("APOSTADOR")) {
            return true;
        }
        if (normalized.startsWith("TOTAL DE PONTOS")) {
            return true;
        }
        String compact = normalized.replace(" ", "");
        return compact.startsWith("SELE") && compact.endsWith("PTS");
    }

    private Optional<ParsedPredictionLine> parsePredictionLine(String line) {
        Matcher matcher = PREDICTION_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new ParsedPredictionLine(
                matcher.group(1).trim(),
                matcher.group(4).trim(),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        ));
    }

    private Optional<ParsedPredictionLine> parseBlockedLine(String line) {
        Matcher matcher = BLOCKED_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new ParsedPredictionLine(
                matcher.group(1).trim(),
                matcher.group(2).trim(),
                null,
                null
        ));
    }

    private Optional<CatalogMatch> resolveMatch(ParsedPredictionLine parsedLine, Catalog catalog) {
        String homeTeamCode = catalog.teamNameToCode().get(normalizeTeamKey(parsedLine.homeTeamName()));
        String awayTeamCode = catalog.teamNameToCode().get(normalizeTeamKey(parsedLine.awayTeamName()));
        if (homeTeamCode == null || awayTeamCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(catalog.matchesByKey().get(matchKey(homeTeamCode, awayTeamCode)));
    }

    private ParsingStatus determineStatus(int validPredictions, int blockedMatches, List<ParsingError> errors) {
        if (!errors.isEmpty()) {
            return ParsingStatus.ERROR;
        }
        if (validPredictions == EXPECTED_MATCHES && blockedMatches == 0) {
            return ParsingStatus.SUCCESS;
        }
        if (validPredictions == EXPECTED_MATCHES - 1 && blockedMatches == 1) {
            return ParsingStatus.SUCCESS_WITH_BLOCKED_MATCH;
        }
        return ParsingStatus.ERROR;
    }

    private ParsingReportOutput buildParsingReport(
            int catalogMatches,
            int pdfsDiscovered,
            List<FileReport> fileReports,
            int participantsIncluded,
            int predictionsIncluded
    ) {
        long pdfsSucceeded = fileReports.stream().filter(report -> report.status() == ParsingStatus.SUCCESS).count();
        long pdfsSucceededWithBlockedMatch = fileReports.stream()
                .filter(report -> report.status() == ParsingStatus.SUCCESS_WITH_BLOCKED_MATCH)
                .count();
        long pdfsFailed = fileReports.stream().filter(report -> report.status() == ParsingStatus.ERROR).count();
        int blockedMatches = fileReports.stream().mapToInt(report -> report.blockedMatches().size()).sum();

        return new ParsingReportOutput(
                OffsetDateTime.now(ZoneOffset.UTC),
                catalogMatches,
                pdfsDiscovered,
                pdfsSucceeded,
                pdfsSucceededWithBlockedMatch,
                pdfsFailed,
                participantsIncluded,
                predictionsIncluded,
                blockedMatches,
                fileReports
        );
    }

    private String matchKey(String homeTeamCode, String awayTeamCode) {
        return homeTeamCode + "::" + awayTeamCode;
    }

    private String normalizeTeamKey(String value) {
        String normalized = normalizeText(value);
        return normalized.replace(".", "");
    }

    private String normalizeText(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        normalized = normalized.toUpperCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^A-Z0-9 ]", " ");
        return normalized.replaceAll("\\s+", " ").trim();
    }
}

record GenerationResult(
        SeedImportOutput seedImport,
        ParsingReportOutput report
) {
    int blockedMatchesCount() {
        return report.blockedMatches();
    }
}

record SeedImportOutput(
        List<SeedParticipantDto> participants,
        List<SeedPredictionDto> predictions
) {
}

record ParsingReportOutput(
        OffsetDateTime generatedAt,
        int catalogMatches,
        int pdfsDiscovered,
        long pdfsSucceeded,
        long pdfsSucceededWithBlockedMatch,
        long pdfsFailed,
        int participantsIncluded,
        int predictionsIncluded,
        int blockedMatches,
        List<FileReport> files
) {
}

record FileReport(
        String fileName,
        String participantName,
        ParsingStatus status,
        int expectedMatches,
        int validPredictions,
        List<BlockedMatchReport> blockedMatches,
        List<ParsingError> errors
) {
}

record BlockedMatchReport(
        int matchNumber,
        String confrontation,
        String reason
) {
}

record ParsingError(
        String line,
        String reason
) {
}

enum ParsingStatus {
    SUCCESS,
    SUCCESS_WITH_BLOCKED_MATCH,
    ERROR
}

record ParsedPredictionLine(
        String homeTeamName,
        String awayTeamName,
        Integer homeScore,
        Integer awayScore
) {
    String confrontation() {
        return homeTeamName + " x " + awayTeamName;
    }
}

record ParsedPdfResult(
        List<SeedPredictionDto> predictions,
        FileReport report
) {
}

record Catalog(
        Map<String, String> teamNameToCode,
        Map<String, CatalogMatch> matchesByKey,
        List<CatalogSeedMatch> matches
) {
}

record CatalogMatch(
        int matchNumber,
        String homeTeamCode,
        String awayTeamCode
) {
}

record GroupStageSeedCatalog(
        List<CatalogTeam> teams,
        List<CatalogSeedMatch> matches
) {
}

record CatalogTeam(
        String name,
        String fifaCode
) {
}

record CatalogSeedMatch(
        Integer matchNumber,
        String homeTeamCode,
        String awayTeamCode
) {
}
