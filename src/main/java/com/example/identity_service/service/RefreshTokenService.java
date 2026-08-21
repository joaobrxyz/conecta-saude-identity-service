package com.example.identity_service.service;

import com.example.identity_service.model.RefreshToken;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository repository;

    public RefreshToken createRefreshToken(User user, boolean manterConectado) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());

        if (manterConectado) {
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        } else {
            refreshToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        }

        return repository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            repository.delete(token);
            throw new RuntimeException("Refresh token expirado. Faça login novamente.");
        }
        return token;
    }
}
