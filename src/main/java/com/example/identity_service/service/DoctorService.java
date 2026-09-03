package com.example.identity_service.service;

import com.example.identity_service.dto.details.DoctorDetailDTO;
import com.example.identity_service.dto.register.DoctorRegisterDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.Doctor;
import com.example.identity_service.model.Specialty;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.DoctorRepository;
import com.example.identity_service.repository.SpecialtyRepository;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
        }

        if (doctorRepository.existsByCrm(data.crm())) {
            throw new RegraDeNegocioException("CRM já cadastrado no sistema.");
        }

        String senhaCripto = passwordEncoder.encode(data.senha());
        User newUser = new User(data, TipoUser.MEDICO, senhaCripto);
        userRepository.save(newUser);

        Specialty specialty = specialtyRepository.findById(data.especialidadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade não encontrada"));
        Doctor newDoctor = new Doctor(newUser, data.crm(), specialty);
        doctorRepository.save(newDoctor);
    }

    public Page<DoctorDetailDTO> listDoctors(Long specialtyId, Pageable pageable) {
        if (specialtyId != null) {
            return doctorRepository.findAllBySpecialtyId(specialtyId, pageable)
                    .map(DoctorDetailDTO::new);
        }

        return doctorRepository.findAll(pageable)
                .map(DoctorDetailDTO::new);
    }
}
