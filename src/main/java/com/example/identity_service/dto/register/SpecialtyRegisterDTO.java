package com.example.identity_service.dto.register;

import jakarta.validation.constraints.NotBlank;

public record SpecialtyRegisterDTO (
        @NotBlank
        String nome
) {
}
