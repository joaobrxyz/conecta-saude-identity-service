package com.example.identity_service.control;

import com.example.identity_service.dto.AuthenticationDTO;
import com.example.identity_service.dto.TokenResponseDTO;
import com.example.identity_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody AuthenticationDTO data) {
        var tokenResponse = authService.authenticate(data);
        return ResponseEntity.ok(tokenResponse);
    }
}
