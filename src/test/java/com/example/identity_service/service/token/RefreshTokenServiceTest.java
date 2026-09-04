package com.example.identity_service.service.token;

import com.example.identity_service.exception.InvalidTokenException;
import com.example.identity_service.model.RefreshToken;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Mock
    RefreshTokenRepository repository;

    @Captor
    ArgumentCaptor<RefreshToken> tokenCaptor;

    // --- MÉTODOS AUXILIARES ---
    private User criarUserValido() {
        User user = new User();
        setField(user, "id", UUID.randomUUID());
        user.setEmail("teste@clinica.com");
        return user;
    }

    private RefreshToken criarToken(boolean expirado) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(criarUserValido());
        token.setManterConectado(true);

        LocalDateTime dataExpiracao = expirado ? LocalDateTime.now().minusDays(1) : LocalDateTime.now().plusDays(7);
        token.setExpiryDate(dataExpiracao);

        return token;
    }

    @DisplayName("Quando criar um refresh token")
    @Nested
    class CriarRefreshToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @BeforeEach
            void beforeEach() {
                // Simula o repositório devolvendo o próprio objeto salvo
                when(repository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
            }

            @DisplayName("Dado que o usuário escolheu 'Manter Conectado', o token deve expirar em 7 dias")
            @Test
            void teste1() {
                // Dado
                User user = criarUserValido();
                boolean manterConectado = true;

                // Quando
                RefreshToken atual = refreshTokenService.createRefreshToken(user, manterConectado);

                // Então
                verify(repository).save(tokenCaptor.capture());
                RefreshToken tokenSalvo = tokenCaptor.getValue();

                assertThat(atual).isNotNull();
                assertThat(tokenSalvo.isManterConectado()).isTrue();
                // A data de expiração deve ser no futuro (aproximadamente 7 dias a partir de agora)
                assertThat(tokenSalvo.getExpiryDate()).isAfter(LocalDateTime.now().plusDays(6));
            }

            @DisplayName("Dado que o usuário NÃO escolheu 'Manter Conectado', o token deve expirar em 1 hora")
            @Test
            void teste2() {
                // Dado
                User user = criarUserValido();
                boolean manterConectado = false;

                // Quando
                RefreshToken atual = refreshTokenService.createRefreshToken(user, manterConectado);

                // Então
                verify(repository).save(tokenCaptor.capture());
                RefreshToken tokenSalvo = tokenCaptor.getValue();

                assertThat(atual).isNotNull();
                assertThat(tokenSalvo.isManterConectado()).isFalse();
                // A data de expiração deve ser no futuro, mas antes de amanhã (cerca de 1h)
                assertThat(tokenSalvo.getExpiryDate())
                        .isAfter(LocalDateTime.now())
                        .isBefore(LocalDateTime.now().plusHours(2));
            }
        }
    }

    @DisplayName("Quando verificar a expiração do token")
    @Nested
    class VerificarExpiracao {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um token com data no futuro")
            @Test
            void teste1() {
                // Dado
                RefreshToken tokenValido = criarToken(false);

                // Quando
                RefreshToken atual = refreshTokenService.verifyExpiration(tokenValido);

                // Então
                assertThat(atual).isEqualTo(tokenValido);
                verify(repository, never()).delete(any());
            }
        }

        @DisplayName("Então deve lançar erro de validação e deletar o token")
        @Nested
        class Falha {

            @DisplayName("Dado um token com data no passado (expirado)")
            @Test
            void teste1() {
                // Dado
                RefreshToken tokenVencido = criarToken(true);

                // Quando / Então
                assertThatThrownBy(() -> refreshTokenService.verifyExpiration(tokenVencido))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Refresh token expirado. Faça login novamente.");

                // Garante que o método deletou o token vencido do banco
                verify(repository, times(1)).delete(tokenVencido);
            }
        }
    }

    @DisplayName("Quando buscar token pela string")
    @Nested
    class BuscarPorToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado uma string de token que existe no banco")
            @Test
            void teste1() {
                // Dado
                RefreshToken tokenBanco = criarToken(false);
                when(repository.findByToken(anyString())).thenReturn(Optional.of(tokenBanco));

                // Quando
                RefreshToken atual = refreshTokenService.findByToken(tokenBanco.getToken());

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.getToken()).isEqualTo(tokenBanco.getToken());
            }
        }

        @DisplayName("Então deve lançar erro")
        @Nested
        class Falha {

            @DisplayName("Dado uma string de token inexistente")
            @Test
            void teste1() {
                // Dado
                when(repository.findByToken(anyString())).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> refreshTokenService.findByToken("token-fantasma"))
                        .isInstanceOf(InvalidTokenException.class)
                        .hasMessage("Token não encontrado");
            }
        }
    }

    @DisplayName("Quando deletar um token (Logout)")
    @Nested
    class DeletarToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado uma entidade de token válida")
            @Test
            void teste1() {
                // Dado
                RefreshToken token = criarToken(false);

                // Quando
                refreshTokenService.deleteToken(token);

                // Então
                verify(repository, times(1)).delete(token);
            }
        }
    }
}