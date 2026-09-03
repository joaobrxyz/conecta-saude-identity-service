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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
            return doctorRepository.findAllBySpecialtyIdAndUserAtivoTrue(specialtyId, pageable)
                    .map(DoctorDetailDTO::new);
        }

        return doctorRepository.findAllByUserAtivoTrue(pageable)
                .map(DoctorDetailDTO::new);
    }

    public DoctorDetailDTO getDoctorById(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado"));
        return new DoctorDetailDTO(doctor);
    }

    @Transactional
    public DoctorDetailDTO updateDoctor(UUID doctorId, DoctorUpdateDTO data) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado"));

        if (data.nome() != null && !data.nome().isBlank()) {
            doctor.getUser().setNome(data.nome());
        }

        if (data.telefone() != null && !data.telefone().isBlank()) {
            doctor.getUser().setTelefone(data.telefone());
        }

        if (data.email() != null && !data.email().isBlank()) {
            Optional<User> userExistente = userRepository.findByEmail(data.email());
            if (userExistente.isPresent() && !userExistente.get().getId().equals(doctor.getUser().getId())) {
                throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
            }
            doctor.getUser().setEmail(data.email());
        }

        return new DoctorDetailDTO(doctor);
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado"));
        doctor.getUser().inativar();
        userRepository.save(doctor.getUser());
    }
}
