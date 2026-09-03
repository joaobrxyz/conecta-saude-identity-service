package com.example.identity_service.service;

import com.example.identity_service.dto.details.SpecialtyDetailDTO;
import com.example.identity_service.dto.register.SpecialtyRegisterDTO;
import com.example.identity_service.exception.RegraDeNegocioException;
import com.example.identity_service.model.Specialty;
import com.example.identity_service.repository.SpecialtyRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecialtyService {
    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Transactional
    public SpecialtyDetailDTO registerSpecialty(@Valid SpecialtyRegisterDTO data) {
        if (specialtyRepository.existsByNome(data.nome())) {
            throw new RegraDeNegocioException("Já existe uma especialidade cadastrada com este nome.");
        }

        Specialty specialty = new Specialty();
        specialty.setNome(data.nome());
        specialtyRepository.save(specialty);


        return new SpecialtyDetailDTO(specialty.getId(), specialty.getNome());
    }
}
