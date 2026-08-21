package com.example.identity_service.service;

import com.example.identity_service.dto.AuthenticationDTO;
import com.example.identity_service.dto.TokenResponseDTO;
import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
}
