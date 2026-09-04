package com.example.identity_service.service.token;

import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    TokenService tokenService;

    // Chave secreta falsa que será injetada para os testes rodarem
    private final String SECRET_TESTE = "minha-chave-secreta-muito-segura-123456";

    // --- MÉTODOS AUXILIARES ---
    private User criarUserValido() {
        User user = new User();
        setField(user, "id", UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884"));
        user.setTipoUser(TipoUser.MEDICO);
        return user;
    }

    @BeforeEach
    void setup() {
        // Injeta o segredo na anotação @Value antes de todos os testes
        setField(tokenService, "secret", SECRET_TESTE);
    }

    @DisplayName("Quando gerar um token")
    @Nested
    class GerarToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um usuário com ID e TipoUser válidos")
            @Test
            void teste1() {
                // Dado
                User user = criarUserValido();

                // Quando
                String token = tokenService.generateToken(user);

                // Então
                assertThat(token).isNotBlank();
                // Um JWT autêntico sempre possui 3 partes separadas por ponto (Header, Payload, Signature)
                assertThat(token.split("\\.")).hasSize(3);
            }
        }
    }

    @DisplayName("Quando validar um token")
    @Nested
    class ValidarToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um token autêntico e não expirado")
            @Test
            void teste1() {
                // Dado - Geramos um token real usando o nosso próprio método
                User user = criarUserValido();
                String tokenGerado = tokenService.generateToken(user);

                // Quando
                String subjectRetornado = tokenService.validateToken(tokenGerado);

                // Então
                assertThat(subjectRetornado).isEqualTo(user.getId().toString());
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um token com assinatura inválida, alterado ou falso")
            @Test
            void teste1() {
                // Dado
                String tokenInvalido = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payloadFalso.assinaturaFalsa123";

                // Quando / Então
                assertThatThrownBy(() -> tokenService.validateToken(tokenInvalido))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("Token JWT inválido");
            }
        }
    }

    @DisplayName("Quando extrair a role do token")
    @Nested
    class ExtrairRole {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um token autêntico")
            @Test
            void teste1() {
                // Dado
                User user = criarUserValido(); // A role configurada aqui é MEDICO
                String tokenGerado = tokenService.generateToken(user);

                // Quando
                String role = tokenService.getRoleFromToken(tokenGerado);

                // Então
                assertThat(role).isEqualTo(TipoUser.MEDICO.name());
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um token corrompido")
            @Test
            void teste1() {
                // Dado
                String tokenInvalido = "token.totalmente.quebrado";

                // Quando / Então
                assertThatThrownBy(() -> tokenService.getRoleFromToken(tokenInvalido))
                        .isInstanceOf(Exception.class);
            }
        }
    }
}