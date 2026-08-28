package com.example.identity_service.dto.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        String telefone,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) implements UserAuthData {
}
