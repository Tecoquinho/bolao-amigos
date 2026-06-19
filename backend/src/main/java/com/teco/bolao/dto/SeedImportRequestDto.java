package com.teco.bolao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SeedImportRequestDto(
        @NotNull
        List<@Valid SeedParticipantDto> participants,
        @NotNull
        List<@Valid SeedTeamDto> teams,
        @NotNull
        List<@Valid SeedMatchDto> matches,
        @NotNull
        List<@Valid SeedPredictionDto> predictions
) {
}
