package com.example.identity_service.dto.update;

public record UserUpdateDTO(
        String nome,
        String email,
        String telefone
) {
}
