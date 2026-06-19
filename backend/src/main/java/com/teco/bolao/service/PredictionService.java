package com.teco.bolao.service;

import com.teco.bolao.dto.ParticipantPredictionResponseDto;
import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.repository.PredictionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final ScoreCalculationService scoreCalculationService;

    public PredictionService(
            PredictionRepository predictionRepository,
            ScoreCalculationService scoreCalculationService
    ) {
        this.predictionRepository = predictionRepository;
        this.scoreCalculationService = scoreCalculationService;
    }

    public List<ParticipantPredictionResponseDto> getPredictionsByParticipant(Long participantId) {
        return predictionRepository.findByParticipantIdOrderByMatchStartsAtAsc(participantId)
                .stream()
                .map(this::toParticipantPredictionResponse)
                .toList();
    }

    @Transactional
    public void recalculatePointsForMatch(Match match) {
        List<Prediction> predictions = predictionRepository.findByMatchId(match.getId());

        if (match.getHomeScore() == null || match.getAwayScore() == null) {
            throw new IllegalArgumentException("Official match result must be defined before recalculating predictions");
        }

        for (Prediction prediction : predictions) {
            int points = scoreCalculationService.calculatePoints(
                    match.getHomeScore(),
                    match.getAwayScore(),
                    prediction.getPredictedHomeScore(),
                    prediction.getPredictedAwayScore()
            );
            prediction.updatePointsAwarded(points);
        }
    }

    private ParticipantPredictionResponseDto toParticipantPredictionResponse(Prediction prediction) {
        Match match = prediction.getMatch();

        return new ParticipantPredictionResponseDto(
                prediction.getId(),
                match.getId(),
                match.getPhase(),
                match.getMatchNumber(),
                match.getStartsAt(),
                match.getStatus(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getFifaCode(),
                match.getHomeTeam().getFlagUrl(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getFifaCode(),
                match.getAwayTeam().getFlagUrl(),
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                match.getHomeScore(),
                match.getAwayScore(),
                prediction.getPointsAwarded()
        );
    }
}
