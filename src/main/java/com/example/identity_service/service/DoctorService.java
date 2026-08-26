package com.example.identity_service.service;

import com.example.identity_service.dto.DoctorRegisterDTO;
import com.example.identity_service.model.Doctor;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.DoctorRepository;
import com.example.identity_service.repository.SpecialtyRepository;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;

@Service
public class DoctorService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerDoctor(DoctorRegisterDTO data) {
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado no sistema.");
        }

        User newUser = new User();
        newUser.setNome(data.nome());
        newUser.setEmail(data.email());
        newUser.setSenha(passwordEncoder.encode(data.senha()));
        newUser.setTipoUser(TipoUser.MEDICO);

        userRepository.save(newUser);

        Doctor newDoctor = new Doctor();
        newDoctor.setUser(newUser);
        newDoctor.setCrm(data.crm());
        newDoctor.setSpecialty(
                specialtyRepository.findById(data.especialidadeId())
                .orElseThrow(() -> new RuntimeException("Especialidade não encontrada")));

        doctorRepository.save(newDoctor);
    }
}
