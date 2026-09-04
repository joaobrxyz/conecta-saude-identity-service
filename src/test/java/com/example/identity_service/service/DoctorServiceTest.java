package com.example.identity_service.service;

import com.example.identity_service.dto.details.DoctorDetailDTO;
import com.example.identity_service.dto.register.DoctorRegisterDTO;
import com.example.identity_service.dto.update.DoctorUpdateDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.Doctor;
import com.example.identity_service.model.Specialty;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.DoctorRepository;
import com.example.identity_service.repository.SpecialtyRepository;
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
class DoctorServiceTest {

    @InjectMocks
    DoctorService doctorService;

    @Mock
    UserRepository userRepository;

    @Mock
    DoctorRepository doctorRepository;

    @Mock
    SpecialtyRepository specialtyRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    // --- MÉTODOS AUXILIARES ---
    private User criarUserValido() {
        User user = new User();
        setField(user, "id", UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884"));
        user.setNome("Dr. Roberto");
        user.setEmail("roberto@clinica.com");
        user.setTelefone("11999999999");
        user.setTipoUser(TipoUser.MEDICO);
        user.setAtivo(true);
        return user;
    }

    private Specialty criarSpecialtyValida() {
        Specialty specialty = new Specialty();
        setField(specialty, "id", 1L);
        specialty.setNome("Cardiologia");
        return specialty;
    }

    private Doctor criarDoctorValido() {
        Doctor doctor = new Doctor(criarUserValido(), "123456-SP", criarSpecialtyValida());
        setField(doctor, "id", UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111"));
        return doctor;
    }

    private DoctorRegisterDTO criarRegisterDTO() {
        // Adaptar a ordem dos parâmetros conforme o seu record
        return new DoctorRegisterDTO("Dr. Roberto", "roberto@clinica.com", "senha123", "11999999999", "123456-SP", 1L);
    }

    @DisplayName("Quando registrar um médico")
    @Nested
    class Registrar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @BeforeEach
            void beforeEach() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
                when(doctorRepository.existsByCrm(anyString())).thenReturn(false);
                when(passwordEncoder.encode(anyString())).thenReturn("senhaCripto");
                when(specialtyRepository.findById(anyLong())).thenReturn(Optional.of(criarSpecialtyValida()));
            }

            @DisplayName("Dado um DTO válido, com e-mail e CRM livres e especialidade existente")
            @Test
            void teste1() {
                // Dado
                var dto = criarRegisterDTO();

                // Quando
                doctorService.registerDoctor(dto);

                // Então
                verify(userRepository, times(1)).save(any(User.class));
                verify(doctorRepository, times(1)).save(any(Doctor.class));
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
                assertThatThrownBy(() -> doctorService.registerDoctor(dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("E-mail já cadastrado no sistema.");

                verify(doctorRepository, never()).existsByCrm(anyString());
            }

            @DisplayName("Dado um CRM que já está cadastrado")
            @Test
            void teste2() {
                // Dado
                var dto = criarRegisterDTO();
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(doctorRepository.existsByCrm(dto.crm())).thenReturn(true);

                // Quando / Então
                assertThatThrownBy(() -> doctorService.registerDoctor(dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("CRM já cadastrado no sistema.");
            }

            @DisplayName("Dado uma especialidade que não existe")
            @Test
            void teste3() {
                // Dado
                var dto = criarRegisterDTO();
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
                when(doctorRepository.existsByCrm(dto.crm())).thenReturn(false);
                when(passwordEncoder.encode(anyString())).thenReturn("senhaCripto");
                when(specialtyRepository.findById(dto.especialidadeId())).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> doctorService.registerDoctor(dto))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Especialidade não encontrada");

                verify(doctorRepository, never()).save(any(Doctor.class));
            }
        }
    }

    @DisplayName("Quando listar médicos")
    @Nested
    class Listar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado uma requisição com filtro de especialidade")
            @Test
            void teste1() {
                // Dado
                Long specialtyId = 1L;
                Pageable pageable = PageRequest.of(0, 10);
                Page<Doctor> paginaMocada = new PageImpl<>(List.of(criarDoctorValido()));

                when(doctorRepository.findAllBySpecialtyIdAndUserAtivoTrue(specialtyId, pageable))
                        .thenReturn(paginaMocada);

                // Quando
                Page<DoctorDetailDTO> atual = doctorService.listDoctors(specialtyId, pageable);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.getContent()).hasSize(1);
                verify(doctorRepository, times(1)).findAllBySpecialtyIdAndUserAtivoTrue(specialtyId, pageable);
                verify(doctorRepository, never()).findAllByUserAtivoTrue(any(Pageable.class));
            }

            @DisplayName("Dado uma requisição sem filtro de especialidade (null)")
            @Test
            void teste2() {
                // Dado
                Long specialtyId = null;
                Pageable pageable = PageRequest.of(0, 10);
                Page<Doctor> paginaMocada = new PageImpl<>(List.of(criarDoctorValido()));

                when(doctorRepository.findAllByUserAtivoTrue(pageable)).thenReturn(paginaMocada);

                // Quando
                Page<DoctorDetailDTO> atual = doctorService.listDoctors(specialtyId, pageable);

                // Então
                assertThat(atual).isNotNull();
                verify(doctorRepository, times(1)).findAllByUserAtivoTrue(pageable);
            }
        }
    }

    @DisplayName("Quando buscar um médico por ID")
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
                Doctor doctor = criarDoctorValido();
                when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

                // Quando
                DoctorDetailDTO atual = doctorService.getDoctorById(id);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.nome()).isEqualTo("Dr. Roberto");
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
                when(doctorRepository.findById(id)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> doctorService.getDoctorById(id))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Médico não encontrado");
            }
        }
    }

    @DisplayName("Quando atualizar um médico")
    @Nested
    class Atualizar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um DTO com novo e-mail livre e novos dados")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Doctor doctorBanco = criarDoctorValido();
                var dto = new DoctorUpdateDTO("Roberto Atualizado", "novo@clinica.com", "11888888888");

                when(doctorRepository.findById(id)).thenReturn(Optional.of(doctorBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

                // Quando
                DoctorDetailDTO atual = doctorService.updateDoctor(id, dto);

                // Então
                assertThat(atual.nome()).isEqualTo("Roberto Atualizado");
                assertThat(atual.email()).isEqualTo("novo@clinica.com");
            }

            @DisplayName("Dado um DTO que mantém o mesmo e-mail do próprio médico")
            @Test
            void teste2() {
                // Dado
                UUID id = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Doctor doctorBanco = criarDoctorValido(); // Email original: roberto@clinica.com
                var dto = new DoctorUpdateDTO(null, "roberto@clinica.com", null);

                when(doctorRepository.findById(id)).thenReturn(Optional.of(doctorBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(doctorBanco.getUser()));

                // Quando
                DoctorDetailDTO atual = doctorService.updateDoctor(id, dto);

                // Então
                assertThat(atual.email()).isEqualTo("roberto@clinica.com");
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
                var dto = new DoctorUpdateDTO("Nome", "email@teste.com", "119999");
                when(doctorRepository.findById(id)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> doctorService.updateDoctor(id, dto))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Médico não encontrado");
            }

            @DisplayName("Dado um novo e-mail que já pertence a outro usuário")
            @Test
            void teste2() {
                // Dado
                UUID idMedicoPrincipal = UUID.fromString("c11f8434-1111-4a3e-aa51-bff7ce7dd111");
                Doctor doctorBanco = criarDoctorValido();

                User outroUser = criarUserValido();
                setField(outroUser, "id", UUID.fromString("a99f8434-9999-4a3e-aa51-bff7ce7dd999"));

                var dto = new DoctorUpdateDTO(null, "email.ocupado@clinica.com", null);

                when(doctorRepository.findById(idMedicoPrincipal)).thenReturn(Optional.of(doctorBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(outroUser));

                // Quando / Então
                assertThatThrownBy(() -> doctorService.updateDoctor(idMedicoPrincipal, dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("E-mail já cadastrado no sistema.");
            }
        }
    }

    @DisplayName("Quando deletar (inativar) um médico")
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
                Doctor doctor = criarDoctorValido();
                when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

                // Quando
                doctorService.deleteDoctor(id);

                // Então
                assertThat(doctor.getUser().isEnabled()).isFalse(); // Verifica inativação
                verify(userRepository, times(1)).save(doctor.getUser());
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
                when(doctorRepository.findById(id)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> doctorService.deleteDoctor(id))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Médico não encontrado");
            }
        }
    }
}