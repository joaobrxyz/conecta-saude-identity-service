package com.example.identity_service.repository;

import com.example.identity_service.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByNome(String nome);
}
