package com.example.identity_service.dto.authentication;

public record AuthenticationDTO(String email, String senha, boolean manterConectado) {
}
