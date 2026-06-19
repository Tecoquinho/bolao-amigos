package com.teco.bolao.dto;

public record SeedImportResultDto(
        int participantsCreated,
        int participantsSkipped,
        int teamsCreated,
        int teamsSkipped,
        int matchesCreated,
        int matchesSkipped,
        int predictionsCreated,
        int predictionsSkipped
) {
}
