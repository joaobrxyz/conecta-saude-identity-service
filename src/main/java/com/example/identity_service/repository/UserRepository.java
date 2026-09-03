package com.example.identity_service.repository;

import com.example.identity_service.model.TipoUser;
import com.example.identity_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Page<User> findAllByTipoUserAndAtivoTrue(TipoUser tipo, Pageable pageable);
    Optional<User> findByIdAndTipoUser(UUID id, TipoUser tipo);
}
