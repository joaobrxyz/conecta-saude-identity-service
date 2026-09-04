package com.example.identity_service.control;

import com.example.identity_service.dto.authentication.*;
import com.example.identity_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    AuthController authController;

    @Mock
    AuthService authService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Setup super rápido e isolado
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @DisplayName("Quando realizar login (POST /auth/login)")
    @Nested
    class Login {

        @DisplayName("Então deve retornar Status 200 OK com os tokens")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new AuthenticationDTO("paciente@clinica.com", "senha123", true);
                var tokenResponse = new TokenResponseDTO("access-token-jwt", "refresh-token-uuid");
                String jsonBody = objectMapper.writeValueAsString(dto);

                when(authService.authenticate(any(AuthenticationDTO.class))).thenReturn(tokenResponse);

                // Quando / Então
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("access-token-jwt"))
                        .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"));

                verify(authService, times(1)).authenticate(any(AuthenticationDTO.class));
            }
        }
    }

    @DisplayName("Quando solicitar renovação de token (POST /auth/refresh)")
    @Nested
    class RefreshToken {

        @DisplayName("Então deve retornar Status 200 OK com os novos tokens")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new RefreshTokenRequestDTO("refresh-token-antigo");
                var tokenResponse = new TokenResponseDTO("novo-access-token", "novo-refresh-token");
                String jsonBody = objectMapper.writeValueAsString(dto);

                when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(tokenResponse);

                // Quando / Então
                mockMvc.perform(post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("novo-access-token"))
                        .andExpect(jsonPath("$.refreshToken").value("novo-refresh-token"));
            }
        }
    }

    @DisplayName("Quando realizar logout (POST /auth/logout)")
    @Nested
    class Logout {

        @DisplayName("Então deve retornar Status 200 OK sem corpo")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new RefreshTokenRequestDTO("refresh-token-para-deletar");
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(authService).revogarToken(any(RefreshTokenRequestDTO.class));

                // Quando / Então
                mockMvc.perform(post("/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk());

                verify(authService, times(1)).revogarToken(any(RefreshTokenRequestDTO.class));
            }
        }
    }

    @DisplayName("Quando solicitar esqueci minha senha (POST /auth/forgot-password)")
    @Nested
    class ForgotPassword {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new ForgotPasswordDTO("paciente@clinica.com");
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(authService).forgotPassword(any(ForgotPasswordDTO.class));

                // Quando / Então
                mockMvc.perform(post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk());

                verify(authService, times(1)).forgotPassword(any(ForgotPasswordDTO.class));
            }
        }
    }

    @DisplayName("Quando redefinir a senha (POST /auth/reset-password/{token})")
    @Nested
    class ResetPassword {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                String tokenNaUrl = "token-uuid-12345";
                var dto = new ResetPasswordDTO("NovaSenhaSegura123!");
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(authService).resetPassword(eq(tokenNaUrl), any(ResetPasswordDTO.class));

                // Quando / Então
                mockMvc.perform(post("/auth/reset-password/{token}", tokenNaUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk());

                verify(authService, times(1)).resetPassword(eq(tokenNaUrl), any(ResetPasswordDTO.class));
            }
        }
    }
}