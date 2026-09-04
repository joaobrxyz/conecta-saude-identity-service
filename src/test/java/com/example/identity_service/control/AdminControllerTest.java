package com.example.identity_service.control;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    AdminController adminController;

    @Mock
    AdminService adminService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    // Interface para simular o Spring Security no MockMvc
    interface MockAuthenticatedPrincipal extends Principal, AuthenticatedPrincipal {}

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Setup super rápido e isolado (não depende do @WebMvcTest)
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // --- MÉTODOS AUXILIARES ---
    // Passamos o ID no construtor para evitar o erro de reflexão em Records (final)
    private UserDetailDTO criarDetailDTO(UUID id) {
        return new UserDetailDTO(id, "Admin Supremo", "admin@clinica.com", "11999999999");
    }

    @DisplayName("Quando registrar um administrador (POST /admins)")
    @Nested
    class Registrar {

        @DisplayName("Então deve retornar Status 201 Created")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new UserRegisterDTO("Admin", "admin@teste.com", "senha123", "119999");
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(adminService).registerAdmin(any(UserRegisterDTO.class));

                // Quando / Então
                mockMvc.perform(post("/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isCreated());

                verify(adminService, times(1)).registerAdmin(any(UserRegisterDTO.class));
            }
        }
    }

    @DisplayName("Quando listar administradores (GET /admins)")
    @Nested
    class Listar {

        @DisplayName("Então deve retornar Status 200 OK com a Página")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                UserDetailDTO detailDTO = criarDetailDTO(id);

                // O truque para não dar erro 500 no Jackson é adicionar o Sort.by() na página fake!
                Pageable pageable = PageRequest.of(0, 10, Sort.by("nome"));
                Page<UserDetailDTO> paginaMocada = new PageImpl<>(List.of(detailDTO), pageable, 1);

                when(adminService.listAdmins(any(Pageable.class))).thenReturn(paginaMocada);

                // Quando / Então
                mockMvc.perform(get("/admins")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "nome,asc") // Passa o sort na URL também
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].nome").value("Admin Supremo"))
                        .andExpect(jsonPath("$.content[0].email").value("admin@clinica.com"));
            }
        }
    }

    @DisplayName("Quando buscar administrador por ID (GET /admins/{id})")
    @Nested
    class BuscarPorId {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                UserDetailDTO detailDTO = criarDetailDTO(id);

                when(adminService.getAdminById(id)).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(get("/admins/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id.toString()))
                        .andExpect(jsonPath("$.nome").value("Admin Supremo"));
            }
        }
    }

    @DisplayName("Quando atualizar administrador (PATCH /admins/{id})")
    @Nested
    class Atualizar {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                var dtoAtualizacao = new UserUpdateDTO("Admin Novo", null, null);
                String jsonBody = objectMapper.writeValueAsString(dtoAtualizacao);

                UserDetailDTO detailDTO = new UserDetailDTO(id, "Admin Novo", "admin@clinica.com", "11999999999");

                when(adminService.updateAdmin(eq(id), any(UserUpdateDTO.class))).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(patch("/admins/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nome").value("Admin Novo"));
            }
        }
    }
}