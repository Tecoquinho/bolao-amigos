package com.teco.bolao.dto;

import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record SeedMatchDto(
        @NotNull
        TournamentPhase phase,
        @NotNull
        @Min(1)
        Integer matchNumber,
        @NotBlank
        String homeTeamCode,
        @NotBlank
        String awayTeamCode,
        @NotNull
        OffsetDateTime startsAt,
        @NotNull
        MatchStatus status,
        String venue,
        @Min(0)
        Integer homeScore,
        @Min(0)
        Integer awayScore
) {
}
