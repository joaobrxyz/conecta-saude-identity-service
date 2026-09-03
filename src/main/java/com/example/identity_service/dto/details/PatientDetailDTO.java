package com.example.identity_service.dto.details;

import com.example.identity_service.model.Doctor;
import com.example.identity_service.model.Patient;

import java.util.UUID;

public record PatientDetailDTO(
        UUID id,
        String nome,
        String email,
        String cpf,
        String telefone
) {
    public PatientDetailDTO(Patient patient) {
        this(
                patient.getId(),
                patient.getUser().getNome(),
                patient.getUser().getEmail(),
                patient.getCpf(),
                patient.getUser().getTelefone()
        );
    }
}
