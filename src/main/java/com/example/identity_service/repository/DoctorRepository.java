package com.example.identity_service.repository;

import com.example.identity_service.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    boolean existsByCrm(String crm);
    Page<Doctor> findAllBySpecialtyId(Long specialtyId, Pageable pageable);
}
