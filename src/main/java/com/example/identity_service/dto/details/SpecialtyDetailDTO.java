package com.example.identity_service.dto.details;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpecialtyDetailDTO(
        @NotNull
        Long id,

        @NotBlank
        String nome
) {
}
