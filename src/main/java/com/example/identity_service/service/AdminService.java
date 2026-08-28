package com.example.identity_service.service;

import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
