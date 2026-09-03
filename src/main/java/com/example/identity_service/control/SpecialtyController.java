package com.example.identity_service.control;

import com.example.identity_service.dto.details.SpecialtyDetailDTO;
import com.example.identity_service.dto.register.SpecialtyRegisterDTO;
import com.example.identity_service.service.SpecialtyService;
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
@RequestMapping("/specialties")
public class SpecialtyController {
    @Autowired
    private SpecialtyService specialtyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtyDetailDTO> registerSpecialty(@RequestBody @Valid SpecialtyRegisterDTO data) {
        SpecialtyDetailDTO dto = specialtyService.registerSpecialty(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
