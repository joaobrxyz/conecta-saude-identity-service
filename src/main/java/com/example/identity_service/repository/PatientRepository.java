package com.example.identity_service.repository;

import com.example.identity_service.model.Patient;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByCpf(String cpf);
    Page<Patient> findAllByUserAtivoTrue(Pageable pageable);
}
