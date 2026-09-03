package com.example.identity_service.control;

import com.example.identity_service.dto.details.PatientDetailDTO;
import com.example.identity_service.dto.register.PatientRegisterDTO;
import com.example.identity_service.dto.update.PatientUpdateDTO;
import com.example.identity_service.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @PostMapping
    public ResponseEntity<Void> registerPatient(@RequestBody @Valid PatientRegisterDTO data) {
        patientService.registerPatient(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'MEDICO')")
    public ResponseEntity<Page<PatientDetailDTO>> listPatients(@PageableDefault(size = 10, sort = {"user.nome"}) Pageable pageable) {
        Page<PatientDetailDTO> page = patientService.listPatients(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO', 'MEDICO')")
    public ResponseEntity<PatientDetailDTO> getPatientById(@PathVariable UUID id) {
        PatientDetailDTO dto = patientService.getPatientById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("profile")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PatientDetailDTO> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        PatientDetailDTO dto = patientService.getPatientById(userId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO')")
    public ResponseEntity<PatientDetailDTO> updatePatient(@PathVariable UUID id, @RequestBody PatientUpdateDTO data) {
        PatientDetailDTO dto = patientService.updatePatient(id, data);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("profile")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PatientDetailDTO> updateMyProfile (Authentication authentication, @RequestBody PatientUpdateDTO data) {
        UUID userId = UUID.fromString(authentication.getName());
        PatientDetailDTO dto = patientService.updatePatient(userId, data);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCAO')")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("profile")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> deleteMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        patientService.deletePatient(userId);
        return ResponseEntity.noContent().build();
    }

}
