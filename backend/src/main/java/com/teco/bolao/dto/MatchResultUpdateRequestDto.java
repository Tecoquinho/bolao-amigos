package com.teco.bolao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchResultUpdateRequestDto(
        @NotNull
        @Min(0)
        Integer homeScore,
        @NotNull
        @Min(0)
        Integer awayScore
) {
}
