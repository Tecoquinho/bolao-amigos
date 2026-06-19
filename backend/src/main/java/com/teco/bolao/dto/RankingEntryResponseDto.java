package com.teco.bolao.dto;

public record RankingEntryResponseDto(
        Integer position,
        Long participantId,
        String participantName,
        Integer totalPoints,
        Integer exactScoreHits,
        Integer resultHits
) {
}
