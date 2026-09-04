package com.example.identity_service.control;

import com.example.identity_service.dto.details.DoctorDetailDTO;
import com.example.identity_service.dto.register.DoctorRegisterDTO;
import com.example.identity_service.dto.update.DoctorUpdateDTO;
import com.example.identity_service.service.DoctorService;
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
class DoctorControllerTest {

    @InjectMocks
    DoctorController doctorController;

    @Mock
    DoctorService doctorService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Setup super rápido e isolado com suporte ao Pageable para não quebrar no GET
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // --- MÉTODOS AUXILIARES ---
    // Ajuste a ordem dos parâmetros de acordo com o seu Record real do DoctorDetailDTO
    private DoctorDetailDTO criarDetailDTO(UUID id) {
        return new DoctorDetailDTO(
                id,
                "Dr. Roberto",    // nome
                "roberto@clinica.com",  // email
                "123456-SP",            // crm
                "Cardiologia",          // especialidade
                "11999999999"           // telefone
        );
    }

    @DisplayName("Quando registrar um médico (POST /doctors)")
    @Nested
    class Registrar {

        @DisplayName("Então deve retornar Status 201 Created")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new DoctorRegisterDTO("Dr. Roberto", "roberto@clinica.com", "senha123", "119999", "123456-SP", 1L);
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(doctorService).registerDoctor(any(DoctorRegisterDTO.class));

                // Quando / Então
                mockMvc.perform(post("/doctors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isCreated());

                verify(doctorService, times(1)).registerDoctor(any(DoctorRegisterDTO.class));
            }
        }
    }

    @DisplayName("Quando listar médicos (GET /doctors)")
    @Nested
    class Listar {

        @DisplayName("Então deve retornar Status 200 OK com a Página")
        @Nested
        class Sucesso {

            @DisplayName("Listagem COM filtro de especialidade")
            @Test
            void teste1ComFiltroEspecialidade() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                DoctorDetailDTO detailDTO = criarDetailDTO(id);
                // Sort adicionado para o Jackson não dar erro 500 ao tentar mapear a página
                Pageable pageable = PageRequest.of(0, 10, Sort.by("user.nome"));
                Page<DoctorDetailDTO> paginaMocada = new PageImpl<>(List.of(detailDTO), pageable, 1);

                when(doctorService.listDoctors(eq(1L), any(Pageable.class))).thenReturn(paginaMocada);

                // Quando / Então
                mockMvc.perform(get("/doctors")
                                .param("specialtyId", "1")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "user.nome,asc")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].nome").value("Dr. Roberto"))
                        .andExpect(jsonPath("$.content[0].crm").value("123456-SP"));
            }

            @DisplayName("Listagem SEM filtro de especialidade")
            @Test
            void teste2SemFiltroEspecialidade() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                DoctorDetailDTO detailDTO = criarDetailDTO(id);
                Pageable pageable = PageRequest.of(0, 10, Sort.by("user.nome"));
                Page<DoctorDetailDTO> paginaMocada = new PageImpl<>(List.of(detailDTO), pageable, 1);

                when(doctorService.listDoctors(isNull(), any(Pageable.class))).thenReturn(paginaMocada);

                // Quando / Então
                mockMvc.perform(get("/doctors")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].nome").value("Dr. Roberto"));
            }
        }
    }

    @DisplayName("Quando buscar médico por ID (GET /doctors/{id})")
    @Nested
    class BuscarPorId {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                DoctorDetailDTO detailDTO = criarDetailDTO(id);

                when(doctorService.getDoctorById(id)).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(get("/doctors/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id.toString()))
                        .andExpect(jsonPath("$.nome").value("Dr. Roberto"));
            }
        }
    }

    @DisplayName("Quando atualizar médico (PATCH /doctors/{id})")
    @Nested
    class Atualizar {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                var dtoAtualizacao = new DoctorUpdateDTO("Dr. Roberto Atualizado", null, null);
                String jsonBody = objectMapper.writeValueAsString(dtoAtualizacao);

                // A Controller de médicos no seu projeto retorna Void no Patch,
                // então ajustamos o mock do serviço para adequar à assinatura.
                DoctorDetailDTO detailDTO = criarDetailDTO(id);
                when(doctorService.updateDoctor(eq(id), any(DoctorUpdateDTO.class))).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(patch("/doctors/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk()); // Não esperamos corpo de volta, apenas Status 200

                verify(doctorService, times(1)).updateDoctor(eq(id), any(DoctorUpdateDTO.class));
            }
        }
    }

    @DisplayName("Quando deletar médico (DELETE /doctors/{id})")
    @Nested
    class Deletar {

        @DisplayName("Então deve retornar Status 204 No Content")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                doNothing().when(doctorService).deleteDoctor(id);

                // Quando / Então
                mockMvc.perform(delete("/doctors/{id}", id))
                        .andExpect(status().isNoContent());

                verify(doctorService, times(1)).deleteDoctor(id);
            }
        }
    }
}