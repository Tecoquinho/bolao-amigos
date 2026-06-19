package com.teco.bolao.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PdfPredictionSeedGeneratorTest {

    @Test
    void shouldGenerateSeedAndReportForCurrentPdfCorpus() throws Exception {
        Path projectRoot = PdfPredictionSeedGenerator.resolveProjectRoot(Path.of("").toAbsolutePath());
        Path catalogPath = projectRoot.resolve("backend/src/main/resources/import/group-stage-seed.json");
        Path pdfDirectory = projectRoot.resolve("data/pdfs");
        Path outputDirectory = prepareOutputDirectory(projectRoot.resolve("backend/target/parser-test-output-corpus"));

        GenerationResult result = new PdfPredictionSeedGenerator().generate(catalogPath, pdfDirectory, outputDirectory);

        assertEquals(20, result.seedImport().participants().size());
        assertEquals(1438, result.seedImport().predictions().size());
        assertEquals(18, result.report().pdfsSucceeded());
        assertEquals(2, result.report().pdfsSucceededWithBlockedMatch());
        assertEquals(0, result.report().pdfsFailed());
        assertEquals(2, result.report().blockedMatches());
    }

    @Test
    void shouldClassifyBlockedMatchesAsSuccessWithBlockedMatch() throws Exception {
        Path projectRoot = PdfPredictionSeedGenerator.resolveProjectRoot(Path.of("").toAbsolutePath());
        Path catalogPath = projectRoot.resolve("backend/src/main/resources/import/group-stage-seed.json");
        Path pdfDirectory = projectRoot.resolve("data/pdfs");
        Path outputDirectory = prepareOutputDirectory(projectRoot.resolve("backend/target/parser-test-output-blocked"));

        GenerationResult result = new PdfPredictionSeedGenerator().generate(catalogPath, pdfDirectory, outputDirectory);

        FileReport arthurReport = result.report().files().stream()
                .filter(report -> report.fileName().equals("BOLÃO ARTHUR.pdf"))
                .findFirst()
                .orElseThrow();

        FileReport fillipeReport = result.report().files().stream()
                .filter(report -> report.fileName().equals("BOLÃO FILLIPE.pdf"))
                .findFirst()
                .orElseThrow();

        assertEquals(ParsingStatus.SUCCESS_WITH_BLOCKED_MATCH, arthurReport.status());
        assertEquals(71, arthurReport.validPredictions());
        assertEquals(1, arthurReport.blockedMatches().size());
        assertEquals(1, arthurReport.blockedMatches().getFirst().matchNumber());
        assertEquals("MÉXICO x ÁFRICA DO SUL", arthurReport.blockedMatches().getFirst().confrontation());
        assertEquals("BLOCKED_OR_LATE_ENTRY", arthurReport.blockedMatches().getFirst().reason());

        assertEquals(ParsingStatus.SUCCESS_WITH_BLOCKED_MATCH, fillipeReport.status());
        assertEquals(71, fillipeReport.validPredictions());
        assertEquals(1, fillipeReport.blockedMatches().size());
        assertEquals(1, fillipeReport.blockedMatches().getFirst().matchNumber());
        assertEquals("MÉXICO x ÁFRICA DO SUL", fillipeReport.blockedMatches().getFirst().confrontation());
        assertEquals("BLOCKED_OR_LATE_ENTRY", fillipeReport.blockedMatches().getFirst().reason());

        FileReport mauroReport = result.report().files().stream()
                .filter(report -> report.fileName().equals("BOLÃO MAURO.pdf"))
                .findFirst()
                .orElseThrow();

        assertEquals(ParsingStatus.SUCCESS, mauroReport.status());
        assertEquals(72, mauroReport.validPredictions());
        assertEquals(0, mauroReport.errors().size());
    }

    private Path prepareOutputDirectory(Path outputDirectory) throws IOException {
        if (Files.exists(outputDirectory)) {
            try (var walk = Files.walk(outputDirectory)) {
                walk.sorted((left, right) -> right.compareTo(left))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        Files.createDirectories(outputDirectory);
        return outputDirectory;
    }
}
