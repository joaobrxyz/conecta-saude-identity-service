package com.example.identity_service.control;

import com.example.identity_service.dto.details.DoctorDetailDTO;
import com.example.identity_service.dto.register.DoctorRegisterDTO;
import com.example.identity_service.dto.update.DoctorUpdateDTO;
import com.example.identity_service.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerDoctor(@RequestBody @Valid DoctorRegisterDTO data) {
        doctorService.registerDoctor(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<DoctorDetailDTO>> listDoctors(@RequestParam(required = false) Long specialtyId, @PageableDefault(size = 10, sort = {"user.nome"}) Pageable pageable) {
        Page<DoctorDetailDTO> page = doctorService.listDoctors(specialtyId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDetailDTO> getDoctorById(@PathVariable UUID id) {
        DoctorDetailDTO dto = doctorService.getDoctorById(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateDoctor(@PathVariable UUID id, @RequestBody @Valid DoctorUpdateDTO data) {
        doctorService.updateDoctor(id, data);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

}
