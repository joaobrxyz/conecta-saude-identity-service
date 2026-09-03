package com.example.identity_service.service;

import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceptionistService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerReceptionist(UserRegisterDTO data) {
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
        }

        String senhaCripto = passwordEncoder.encode(data.senha());
        User user = new User(data, TipoUser.RECEPCAO, senhaCripto);
        userRepository.save(user);
    }
}
