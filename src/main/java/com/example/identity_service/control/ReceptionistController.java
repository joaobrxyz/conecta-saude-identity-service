package com.example.identity_service.control;

import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.service.ReceptionistService;
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
@RequestMapping("receptionists")
public class ReceptionistController {
    @Autowired
    private ReceptionistService receptionistService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerReceptionist(@RequestBody @Valid UserRegisterDTO data) {
        receptionistService.registerReceptionist(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
