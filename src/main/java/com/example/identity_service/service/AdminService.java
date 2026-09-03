package com.example.identity_service.service;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerAdmin(UserRegisterDTO data) {
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado no sistema.");
        }

        String senhaCripto = passwordEncoder.encode(data.senha());
        User newAdmin = new User(data, TipoUser.ADMIN, senhaCripto);
        userRepository.save(newAdmin);
    }

    public Page<UserDetailDTO> listAdmins(Pageable pageable) {
        return userRepository.findAllByTipoUserAndAtivoTrue(TipoUser.ADMIN, pageable).map(UserDetailDTO::new);
    }

    public UserDetailDTO getAdminById(UUID id) {
        User admin = userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado."));
        return new UserDetailDTO(admin);
    }

    @Transactional
    public UserDetailDTO updateAdmin(UUID id, UserUpdateDTO data) {
        User admin = userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado."));

        if (data.nome() != null && !data.nome().isBlank()) {
            admin.setNome(data.nome());
        }
        if (data.telefone() != null && !data.telefone().isBlank()) {
            admin.setTelefone(data.telefone());
        }
        if (data.email() != null && !data.email().isBlank()) {
            if (!admin.getEmail().equals(data.email()) && userRepository.findByEmail(data.email()).isPresent()) {
                throw new RuntimeException("E-mail já cadastrado no sistema.");
            }
            admin.setEmail(data.email());
        }

        userRepository.save(admin);
        return new UserDetailDTO(admin);
    }

    @Transactional
    public void deleteAdmin(UUID id) {
        User admin = userRepository.findByIdAndTipoUser(id, TipoUser.ADMIN)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado."));
        admin.inativar();
        userRepository.save(admin);
    }
}
