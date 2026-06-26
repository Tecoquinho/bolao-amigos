package com.teco.bolao.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teco.bolao.dto.MatchResponseDto;
import com.teco.bolao.dto.ParticipantPredictionResponseDto;
import com.teco.bolao.dto.ParticipantResponseDto;
import com.teco.bolao.dto.RankingEntryResponseDto;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import com.teco.bolao.exception.NotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

@Service
public class PublicSnapshotService {

    private static final TypeReference<List<MatchResponseDto>> MATCH_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<RankingEntryResponseDto>> RANKING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<ParticipantResponseDto>> PARTICIPANT_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<Long, ParticipantResponseDto>> PARTICIPANT_MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<Long, List<ParticipantPredictionResponseDto>>> PREDICTIONS_MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final RankingService rankingService;
    private final MatchService matchService;
    private final ParticipantService participantService;
    private final PredictionService predictionService;
    private final Path snapshotDirectory;
    private final boolean enabled;

    private volatile SnapshotState snapshotState;

    public PublicSnapshotService(
            ObjectMapper objectMapper,
            RankingService rankingService,
            MatchService matchService,
            ParticipantService participantService,
            PredictionService predictionService,
            @Value("${app.snapshots.enabled:true}") boolean enabled,
            @Value("${app.snapshots.directory:data/runtime-snapshots}") String snapshotDirectory
    ) {
        this.objectMapper = objectMapper;
        this.rankingService = rankingService;
        this.matchService = matchService;
        this.participantService = participantService;
        this.predictionService = predictionService;
        this.enabled = enabled;
        this.snapshotDirectory = Path.of(snapshotDirectory);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSnapshots() {
        if (!enabled) {
            return;
        }
        synchronized (this) {
            if (loadSnapshotsFromDisk()) {
                return;
            }
            refreshSnapshots();
        }
    }

    public synchronized void refreshSnapshots() {
        if (!enabled) {
            snapshotState = null;
            return;
        }
        SnapshotState nextState = buildSnapshotState();
        try {
            Files.createDirectories(snapshotDirectory);
            writeJson("matches.json", nextState.matches());
            writeJson("ranking.json", nextState.ranking());
            writeJson("participants.json", nextState.participants());
            writeJson("participants-by-id.json", nextState.participantsById());
            writeJson("participant-predictions.json", nextState.predictionsByParticipant());
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write public snapshots", exception);
        }
        snapshotState = nextState;
    }

    public List<RankingEntryResponseDto> getRanking() {
        if (!enabled) {
            return rankingService.getRanking();
        }
        SnapshotState state = snapshotState;
        return state != null ? state.ranking() : rankingService.getRanking();
    }

    public List<MatchResponseDto> getMatches(
            MatchStatus status,
            TournamentPhase phase,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (!enabled) {
            return matchService.getMatches(status, phase, date);
        }
        SnapshotState state = snapshotState;
        List<MatchResponseDto> matches = state != null ? state.matches() : matchService.getMatches(status, phase, date);
        if (state == null) {
            return matches;
        }

        return matches.stream()
                .filter(match -> status == null || match.status() == status)
                .filter(match -> phase == null || match.phase() == phase)
                .filter(match -> date == null || match.startsAt().toLocalDate().isEqual(date))
                .toList();
    }

    public MatchResponseDto getMatch(Long matchId) {
        if (!enabled) {
            return matchService.getMatch(matchId);
        }
        SnapshotState state = snapshotState;
        if (state == null) {
            return matchService.getMatch(matchId);
        }

        return state.matches().stream()
                .filter(match -> Objects.equals(match.id(), matchId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Match not found: " + matchId));
    }

    public List<ParticipantResponseDto> getParticipants() {
        if (!enabled) {
            return participantService.getParticipants();
        }
        SnapshotState state = snapshotState;
        return state != null ? state.participants() : participantService.getParticipants();
    }

    public ParticipantResponseDto getParticipant(Long participantId) {
        if (!enabled) {
            return participantService.getParticipant(participantId);
        }
        SnapshotState state = snapshotState;
        if (state == null) {
            return participantService.getParticipant(participantId);
        }

        ParticipantResponseDto participant = state.participantsById().get(participantId);
        if (participant == null) {
            throw new NotFoundException("Participant not found: " + participantId);
        }
        return participant;
    }

    public List<ParticipantPredictionResponseDto> getPredictionsByParticipant(Long participantId) {
        if (!enabled) {
            participantService.getParticipant(participantId);
            return predictionService.getPredictionsByParticipant(participantId);
        }
        SnapshotState state = snapshotState;
        if (state == null) {
            participantService.getParticipant(participantId);
            return predictionService.getPredictionsByParticipant(participantId);
        }

        if (!state.participantsById().containsKey(participantId)) {
            throw new NotFoundException("Participant not found: " + participantId);
        }
        return state.predictionsByParticipant().getOrDefault(participantId, List.of());
    }

    private SnapshotState buildSnapshotState() {
        List<MatchResponseDto> matches = List.copyOf(matchService.getMatches(null, null, null));
        List<RankingEntryResponseDto> ranking = List.copyOf(rankingService.getRanking());
        List<ParticipantResponseDto> participants = List.copyOf(participantService.getParticipants());

        Map<Long, ParticipantResponseDto> participantsById = new LinkedHashMap<>();
        Map<Long, List<ParticipantPredictionResponseDto>> predictionsByParticipant = new LinkedHashMap<>();

        for (ParticipantResponseDto participant : participants) {
            participantsById.put(participant.id(), participant);
            predictionsByParticipant.put(
                    participant.id(),
                    List.copyOf(predictionService.getPredictionsByParticipant(participant.id()))
            );
        }

        return new SnapshotState(
                matches,
                ranking,
                participants,
                Map.copyOf(participantsById),
                Map.copyOf(predictionsByParticipant)
        );
    }

    private boolean loadSnapshotsFromDisk() {
        try {
            Path matchesFile = snapshotDirectory.resolve("matches.json");
            Path rankingFile = snapshotDirectory.resolve("ranking.json");
            Path participantsFile = snapshotDirectory.resolve("participants.json");
            Path participantsByIdFile = snapshotDirectory.resolve("participants-by-id.json");
            Path predictionsByParticipantFile = snapshotDirectory.resolve("participant-predictions.json");

            if (!Files.exists(matchesFile)
                    || !Files.exists(rankingFile)
                    || !Files.exists(participantsFile)
                    || !Files.exists(participantsByIdFile)
                    || !Files.exists(predictionsByParticipantFile)) {
                return false;
            }

            snapshotState = new SnapshotState(
                    List.copyOf(objectMapper.readValue(matchesFile.toFile(), MATCH_LIST_TYPE)),
                    List.copyOf(objectMapper.readValue(rankingFile.toFile(), RANKING_LIST_TYPE)),
                    List.copyOf(objectMapper.readValue(participantsFile.toFile(), PARTICIPANT_LIST_TYPE)),
                    Map.copyOf(objectMapper.readValue(participantsByIdFile.toFile(), PARTICIPANT_MAP_TYPE)),
                    normalizePredictionMap(objectMapper.readValue(predictionsByParticipantFile.toFile(), PREDICTIONS_MAP_TYPE))
            );
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private Map<Long, List<ParticipantPredictionResponseDto>> normalizePredictionMap(
            Map<Long, List<ParticipantPredictionResponseDto>> raw
    ) {
        Map<Long, List<ParticipantPredictionResponseDto>> normalized = new LinkedHashMap<>();
        raw.forEach((participantId, predictions) -> normalized.put(participantId, List.copyOf(predictions)));
        return Map.copyOf(normalized);
    }

    private void writeJson(String fileName, Object payload) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(snapshotDirectory.resolve(fileName).toFile(), payload);
    }

    private record SnapshotState(
            List<MatchResponseDto> matches,
            List<RankingEntryResponseDto> ranking,
            List<ParticipantResponseDto> participants,
            Map<Long, ParticipantResponseDto> participantsById,
            Map<Long, List<ParticipantPredictionResponseDto>> predictionsByParticipant
    ) {
    }
}
