package com.teco.bolao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeedTeamDto(
        @NotBlank
        String name,
        @NotBlank
        @Size(min = 3, max = 3)
        String fifaCode,
        String flagUrl
) {
}
