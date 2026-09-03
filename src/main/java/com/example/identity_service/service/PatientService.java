package com.example.identity_service.service;

import com.example.identity_service.dto.details.PatientDetailDTO;
import com.example.identity_service.dto.register.PatientRegisterDTO;
import com.example.identity_service.dto.update.PatientUpdateDTO;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.Patient;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.PatientRepository;
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
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerPatient(PatientRegisterDTO data) {
        if (userRepository.findByEmail(data.email()).isPresent()){
            throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
        }

        if (patientRepository.existsByCpf(data.cpf())) {
            throw new RegraDeNegocioException("CPF já cadastrado no sistema.");
        }

        String senhaCripto = passwordEncoder.encode(data.senha());
        User user = new User(data, TipoUser.PACIENTE, senhaCripto);
        userRepository.save(user);

        Patient patient = new Patient(user, data.cpf());
        patientRepository.save(patient);
    }

    public Page<PatientDetailDTO> listPatients(Pageable pageable) {
        return patientRepository.findAllByUserAtivoTrue(pageable).map(PatientDetailDTO::new);
    }

    public PatientDetailDTO getPatientById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return new PatientDetailDTO(patient);
    }

    @Transactional
    public PatientDetailDTO updatePatient(UUID id, PatientUpdateDTO data) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));

        if (data.nome() != null && !data.nome().isBlank()) {
            patient.getUser().setNome(data.nome());
        }

        if (data.telefone() != null && !data.telefone().isBlank()) {
            patient.getUser().setTelefone(data.telefone());
        }

        if (data.email() != null && !data.email().isBlank()) {
            Optional<User> userExistente = userRepository.findByEmail(data.email());
            if (userExistente.isPresent() && !userExistente.get().getId().equals(patient.getUser().getId())) {
                throw new RegraDeNegocioException("Este e-mail já está em uso por outra conta.");
            }
            patient.getUser().setEmail(data.email());
        }
        return new PatientDetailDTO(patient);
    }

        public void deletePatient(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
        patient.getUser().inativar();
    }
}
