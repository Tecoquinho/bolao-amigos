package com.teco.bolao.dto;

import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import java.time.OffsetDateTime;

public record ParticipantPredictionResponseDto(
        Long predictionId,
        Long matchId,
        TournamentPhase phase,
        Integer matchNumber,
        OffsetDateTime startsAt,
        MatchStatus matchStatus,
        String homeTeamName,
        String homeTeamFifaCode,
        String homeTeamFlagUrl,
        String awayTeamName,
        String awayTeamFifaCode,
        String awayTeamFlagUrl,
        Integer predictedHomeScore,
        Integer predictedAwayScore,
        Integer officialHomeScore,
        Integer officialAwayScore,
        Integer pointsAwarded
) {
}
