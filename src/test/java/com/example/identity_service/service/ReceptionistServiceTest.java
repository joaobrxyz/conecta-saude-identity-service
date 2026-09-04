package com.example.identity_service.service;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ReceptionistServiceTest {

    @InjectMocks
    ReceptionistService receptionistService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    // Métodos auxiliares para criar dados falsos reutilizáveis
    private User criarUsuarioValido() {
        User user = new User();
        setField(user, "id", UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884"));
        user.setNome("Ana Silva");
        user.setEmail("ana@clinica.com");
        user.setTelefone("11999999999");
        user.setTipoUser(TipoUser.RECEPCAO);
        user.setAtivo(true);
        return user;
    }

    private UserRegisterDTO criarRegisterDTO() {
        // Adaptar os parâmetros de acordo com o seu record UserRegisterDTO
        return new UserRegisterDTO("Ana Silva", "ana@clinica.com", "senha123", "11999999999");
    }

    @DisplayName("Quando registrar uma recepcionista")
    @Nested
    class Registrar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @BeforeEach
            void beforeEach() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
            }

            @DisplayName("Dado um DTO de registro válido")
            @Test
            void teste1() {
                // Dado
                var dto = criarRegisterDTO();

                // Quando
                receptionistService.registerReceptionist(dto);

                // Então
                verify(userRepository, times(1)).save(any(User.class));
            }
        }

        @DisplayName("Então deve lançar erro de validação")
        @Nested
        class Falha {

            @BeforeEach
            void beforeEach() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
            }

            @DisplayName("Dado um e-mail que já está cadastrado")
            @Test
            void teste1() {
                // Dado
                var dto = criarRegisterDTO();

                // Quando / Então
                assertThatThrownBy(() -> receptionistService.registerReceptionist(dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("E-mail já cadastrado no sistema.");

                verify(userRepository, never()).save(any(User.class));
            }
        }
    }

    @DisplayName("Quando listar recepcionistas")
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
                User user = criarUsuarioValido();
                Page<User> paginaMocada = new PageImpl<>(List.of(user));

                when(userRepository.findAllByTipoUserAndAtivoTrue(TipoUser.RECEPCAO, pageable))
                        .thenReturn(paginaMocada);

                // Quando
                Page<UserDetailDTO> atual = receptionistService.listReceptionists(pageable);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.getContent()).hasSize(1);
                assertThat(atual.getContent().get(0).nome()).isEqualTo("Ana Silva");
            }
        }
    }

    @DisplayName("Quando buscar uma recepcionista por ID")
    @Nested
    class BuscarPorId {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um ID existente")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884");
                User user = criarUsuarioValido();
                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.of(user));

                // Quando
                UserDetailDTO atual = receptionistService.getReceptionistById(id);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.nome()).isEqualTo("Ana Silva");
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
                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> receptionistService.getReceptionistById(id))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Recepcionista não encontrado.");
            }
        }
    }

    @DisplayName("Quando atualizar uma recepcionista")
    @Nested
    class Atualizar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um DTO com um novo e-mail livre e novos dados")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884");
                User userBanco = criarUsuarioValido();
                var dto = new UserUpdateDTO("Ana Atualizada", "novo@clinica.com", "11888888888");

                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.of(userBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty()); // E-mail livre

                // Quando
                UserDetailDTO atual = receptionistService.updateReceptionist(id, dto);

                // Então
                assertThat(atual.nome()).isEqualTo("Ana Atualizada");
                assertThat(atual.email()).isEqualTo("novo@clinica.com");
                verify(userRepository, times(1)).save(userBanco);
            }

            @DisplayName("Dado um DTO que mantém o mesmo e-mail do próprio usuário")
            @Test
            void teste2() {
                // Dado
                UUID id = UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884");
                User userBanco = criarUsuarioValido(); // Email original: ana@clinica.com
                var dto = new UserUpdateDTO(null, "ana@clinica.com", null); // Só enviou o e-mail igual

                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.of(userBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(userBanco)); // Retorna ele mesmo

                // Quando
                UserDetailDTO atual = receptionistService.updateReceptionist(id, dto);

                // Então
                assertThat(atual.email()).isEqualTo("ana@clinica.com");
                verify(userRepository, times(1)).save(userBanco);
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
                var dto = new UserUpdateDTO("Nome", "email@teste.com", "119999");
                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> receptionistService.updateReceptionist(id, dto))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Recepcionista não encontrado.");
            }

            @DisplayName("Dado um novo e-mail que já pertence a outro usuário")
            @Test
            void teste2() {
                // Dado
                UUID idPrincipal = UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884");
                UUID idOutroUsuario = UUID.fromString("c99f8434-1111-4a3e-aa51-bff7ce7dd999");

                User userBanco = criarUsuarioValido();
                User outroUserNoBanco = criarUsuarioValido();
                setField(outroUserNoBanco, "id", idOutroUsuario); // ID diferente!

                var dto = new UserUpdateDTO("Ana", "email.ocupado@clinica.com", "119999");

                when(userRepository.findByIdAndTipoUser(idPrincipal, TipoUser.RECEPCAO)).thenReturn(Optional.of(userBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(outroUserNoBanco));

                // Quando / Então
                assertThatThrownBy(() -> receptionistService.updateReceptionist(idPrincipal, dto))
                        .isInstanceOf(RegraDeNegocioException.class)
                        .hasMessage("E-mail já cadastrado no sistema.");

                verify(userRepository, never()).save(any(User.class));
            }
        }
    }

    @DisplayName("Quando deletar (inativar) uma recepcionista")
    @Nested
    class Deletar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um ID válido")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("b34f8434-5dfb-4a3e-aa51-bff7ce7dd884");
                User user = criarUsuarioValido();
                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.of(user));

                // Quando
                receptionistService.deleteReceptionist(id);

                // Então
                assertThat(user.isEnabled()).isFalse(); // Verifica se o inativar() mudou o status
                verify(userRepository, times(1)).save(user);
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
                when(userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> receptionistService.deleteReceptionist(id))
                        .isInstanceOf(RecursoNaoEncontradoException.class)
                        .hasMessage("Recepcionista não encontrado.");
            }
        }
    }
}