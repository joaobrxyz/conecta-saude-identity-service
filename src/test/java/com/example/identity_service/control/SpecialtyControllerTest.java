package com.example.identity_service.control;

import com.example.identity_service.dto.details.SpecialtyDetailDTO;
import com.example.identity_service.dto.register.SpecialtyRegisterDTO;
import com.example.identity_service.service.SpecialtyService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SpecialtyControllerTest {

    @InjectMocks
    SpecialtyController specialtyController;

    @Mock
    SpecialtyService specialtyService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Setup standalone ultra-rápido
        mockMvc = MockMvcBuilders.standaloneSetup(specialtyController).build();
    }

    @DisplayName("Quando registrar uma especialidade (POST /specialties)")
    @Nested
    class Registrar {

        @DisplayName("Então deve retornar Status 201 Created com a especialidade criada")
        @Nested
        class Sucesso {

            @Test
            void teste1() throws Exception {
                // Dado
                var dto = new SpecialtyRegisterDTO("Cardiologia");
                String jsonBody = objectMapper.writeValueAsString(dto);

                // Assumindo que o SpecialtyDetailDTO tenha id (Long) e nome (String)
                var detailDTO = new SpecialtyDetailDTO(1L, "Cardiologia");

                when(specialtyService.registerSpecialty(any(SpecialtyRegisterDTO.class))).thenReturn(detailDTO);

                // Quando / Então
                mockMvc.perform(post("/specialties")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                        .andExpect(status().isCreated())
                        // Valida se o Controller repassou o body gerado pelo Service corretamente para o JSON
                        .andExpect(jsonPath("$.id").value(1L))
                        .andExpect(jsonPath("$.nome").value("Cardiologia"));

                verify(specialtyService, times(1)).registerSpecialty(any(SpecialtyRegisterDTO.class));
            }
        }
    }
}