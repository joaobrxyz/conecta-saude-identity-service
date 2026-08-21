package com.example.identity_service.service;

import com.example.identity_service.dto.PatientRegisterDTO;
import com.example.identity_service.model.Patient;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.PatientRepository;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerPatient(PatientRegisterDTO data) {
        var user = new User();
        user.setNome(data.nome());
        user.setEmail(data.email());
        user.setSenha(data.senha());
        user.setTelefone(data.telefone());
        user.setTipoUser(TipoUser.PACIENTE);
        user.setSenha(passwordEncoder.encode(data.senha()));
        userRepository.save(user);

        var patient = new Patient();
        patient.setCpf(data.cpf());
        patient.setUser(user);

        patientRepository.save(patient);
    }
}
