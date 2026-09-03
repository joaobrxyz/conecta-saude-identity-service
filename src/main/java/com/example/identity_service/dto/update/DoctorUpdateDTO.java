package com.example.identity_service.dto.update;

public record DoctorUpdateDTO(
        String nome,
        String email,
        String telefone
) {
}
