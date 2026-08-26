package com.example.identity_service.service;

import com.example.identity_service.dto.AuthenticationDTO;
import com.example.identity_service.dto.RefreshTokenRequestDTO;
import com.example.identity_service.dto.TokenResponseDTO;
import com.example.identity_service.exception.InvalidTokenException;
import com.example.identity_service.model.RefreshToken;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import com.example.identity_service.service.token.RefreshTokenService;
import com.example.identity_service.service.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService implements UserDetailsService {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    @Lazy
    private AuthenticationManager authenticatorManager;

    @Autowired
    private UserRepository userRepository;

    public TokenResponseDTO authenticate(AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        var auth = this.authenticatorManager.authenticate(usernamePassword);
        var user = (User) auth.getPrincipal();
        var accessToken = tokenService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user, data.manterConectado());
        return new TokenResponseDTO(accessToken, refreshToken.getToken());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado"));
    }

    public TokenResponseDTO refreshToken(RefreshTokenRequestDTO data) {
        RefreshToken refreshToken = refreshTokenService.findByToken(data.refreshToken());

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenService.deleteToken(refreshToken);
            throw new InvalidTokenException("Refresh Token expirado. Faça login novamente.");
        }

        User user = refreshToken.getUser();

        String newAccessToken = tokenService.generateToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user, refreshToken.isManterConectado()).getToken();

        refreshTokenService.deleteToken(refreshToken);

        return new TokenResponseDTO(newAccessToken, newRefreshToken);
    }
}
