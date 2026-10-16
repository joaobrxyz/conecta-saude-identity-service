package com.example.identity_service.control;

import com.example.identity_service.model.User;
import com.example.identity_service.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AccountController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public record PasswordChange(@NotBlank String senhaAtual, @NotBlank @Size(min = 6) String novaSenha) {}

    @PatchMapping("/password")
    @Transactional
    public ResponseEntity<Void> changePassword(Authentication auth, @RequestBody @Valid PasswordChange input) {
        User user = users.findById(UUID.fromString(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (!encoder.matches(input.senhaAtual(), user.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }
        user.setSenha(encoder.encode(input.novaSenha()));
        users.save(user);
        return ResponseEntity.noContent().build();
    }
}
