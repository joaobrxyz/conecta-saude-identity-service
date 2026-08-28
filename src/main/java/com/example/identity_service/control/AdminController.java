package com.example.identity_service.control;

import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid UserRegisterDTO data) {
        adminService.registerAdmin(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
