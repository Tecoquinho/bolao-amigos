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
    private final PublicSnapshotService publicSnapshotService;

    public MatchResultService(
            MatchService matchService,
            PredictionService predictionService,
            PublicSnapshotService publicSnapshotService
    ) {
        this.matchService = matchService;
        this.predictionService = predictionService;
        this.publicSnapshotService = publicSnapshotService;
    }

    @Transactional
    public MatchResponseDto updateOfficialResult(Long matchId, int homeScore, int awayScore) {
        Match match = matchService.getMatchEntity(matchId);
        match.updateOfficialResult(homeScore, awayScore, OffsetDateTime.now());
        predictionService.recalculatePointsForMatch(match);
        MatchResponseDto response = matchService.toResponse(match);
        publicSnapshotService.refreshSnapshots();
        return response;
    }
}
