package com.example.identity_service.dto.details;

import com.example.identity_service.model.User;

import java.util.UUID;

public record UserDetailDTO(
        UUID id,
        String nome,
        String email,
        String telefone
) {
    public UserDetailDTO(User user) {
        this(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getTelefone()
        );
    }
}
