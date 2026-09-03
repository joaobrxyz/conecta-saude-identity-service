package com.example.identity_service.service;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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

    public Page<UserDetailDTO> listReceptionists(Pageable pageable) {
        return userRepository.findAllByTipoUserAndAtivoTrue(TipoUser.RECEPCAO, pageable)
                .map(UserDetailDTO::new);
    }

    public UserDetailDTO getReceptionistById(UUID id) {
        User user = userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recepcionista não encontrado."));
        return new UserDetailDTO(user);
    }

    @Transactional
    public UserDetailDTO updateReceptionist(UUID id, UserUpdateDTO data) {
        User user = userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recepcionista não encontrado."));

        if (data.nome() != null && !data.nome().isBlank()) {
            user.setNome(data.nome());
        }
        if (data.telefone() != null && !data.telefone().isBlank()) {
            user.setTelefone(data.telefone());
        }
        if (data.email() != null && !data.email().isBlank()) {
            Optional<User> userExistente =  userRepository.findByEmail(data.email());
            if (userExistente.isPresent() && !userExistente.get().getId().equals(id)) {
                throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
            }
            user.setEmail(data.email());
        }

        userRepository.save(user);
        return new UserDetailDTO(user);
    }

    @Transactional
    public void deleteReceptionist(UUID id) {
        User user = userRepository.findByIdAndTipoUser(id, TipoUser.RECEPCAO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recepcionista não encontrado."));

        user.inativar();
        userRepository.save(user);
    }
}
