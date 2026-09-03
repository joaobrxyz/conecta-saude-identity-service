package com.example.identity_service.dto.update;

public record PatientUpdateDTO(
        String nome,
        String email,
        String telefone
) {
}
