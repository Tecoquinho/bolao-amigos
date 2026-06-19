package com.teco.bolao.dto;

import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import java.time.OffsetDateTime;

public record MatchResponseDto(
        Long id,
        TournamentPhase phase,
        Integer matchNumber,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        OffsetDateTime startsAt,
        MatchStatus status,
        String venue,
        Integer homeScore,
        Integer awayScore,
        OffsetDateTime officialResultAt
) {
}
