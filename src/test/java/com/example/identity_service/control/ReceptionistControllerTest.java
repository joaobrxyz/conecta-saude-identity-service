package com.example.identity_service.control;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.service.ReceptionistService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReceptionistControllerTest {

    @InjectMocks
    ReceptionistController receptionistController;

    @Mock
    ReceptionistService receptionistService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Setup isolado com suporte à resolução do @PageableDefault
        mockMvc = MockMvcBuilders.standaloneSetup(receptionistController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // --- MÉTODOS AUXILIARES ---
    // Estrutura baseada no UserDetailDTO (id, nome, email, telefone)
    private UserDetailDTO criarDetailDTO(UUID id) {
        return new UserDetailDTO(
                id,
                "Ana Recepcionista",
                "ana@clinica.com",
                "11999999999"
        );
    }

    @DisplayName("Quando registrar uma recepcionista (POST /receptionists)")
    @Nested
    class Registrar {

        @DisplayName("Então deve retornar Status 201 Created")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado - Enviando o DTO completo com a senha para passar no @Valid
                var dto = new UserRegisterDTO("Ana Recepcionista", "ana@clinica.com", "senhaForte123", "11999999999");
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(receptionistService).registerReceptionist(any(UserRegisterDTO.class));

                // Quando / Então
                mockMvc.perform(post("/receptionists")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isCreated());

                verify(receptionistService, times(1)).registerReceptionist(any(UserRegisterDTO.class));
            }
        }
    }

    @DisplayName("Quando listar recepcionistas (GET /receptionists)")
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

                // Aplicando o Sort falso para o Jackson serializar corretamente
                Pageable pageable = PageRequest.of(0, 10, Sort.by("nome"));
                Page<UserDetailDTO> paginaMocada = new PageImpl<>(List.of(detailDTO), pageable, 1);

                when(receptionistService.listReceptionists(any(Pageable.class))).thenReturn(paginaMocada);

                // Quando / Então
                mockMvc.perform(get("/receptionists")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "nome,asc")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].nome").value("Ana Recepcionista"))
                        .andExpect(jsonPath("$.content[0].email").value("ana@clinica.com"));
            }
        }
    }

    @DisplayName("Quando buscar recepcionista por ID (GET /receptionists/{id})")
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

                when(receptionistService.getReceptionistById(id)).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(get("/receptionists/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id.toString()))
                        .andExpect(jsonPath("$.nome").value("Ana Recepcionista"));
            }
        }
    }

    @DisplayName("Quando atualizar recepcionista (PUT /receptionists/{id})")
    @Nested
    class Atualizar {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                var dtoAtualizacao = new UserUpdateDTO("Ana Atualizada", null, null);
                String jsonBody = objectMapper.writeValueAsString(dtoAtualizacao);

                UserDetailDTO detailDTO = new UserDetailDTO(id, "Ana Atualizada", "ana@clinica.com", "11999999999");

                when(receptionistService.updateReceptionist(eq(id), any(UserUpdateDTO.class))).thenReturn(detailDTO);

                // Quando / Então
                // Repare que aqui estamos usando put() em vez de patch(), respeitando o seu Controller
                mockMvc.perform(put("/receptionists/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nome").value("Ana Atualizada"));

                verify(receptionistService, times(1)).updateReceptionist(eq(id), any(UserUpdateDTO.class));
            }
        }
    }

    @DisplayName("Quando deletar recepcionista (DELETE /receptionists/{id})")
    @Nested
    class Deletar {

        @DisplayName("Então deve retornar Status 204 No Content")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                doNothing().when(receptionistService).deleteReceptionist(id);

                // Quando / Então
                mockMvc.perform(delete("/receptionists/{id}", id))
                        .andExpect(status().isNoContent());

                verify(receptionistService, times(1)).deleteReceptionist(id);
            }
        }
    }
}