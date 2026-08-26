package com.example.identity_service.control;

import com.example.identity_service.dto.DoctorRegisterDTO;
import com.example.identity_service.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerDoctor(@RequestBody @Valid DoctorRegisterDTO data) {
        doctorService.registerDoctor(data);
        return ResponseEntity.ok().build();
    }
}
