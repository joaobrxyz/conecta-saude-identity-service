package com.example.identity_service.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "O Refresh Token é obrigatório")
        String refreshToken
) {
}
