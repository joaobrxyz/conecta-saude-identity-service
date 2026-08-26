package com.example.identity_service.service.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.example.identity_service.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("conecta-saude")
                    .withSubject(user.getId().toString())
                    .withClaim("role", user.getTipoUser().name())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("conecta-saude")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (Exception exception) {
            throw new RuntimeException("Token JWT inválido", exception);
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-03:00"));
    }

    public String getRoleFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("conecta-saude")
                .build()
                .verify(token)
                .getClaim("role").asString();
    }
}
