package com.teco.bolao.service;

import com.teco.bolao.dto.RankingEntryResponseDto;
import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.repository.ParticipantRepository;
import com.teco.bolao.repository.PredictionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RankingService {

    private final PredictionRepository predictionRepository;
    private final ParticipantRepository participantRepository;
    private final ScoreCalculationService scoreCalculationService;

    public RankingService(
            PredictionRepository predictionRepository,
            ParticipantRepository participantRepository,
            ScoreCalculationService scoreCalculationService
    ) {
        this.predictionRepository = predictionRepository;
        this.participantRepository = participantRepository;
        this.scoreCalculationService = scoreCalculationService;
    }

    public List<RankingEntryResponseDto> getRanking() {
        Map<Long, RankingAccumulator> accumulators = new LinkedHashMap<>();

        for (Participant participant : participantRepository.findAllByOrderByNameAsc()) {
            accumulators.put(
                    participant.getId(),
                    new RankingAccumulator(participant.getId(), participant.getName())
            );
        }

        for (Prediction prediction : predictionRepository.findAll()) {
            Participant participant = prediction.getParticipant();
            RankingAccumulator accumulator = accumulators.computeIfAbsent(
                    participant.getId(),
                    ignored -> new RankingAccumulator(participant.getId(), participant.getName())
            );

            Match match = prediction.getMatch();
            if (match.getStatus() != MatchStatus.FINISHED || match.getHomeScore() == null || match.getAwayScore() == null) {
                continue;
            }

            accumulator.addPoints(prediction.getPointsAwarded());

            if (scoreCalculationService.isExactScore(
                    match.getHomeScore(),
                    match.getAwayScore(),
                    prediction.getPredictedHomeScore(),
                    prediction.getPredictedAwayScore()
            )) {
                accumulator.incrementExactScoreHits();
            }

            if (scoreCalculationService.isCorrectOutcome(
                    match.getHomeScore(),
                    match.getAwayScore(),
                    prediction.getPredictedHomeScore(),
                    prediction.getPredictedAwayScore()
            )) {
                accumulator.incrementResultHits();
            }
        }

        List<RankingAccumulator> sorted = new ArrayList<>(accumulators.values());
        sorted.sort(
                Comparator.comparingInt(RankingAccumulator::getTotalPoints).reversed()
                        .thenComparing(Comparator.comparingInt(RankingAccumulator::getExactScoreHits).reversed())
                        .thenComparing(Comparator.comparingInt(RankingAccumulator::getResultHits).reversed())
                        .thenComparing(RankingAccumulator::getParticipantName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RankingAccumulator::getParticipantName)
        );

        List<RankingEntryResponseDto> ranking = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            RankingAccumulator accumulator = sorted.get(index);
            ranking.add(new RankingEntryResponseDto(
                    index + 1,
                    accumulator.getParticipantId(),
                    accumulator.getParticipantName(),
                    accumulator.getTotalPoints(),
                    accumulator.getExactScoreHits(),
                    accumulator.getResultHits()
            ));
        }

        return ranking;
    }

    private static final class RankingAccumulator {

        private final Long participantId;
        private final String participantName;
        private int totalPoints;
        private int exactScoreHits;
        private int resultHits;

        private RankingAccumulator(Long participantId, String participantName) {
            this.participantId = participantId;
            this.participantName = participantName;
        }

        private Long getParticipantId() {
            return participantId;
        }

        private String getParticipantName() {
            return participantName;
        }

        private int getTotalPoints() {
            return totalPoints;
        }

        private int getExactScoreHits() {
            return exactScoreHits;
        }

        private int getResultHits() {
            return resultHits;
        }

        private void addPoints(int points) {
            this.totalPoints += points;
        }

        private void incrementExactScoreHits() {
            this.exactScoreHits++;
        }

        private void incrementResultHits() {
            this.resultHits++;
        }
    }
}
