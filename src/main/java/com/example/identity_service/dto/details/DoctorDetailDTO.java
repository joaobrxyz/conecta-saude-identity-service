package com.example.identity_service.dto.details;

import com.example.identity_service.model.Doctor;

import java.util.UUID;

public record DoctorDetailDTO(
        UUID id,
        String nome,
        String email,
        String crm,
        String especialidade,
        String telefone
) {
    public DoctorDetailDTO(Doctor doctor) {
        this(
                doctor.getId(),
                doctor.getUser().getNome(),
                doctor.getUser().getEmail(),
                doctor.getCrm(),
                doctor.getSpecialty().getNome(),
                doctor.getUser().getTelefone()
        );
    }
}
