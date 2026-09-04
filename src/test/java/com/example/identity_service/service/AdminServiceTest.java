package com.example.identity_service.service;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    AdminService adminService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    // --- MÉTODOS AUXILIARES ---
    private User criarAdminValido() {
        User user = new User();
        setField(user, "id", UUID.fromString("a11f8434-5dfb-4a3e-aa51-bff7ce7dd111"));
        user.setNome("Admin Supremo");
        user.setEmail("admin@clinica.com");
        user.setTelefone("11999999999");
        user.setTipoUser(TipoUser.ADMIN);
        user.setAtivo(true);
        return user;
    }

    private UserRegisterDTO criarRegisterDTO() {
        return new UserRegisterDTO("Admin Supremo", "admin@clinica.com", "senhaForte", "11999999999");
    }

    @DisplayName("Quando registrar um administrador")
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

            @DisplayName("Dado um DTO de registro com e-mail livre")
            @Test
            void teste1() {
                // Dado
                var dto = criarRegisterDTO();

                // Quando
                adminService.registerAdmin(dto);

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
                assertThatThrownBy(() -> adminService.registerAdmin(dto))
                        .isInstanceOf(RuntimeException.class) // Refletindo o seu código atual
                        .hasMessage("E-mail já cadastrado no sistema.");

                verify(userRepository, never()).save(any(User.class));
            }
        }
    }

    @DisplayName("Quando listar administradores")
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
                User admin = criarAdminValido();
                Page<User> paginaMocada = new PageImpl<>(List.of(admin));

                when(userRepository.findAllByTipoUserAndAtivoTrue(TipoUser.ADMIN, pageable))
                        .thenReturn(paginaMocada);

                // Quando
                Page<UserDetailDTO> atual = adminService.listAdmins(pageable);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.getContent()).hasSize(1);
                assertThat(atual.getContent().get(0).nome()).isEqualTo("Admin Supremo");
            }
        }
    }

    @DisplayName("Quando buscar um administrador por ID")
    @Nested
    class BuscarPorId {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um ID existente")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("a11f8434-5dfb-4a3e-aa51-bff7ce7dd111");
                User admin = criarAdminValido();
                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.of(admin));

                // Quando
                UserDetailDTO atual = adminService.getAdminById(id);

                // Então
                assertThat(atual).isNotNull();
                assertThat(atual.nome()).isEqualTo("Admin Supremo");
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
                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> adminService.getAdminById(id))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Administrador não encontrado.");
            }
        }
    }

    @DisplayName("Quando atualizar um administrador")
    @Nested
    class Atualizar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um DTO com um novo e-mail livre e novos dados")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("a11f8434-5dfb-4a3e-aa51-bff7ce7dd111");
                User adminBanco = criarAdminValido();
                var dto = new UserUpdateDTO("Admin Novo", "novo@clinica.com", "11888888888");

                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.of(adminBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

                // Quando
                UserDetailDTO atual = adminService.updateAdmin(id, dto);

                // Então
                assertThat(atual.nome()).isEqualTo("Admin Novo");
                assertThat(atual.email()).isEqualTo("novo@clinica.com");
                verify(userRepository, times(1)).save(adminBanco);
            }

            @DisplayName("Dado um DTO que mantém o mesmo e-mail do próprio administrador")
            @Test
            void teste2() {
                // Dado
                UUID id = UUID.fromString("a11f8434-5dfb-4a3e-aa51-bff7ce7dd111");
                User adminBanco = criarAdminValido(); // Email original: admin@clinica.com
                var dto = new UserUpdateDTO(null, "admin@clinica.com", null);

                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.of(adminBanco));

                // O findByEmail nem deve ser chamado pois a validação do IF (!admin.getEmail().equals(data.email())) vai dar falso

                // Quando
                UserDetailDTO atual = adminService.updateAdmin(id, dto);

                // Então
                assertThat(atual.email()).isEqualTo("admin@clinica.com");
                verify(userRepository, times(1)).save(adminBanco);
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
                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> adminService.updateAdmin(id, dto))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Administrador não encontrado.");
            }

            @DisplayName("Dado um novo e-mail que já pertence a outro usuário")
            @Test
            void teste2() {
                // Dado
                UUID idPrincipal = UUID.fromString("a11f8434-5dfb-4a3e-aa51-bff7ce7dd111");
                User adminBanco = criarAdminValido();

                var dto = new UserUpdateDTO(null, "email.ocupado@clinica.com", null);

                when(userRepository.findByIdAndTipoUser(idPrincipal, TipoUser.ADMIN)).thenReturn(Optional.of(adminBanco));
                when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(new User())); // Retorna outro usuário

                // Quando / Então
                assertThatThrownBy(() -> adminService.updateAdmin(idPrincipal, dto))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("E-mail já cadastrado no sistema.");

                verify(userRepository, never()).save(any(User.class));
            }
        }
    }

    @DisplayName("Quando deletar (inativar) um administrador")
    @Nested
    class Deletar {

        @DisplayName("Então deve executar com sucesso")
        @Nested
        class Sucesso {

            @DisplayName("Dado um ID válido")
            @Test
            void teste1() {
                // Dado
                UUID id = UUID.fromString("a11f8434-5dfb-4a3e-aa51-bff7ce7dd111");
                User admin = criarAdminValido();
                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.of(admin));

                // Quando
                adminService.deleteAdmin(id);

                // Então
                assertThat(admin.isEnabled()).isFalse();
                verify(userRepository, times(1)).save(admin);
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
                when(userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)).thenReturn(Optional.empty());

                // Quando / Então
                assertThatThrownBy(() -> adminService.deleteAdmin(id))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Administrador não encontrado.");
            }
        }
    }
}