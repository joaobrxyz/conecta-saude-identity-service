package com.example.identity_service.service;

import com.example.identity_service.dto.details.PatientDetailDTO;
import com.example.identity_service.dto.register.PatientRegisterDTO;
import com.example.identity_service.dto.update.PatientUpdateDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.Patient;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.PatientRepository;
import com.example.identity_service.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @InjectMocks
    PatientService patientService;

    @Mock
    PatientRepository patientRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    // --- MÉTODOS AUXILIARES ---
    private User criarUserValido() {
        User user = new User();
        setField(user, "id", UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884"));
        user.setNome("João Paciente");
        user.setEmail("joao@clinica.com");
        user.setTelefone("11999999999");
        user.setTipoUser(TipoUser.PACIENTE);
        user.setAtivo(true);
        return user;
    }

    private Patient criarPatientValido() {
        Patient patient = new Patient(criarUserValido(), "12345678900");
        setField(patient, "id", UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111"));
        return patient;
    }

    private PatientRegisterDTO criarRegisterDTO() {
        // Adapte os parâmetros caso a ordem do seu record seja diferente
        return new PatientRegisterDTO("João Paciente", "joao@clinica.com", "senha123", "11999999999", "12345678900");
    }

    @DisplayName("Quando registrar um paciente")
    @Nested
    class Registrar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @BeforeEach
            void beforeEach() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
                when(patientRepository.existsByCpf(anyString())).thenReturn(false);
                when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
            }

            @DisplayName("Dado um DTO de registro com E-mail e CPF livres")
            @Test
            void teste1() {
                // Dado
                var dto = criarRegisterDTO();

                // Quando
                patientService.registerPatient(dto);

                // Então
                verify(userRepository, times(1)).save(any(User.class));
                verify(patientRepository, times(1)).save(any(Patient.class));
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um e-mail que já está cadastrado")
            @Test
            void teste1() {
                // Dado
                var dto = criarRegisterDTO();
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(new User()));

                // Quando / Então
                assertThatThrownBy(() -> patientService.registerPatient(dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("E-mail já cadastrado no sistema.");

                verify(patientRepository, never()).existsByCpf(anyString());
                verify(userRepository, never()).save(any(User.class));
            }

            @DisplayName("Dado um E-mail livre, mas um CPF já cadastrado")
            @Test
            void teste2() {
                // Dado
                var dto = criarRegisterDTO();
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(patientRepository.existsByCpf(dto.cpf())).thenReturn(true);

                // Quando / Então
                assertThatThrownBy(() -> patientService.registerPatient(dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("CPF já cadastrado no sistema.");

                verify(userRepository, never()).save(any(User.class));
            }
        }
    }

    @DisplayName("Quando listar pacientes")
    @Nested
    class Listar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado uma requisição paginada")
            @Test
            void teste1() {
                // Dado
                Pageable pageable = PageRequest.of(0, 10);
                Patient patient = criarPatientValido();
                Page<Patient> paginaMocada = new PageImpl<>(List.of(patient));

                when(patientRepository.findAllByUserAtivoTrue(pageable)).thenReturn(paginaMocada);

                // Quando
                Page<PatientDetailDTO> atual = patientService.listPatients(pageable);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.getContent()).hasSize(1);
                assertThat(atual.getContent().get(0).nome()).isEqualTo("João Paciente");
            }
        }
    }

    @DisplayName("Quando buscar um paciente por ID")
    @Nested
    class BuscarPorId {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um ID existente")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Patient patient = criarPatientValido();
                when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

                // Quando
                PatientDetailDTO atual = patientService.getPatientById(id);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.nome()).isEqualTo("João Paciente");
                assertThat(atual.cpf()).isEqualTo("12345678900");
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um ID que não existe")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.randomUUID();
                when(patientRepository.findById(id)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> patientService.getPatientById(id))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Usuário não encontrado.");
            }
        }
    }

    @DisplayName("Quando atualizar um paciente")
    @Nested
    class Atualizar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um DTO com um novo e-mail livre e novos dados")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Patient patientBanco = criarPatientValido();
                var dto = new PatientUpdateDTO("João Atualizado", "novo@clinica.com", "11888888888");

                when(patientRepository.findById(id)).thenReturn(Optional.of(patientBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty()); // E-mail livre

                // Quando
                PatientDetailDTO atual = patientService.updatePatient(id, dto);

                // Então
                // O Assert garante que os dados da entidade foram alterados em memória (o JPA cuida do save no Transactional)
                assertThat(atual.nome()).isEqualTo("João Atualizado");
                assertThat(atual.email()).isEqualTo("novo@clinica.com");
                assertThat(atual.telefone()).isEqualTo("11888888888");
            }

            @DisplayName("Dado um DTO que mantém o mesmo e-mail do próprio paciente")
            @Test
            void teste2() {
                // Dado
                UUID id = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Patient patientBanco = criarPatientValido(); // User ID: b34f8434...
                var dto = new PatientUpdateDTO(null, "joao@clinica.com", null);

                when(patientRepository.findById(id)).thenReturn(Optional.of(patientBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(patientBanco.getUser()));

                // Quando
                PatientDetailDTO atual = patientService.updatePatient(id, dto);

                // Então
                assertThat(atual.email()).isEqualTo("joao@clinica.com");
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um ID que não existe")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.randomUUID();
                var dto = new PatientUpdateDTO("Nome", "email@teste.com", "119999");
                when(patientRepository.findById(id)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> patientService.updatePatient(id, dto))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Paciente não encontrado.");
            }

            @DisplayName("Dado um novo e-mail que já pertence a outro usuário")
            @Test
            void teste2() {
                // Dado
                UUID idPacientePrincipal = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Patient patientBanco = criarPatientValido();

                // Simulando outro usuário no banco dono do e-mail
                User outroUser = criarUserValido();
                setField(outroUser, "id", UUID.fromString("a99f8434-9999-4a3e-aa51-bff7ce7dd999"));

                var dto = new PatientUpdateDTO(null, "email.ocupado@clinica.com", null);

                when(patientRepository.findById(idPacientePrincipal)).thenReturn(Optional.of(patientBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(outroUser));

                // Quando / Então
                assertThatThrownBy(() -> patientService.updatePatient(idPacientePrincipal, dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("Este e-mail já está em uso por outra conta.");
            }
        }
    }

    @DisplayName("Quando deletar (inativar) um paciente")
    @Nested
    class Deletar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um ID válido")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Patient patient = criarPatientValido();
                when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

                // Quando
                patientService.deletePatient(id);

                // Então
                assertThat(patient.getUser().isEnabled()).isFalse();
                verify(userRepository, times(1)).save(patient.getUser());
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @DisplayName("Dado um ID que não existe")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.randomUUID();
                when(patientRepository.findById(id)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> patientService.deletePatient(id))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Paciente não encontrado.");
            }
        }
    }
}