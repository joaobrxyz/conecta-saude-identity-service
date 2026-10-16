package com.example.identity_service.config;

import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.model.TipoUser;
import com.example.identity_service.repository.UserRepository;
import com.example.identity_service.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Explicit, opt-in bootstrap for a new local database; never runs in the default profile. */
@Component
@Profile("local")
public class LocalAdminBootstrap implements CommandLineRunner {
    private final UserRepository users;
    private final AdminService admins;
    @Value("${app.bootstrap-admin.email:}") private String email;
    @Value("${app.bootstrap-admin.password:}") private String password;
    public LocalAdminBootstrap(UserRepository users, AdminService admins) { this.users = users; this.admins = admins; }
    @Override public void run(String... args) {
        if (email.isBlank() || password.isBlank()) return;
        if (users.findAllByTipoUserAndAtivoTrue(TipoUser.ADMIN, Pageable.unpaged()).hasContent()) return;
        if (password.length() < 12) throw new IllegalArgumentException("A senha inicial do administrador deve ter pelo menos 12 caracteres.");
        admins.registerAdmin(new UserRegisterDTO("Administrador", email, "", password));
    }
}
