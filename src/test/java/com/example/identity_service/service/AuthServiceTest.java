package com.example.identity_service.service;

import com.example.identity_service.dto.authentication.*;
import com.example.identity_service.exception.InvalidTokenException;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.model.PasswordResetToken;
import com.example.identity_service.model.RefreshToken;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.PasswordResetTokenRepository;
import com.example.identity_service.repository.UserRepository;
import com.example.identity_service.service.token.RefreshTokenService;
import com.example.identity_service.service.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    TokenService tokenService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    AuthenticationManager authenticatorManager;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    // --- MÉTODOS AUXILIARES ---
    private User criarUserValido() {
        User user = new User();
        setField(user, "id", UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884"));
        user.setNome("João");
        user.setEmail("joao@teste.com");
        user.setSenha("senhaCriptografada");
        user.setTipoUser(TipoUser.PACIENTE);
        user.setAtivo(true);
        return user;
    }

    private RefreshToken criarRefreshToken(User user, boolean expirado) {
        RefreshToken token = new RefreshToken();
        setField(token, "token", "refresh-token-valido");
        setField(token, "user", user);
        setField(token, "manterConectado", true);

        // Manipula a data para simular se está expirado ou não
        LocalDateTime dataExpiracao = expirado ? LocalDateTime.now().minusDays(1) : LocalDateTime.now().plusDays(7);
        setField(token, "expiryDate", dataExpiracao);

        return token;
    }

    private PasswordResetToken criarPasswordResetToken(User user, boolean expirado) {
        PasswordResetToken token = new PasswordResetToken("reset-token-123", user);

        // Sobrescreve a data criada pelo construtor via reflection para forçar o teste de expiração
        LocalDateTime dataExpiracao = expirado ? LocalDateTime.now().minusMinutes(5) : LocalDateTime.now().plusMinutes(30);
        setField(token, "expiryDate", dataExpiracao);

        return token;
    }

    @DisplayName("Quando autenticar um usuário (Login)")
    @Nested
    class Autenticar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado credenciais válidas, deve retornar os tokens")
            @Test
            void teste1() {
                // Dado
                var dto = new AuthenticationDTO("joao@teste.com", "123456", true);
                User user = criarUserValido();

                Authentication auth = mock(Authentication.class);
                when(auth.getPrincipal()).thenReturn(user);
                when(authenticatorManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

                when(tokenService.generateToken(user)).thenReturn("access-token-jwt");

                RefreshToken refreshTokenMock = criarRefreshToken(user, false);
                when(refreshTokenService.createRefreshToken(user, true)).thenReturn(refreshTokenMock);

                // Quando
                TokenResponseDTO atual = authService.authenticate(dto);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.accessToken()).isEqualTo("access-token-jwt");
                assertThat(atual.refreshToken()).isEqualTo("refresh-token-valido");
            }
        }
    }

    @DisplayName("Quando carregar usuário pelo username (Spring Security)")
    @Nested
    class LoadUserByUsername {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um e-mail que existe no banco")
            @Test
            void teste1() {
                // Dado
                User user = criarUserValido();
                when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

                // Quando
                UserDetails atual = authService.loadUserByUsername(user.getEmail());

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.getUsername()).isEqualTo(user.getEmail());
            }
        }

        @DisplayName("Então deve lançar erro")
        @Nested
        class Falha {

            @DisplayName("Dado um e-mail que não existe")
            @Test
            void teste1() {
                // Dado
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> authService.loadUserByUsername("fantasma@teste.com"))
                        .isInstanceOf(UsernameNotFoundException.class)
                        .hasMessage("O usuário não foi encontrado");
            }
        }
    }

    @DisplayName("Quando atualizar o token (Refresh Token)")
    @Nested
    class RefreshTokenFluxo {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um refresh token válido e não expirado")
            @Test
            void teste1() {
                // Dado
                var dto = new RefreshTokenRequestDTO("refresh-token-valido");
                User user = criarUserValido();
                RefreshToken refreshTokenValido = criarRefreshToken(user, false);

                when(refreshTokenService.findByToken(dto.refreshToken())).thenReturn(refreshTokenValido);
                when(tokenService.generateToken(user)).thenReturn("novo-access-token");

                RefreshToken novoRefreshTokenMock = criarRefreshToken(user, false);
                setField(novoRefreshTokenMock, "token", "novo-refresh-token");
                when(refreshTokenService.createRefreshToken(user, true)).thenReturn(novoRefreshTokenMock);

                // Quando
                TokenResponseDTO atual = authService.refreshToken(dto);

                // Então
                assertThat(atual.accessToken()).isEqualTo("novo-access-token");
                assertThat(atual.refreshToken()).isEqualTo("novo-refresh-token");

                // O token antigo deve ser deletado
                verify(refreshTokenService, times(1)).deleteToken(refreshTokenValido);
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um refresh token que já passou da data de expiração")
            @Test
            void teste1() {
                // Dado
                var dto = new RefreshTokenRequestDTO("token-vencido");
                User user = criarUserValido();
                RefreshToken refreshTokenVencido = criarRefreshToken(user, true); // Expirado!

                when(refreshTokenService.findByToken(dto.refreshToken())).thenReturn(refreshTokenVencido);

                // Quando / Então
                assertThatThrownBy(() -> authService.refreshToken(dto))
                        .isInstanceOf(InvalidTokenException.class)
                        .hasMessage("Refresh Token expirado. Faça login novamente.");

                // Garante que o token vencido foi deletado do banco por segurança
                verify(refreshTokenService, times(1)).deleteToken(refreshTokenVencido);
                verify(tokenService, never()).generateToken(any());
            }
        }
    }

    @DisplayName("Quando revogar um token (Logout)")
    @Nested
    class RevogarToken {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um refresh token válido")
            @Test
            void teste1() {
                // Dado
                var dto = new RefreshTokenRequestDTO("refresh-token-valido");
                RefreshToken tokenMock = criarRefreshToken(criarUserValido(), false);
                when(refreshTokenService.findByToken(dto.refreshToken())).thenReturn(tokenMock);

                // Quando
                authService.revogarToken(dto);

                // Então
                verify(refreshTokenService, times(1)).deleteToken(tokenMock);
            }
        }
    }

    @DisplayName("Quando solicitar recuperação de senha (Forgot Password)")
    @Nested
    class ForgotPassword {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um e-mail válido cadastrado")
            @Test
            void teste1() {
                // Dado
                var dto = new ForgotPasswordDTO("joao@teste.com");
                User user = criarUserValido();
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));

                // Quando
                authService.forgotPassword(dto);

                // Então
                verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
            }
        }

        @DisplayName("Então deve lançar erro")
        @Nested
        class Falha {

            @DisplayName("Dado um e-mail não cadastrado")
            @Test
            void teste1() {
                // Dado
                var dto = new ForgotPasswordDTO("naoexiste@teste.com");
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> authService.forgotPassword(dto))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Usuário não encontrado.");

                verify(passwordResetTokenRepository, never()).save(any());
            }
        }
    }

    @DisplayName("Quando redefinir a senha (Reset Password)")
    @Nested
    class ResetPassword {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um token válido e não expirado")
            @Test
            void teste1() {
                // Dado
                String tokenString = "reset-token-123";
                var dto = new ResetPasswordDTO("NovaSenhaForte123");

                User user = criarUserValido();
                PasswordResetToken resetTokenMock = criarPasswordResetToken(user, false);

                when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(resetTokenMock));
                when(passwordEncoder.encode(dto.novaSenha())).thenReturn("novaSenhaCriptografada");

                // Quando
                authService.resetPassword(tokenString, dto);

                // Então
                assertThat(user.getSenha()).isEqualTo("novaSenhaCriptografada");
                verify(userRepository, times(1)).save(user);
                verify(passwordResetTokenRepository, times(1)).delete(resetTokenMock);
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um token que não existe no banco")
            @Test
            void teste1() {
                // Dado
                String tokenString = "token-invalido";
                var dto = new ResetPasswordDTO("Senha123");

                when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> authService.resetPassword(tokenString, dto))
                        .isInstanceOf(InvalidTokenException.class)
                        .hasMessage("Token de redefinição de senha inválido.");
            }

            @DisplayName("Dado um token que já expirou")
            @Test
            void teste2() {
                // Dado
                String tokenString = "reset-token-123";
                var dto = new ResetPasswordDTO("Senha123");

                User user = criarUserValido();
                PasswordResetToken resetTokenExpirado = criarPasswordResetToken(user, true); // Expirado!

                when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(resetTokenExpirado));

                // Quando / Então
                assertThatThrownBy(() -> authService.resetPassword(tokenString, dto))
                        .isInstanceOf(InvalidTokenException.class)
                        .hasMessage("Token de redefinição de senha expirado.");

                verify(userRepository, never()).save(any());
            }
        }
    }
}