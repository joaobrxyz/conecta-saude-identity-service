package com.example.identity_service.service;

import com.example.identity_service.dto.register.SpecialtyRegisterDTO;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.Specialty;
import com.example.identity_service.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @InjectMocks
    SpecialtyService specialtyService;

    @Mock
    SpecialtyRepository specialtyRepository;

    @DisplayName("Quando registrar uma especialidade")
    @Nested
    class Registrar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @BeforeEach
            void beforeEach() {
                // Simula que a especialidade não existe no banco
                when(specialtyRepository.existsByNome(any()))
                        .thenReturn(false);

                // Simula o JPA salvando a entidade e gerando o ID
                when(specialtyRepository.save(any()))
                        .thenAnswer(invocationOnMock -> {
                            Specialty specialty = invocationOnMock.getArgument(0);

                            // Define um ID falso gerado pelo banco.
                            // Obs: Se o seu ID de Specialty for UUID em vez de Long,
                            // basta trocar o 1L por UUID.randomUUID()
                            setField(specialty, "id", 1L);

                            return specialty;
                        });
            }

            @DisplayName("Dado um DTO com dados válidos")
            @Test
            void teste1() {
                // Dado
                var dto = new SpecialtyRegisterDTO("Cardiologia");

                // Quando
                var atual = specialtyService.registerSpecialty(dto);

                // Então
                assertThat(atual)
                        .isNotNull();
                assertThat(atual.id())
                        .isNotNull()
                        .isEqualTo(1L);
                assertThat(atual.nome())
                        .isEqualTo("Cardiologia");
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @BeforeEach
            void beforeEach() {
                // Simula que a especialidade já existe no banco
                when(specialtyRepository.existsByNome(any()))
                        .thenReturn(true);
            }

            @DisplayName("Dado um nome de especialidade que já existe no banco")
            @Test
            void teste1() {
                // Dado
                var dto = new SpecialtyRegisterDTO("Cardiologia");

                // Quando / Então
                assertThatThrownBy(() -> specialtyService.registerSpecialty(dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("Já existe uma especialidade cadastrada com este nome.");
            }
        }
    }
}