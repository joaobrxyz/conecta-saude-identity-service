package com.example.identity_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoctorRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        @NotBlank(message = "O CRM é obrigatório")
        String crm,

        @NotNull(message = "O ID da especialidade é obrigatório")
        Long especialidadeId
        ) {
}
