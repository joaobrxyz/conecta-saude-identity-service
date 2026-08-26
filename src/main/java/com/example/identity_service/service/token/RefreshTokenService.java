package com.example.identity_service.service.token;

import com.example.identity_service.exception.InvalidTokenException;
import com.example.identity_service.model.RefreshToken;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        refreshToken.setManterConectado(manterConectado);

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

    public RefreshToken findByToken(String refreshToken) {
        return repository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Token não encontrado"));
    }

    @Transactional
    public void deleteToken(RefreshToken refreshToken) {
        repository.delete(refreshToken);
    }
}
