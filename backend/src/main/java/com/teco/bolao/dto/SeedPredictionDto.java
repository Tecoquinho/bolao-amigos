package com.teco.bolao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeedPredictionDto(
        @NotBlank
        String participantName,
        @NotNull
        @Min(1)
        Integer matchNumber,
        @NotNull
        @Min(0)
        Integer predictedHomeScore,
        @NotNull
        @Min(0)
        Integer predictedAwayScore
) {
}
