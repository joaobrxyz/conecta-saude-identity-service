package com.example.identity_service.control;

import com.example.identity_service.dto.details.PatientDetailDTO;
import com.example.identity_service.dto.register.PatientRegisterDTO;
import com.example.identity_service.dto.update.PatientUpdateDTO;
import com.example.identity_service.service.PatientService;
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
import org.springframework.security.core.Authentication;
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
class PatientControllerTest {

    @InjectMocks
    PatientController patientController;

    @Mock
    PatientService patientService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(patientController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private PatientDetailDTO criarDetailDTO(UUID id) {
        return new PatientDetailDTO(
                id,
                "João Paciente",
                "joao@clinica.com",
                "54642196099",
                "11999999999"
        );
    }

    @DisplayName("Quando registrar um paciente (POST /patients)")
    @Nested
    class Registrar {

        @DisplayName("Então deve retornar Status 201 Created")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new PatientRegisterDTO("João Paciente", "joao@clinica.com", "senhaForte123", "54642196099", "12345678900");
                String jsonBody = objectMapper.writeValueAsString(dto);

                doNothing().when(patientService).registerPatient(any(PatientRegisterDTO.class));

                // Quando / Então
                mockMvc.perform(post("/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isCreated());

                verify(patientService, times(1)).registerPatient(any(PatientRegisterDTO.class));
            }
        }
    }

    @DisplayName("Quando listar pacientes (GET /patients)")
    @Nested
    class Listar {

        @DisplayName("Então deve retornar Status 200 OK com a Página")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                PatientDetailDTO detailDTO = criarDetailDTO(id);
                Pageable pageable = PageRequest.of(0, 10, Sort.by("user.nome"));
                Page<PatientDetailDTO> paginaMocada = new PageImpl<>(List.of(detailDTO), pageable, 1);

                when(patientService.listPatients(any(Pageable.class))).thenReturn(paginaMocada);

                // Quando / Então
                mockMvc.perform(get("/patients")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "user.nome,asc")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].nome").value("João Paciente"));
            }
        }
    }

    @DisplayName("Quando buscar paciente por ID (GET /patients/{id})")
    @Nested
    class BuscarPorId {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                PatientDetailDTO detailDTO = criarDetailDTO(id);

                when(patientService.getPatientById(id)).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(get("/patients/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id.toString()))
                        .andExpect(jsonPath("$.nome").value("João Paciente"));
            }
        }
    }

    @DisplayName("Quando buscar o próprio perfil (GET /patients/profile)")
    @Nested
    class BuscarMeuPerfil {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID userIdLogado = UUID.randomUUID();
                PatientDetailDTO detailDTO = criarDetailDTO(userIdLogado);

                // Mockando a autenticação do Spring Security
                Authentication authenticationMock = mock(Authentication.class);
                when(authenticationMock.getName()).thenReturn(userIdLogado.toString());

                when(patientService.getPatientById(userIdLogado)).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(get("/patients/profile")
                                .principal(authenticationMock) // Injetando o mock do usuário logado
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(userIdLogado.toString()))
                        .andExpect(jsonPath("$.nome").value("João Paciente"));
            }
        }
    }

    @DisplayName("Quando atualizar paciente (PATCH /patients/{id})")
    @Nested
    class Atualizar {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                var dtoAtualizacao = new PatientUpdateDTO("João Atualizado", null, null);
                String jsonBody = objectMapper.writeValueAsString(dtoAtualizacao);

                PatientDetailDTO detailDTO = criarDetailDTO(id);
                when(patientService.updatePatient(eq(id), any(PatientUpdateDTO.class))).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(patch("/patients/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk());

                verify(patientService, times(1)).updatePatient(eq(id), any(PatientUpdateDTO.class));
            }
        }
    }

    @DisplayName("Quando atualizar o próprio perfil (PATCH /patients/profile)")
    @Nested
    class AtualizarMeuPerfil {

        @DisplayName("Então deve retornar Status 200 OK")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID userIdLogado = UUID.randomUUID();
                var dtoAtualizacao = new PatientUpdateDTO("João Atualizado", null, null);
                String jsonBody = objectMapper.writeValueAsString(dtoAtualizacao);

                Authentication authenticationMock = mock(Authentication.class);
                when(authenticationMock.getName()).thenReturn(userIdLogado.toString());

                PatientDetailDTO detailDTO = criarDetailDTO(userIdLogado);
                when(patientService.updatePatient(eq(userIdLogado), any(PatientUpdateDTO.class))).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(patch("/patients/profile")
                                .principal(authenticationMock)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isOk());

                verify(patientService, times(1)).updatePatient(eq(userIdLogado), any(PatientUpdateDTO.class));
            }
        }
    }

    @DisplayName("Quando deletar paciente (DELETE /patients/{id})")
    @Nested
    class Deletar {

        @DisplayName("Então deve retornar Status 204 No Content")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID id = UUID.randomUUID();
                doNothing().when(patientService).deletePatient(id);

                // Quando / Então
                mockMvc.perform(delete("/patients/{id}", id))
                        .andExpect(status().isNoContent());

                verify(patientService, times(1)).deletePatient(id);
            }
        }
    }

    @DisplayName("Quando deletar o próprio perfil (DELETE /patients/profile)")
    @Nested
    class DeletarMeuPerfil {

        @DisplayName("Então deve retornar Status 204 No Content")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                UUID userIdLogado = UUID.randomUUID();

                Authentication authenticationMock = mock(Authentication.class);
                when(authenticationMock.getName()).thenReturn(userIdLogado.toString());

                doNothing().when(patientService).deletePatient(userIdLogado);

                // Quando / Então
                mockMvc.perform(delete("/patients/profile")
                                .principal(authenticationMock))
                        .andExpect(status().isNoContent());

                verify(patientService, times(1)).deletePatient(userIdLogado);
            }
        }
    }
}