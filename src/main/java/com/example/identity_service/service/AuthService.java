package com.example.identity_service.service;

import com.example.identity_service.dto.authentication.*;
import com.example.identity_service.exception.InvalidTokenException;
import com.example.identity_service.exception.RecursoNaoEncontradoException;
import com.example.identity_service.model.PasswordResetToken;
import com.example.identity_service.model.RefreshToken;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.PasswordResetTokenRepository;
import com.example.identity_service.repository.UserRepository;
import com.example.identity_service.service.token.RefreshTokenService;
import com.example.identity_service.service.token.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Transactional
    public void revogarToken(RefreshTokenRequestDTO data) {
        RefreshToken refreshToken = refreshTokenService.findByToken(data.refreshToken());
        refreshTokenService.deleteToken(refreshToken);
    }

    public void forgotPassword(@Valid ForgotPasswordDTO data) {
        Optional<User> userOptional = userRepository.findByEmail(data.email());
        if (userOptional.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(new PasswordResetToken(token, user));

        String link = "http://localhost:8080/auth/reset-password?token=" + token;

        System.out.println("E-MAIL ENVIADO PARA: " + user.getEmail());
        System.out.println("LINK: " + link);
    }

    @Transactional
    public void resetPassword(String token, @Valid ResetPasswordDTO data) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token de redefinição de senha inválido."));

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Token de redefinição de senha expirado.");
        }

        User user = resetToken.getUser();
        user.setSenha(passwordEncoder.encode(data.novaSenha()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}
