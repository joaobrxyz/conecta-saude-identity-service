package com.example.identity_service.control;

import com.example.identity_service.dto.PatientRegisterDTO;
import com.example.identity_service.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
