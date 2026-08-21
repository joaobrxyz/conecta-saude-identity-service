package com.example.identity_service.dto;

public record AuthenticationDTO(String email, String senha, boolean manterConectado) {
}
