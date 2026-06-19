package com.teco.bolao.dto;

public record PredictionResponseDto(
        Long id,
        Long participantId,
        Long matchId,
        Integer predictedHomeScore,
        Integer predictedAwayScore,
        Integer pointsAwarded
) {
}
