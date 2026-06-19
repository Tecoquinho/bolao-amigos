package com.teco.bolao.service;

import com.teco.bolao.dto.MatchResponseDto;
import com.teco.bolao.entity.Match;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchResultService {

    private final MatchService matchService;
    private final PredictionService predictionService;

    public MatchResultService(
            MatchService matchService,
            PredictionService predictionService
    ) {
        this.matchService = matchService;
        this.predictionService = predictionService;
    }

    @Transactional
    public MatchResponseDto updateOfficialResult(Long matchId, int homeScore, int awayScore) {
        Match match = matchService.getMatchEntity(matchId);
        match.updateOfficialResult(homeScore, awayScore, OffsetDateTime.now());
        predictionService.recalculatePointsForMatch(match);
        return matchService.toResponse(match);
    }
}
