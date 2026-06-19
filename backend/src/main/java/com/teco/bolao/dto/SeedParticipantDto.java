package com.teco.bolao.dto;

import jakarta.validation.constraints.NotBlank;

public record SeedParticipantDto(
        @NotBlank
        String name
) {
}
