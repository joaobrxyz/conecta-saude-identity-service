package com.example.identity_service.control;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.service.ReceptionistService;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCAO')")
    public ResponseEntity<Page<UserDetailDTO>> listReceptionists(@PageableDefault(size = 10, sort = {"nome"}) Pageable pageable) {
        Page<UserDetailDTO> page = receptionistService.listReceptionists(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCAO')")
    public ResponseEntity<UserDetailDTO> getReceptionistById(@PathVariable("id") UUID id) {
        UserDetailDTO receptionist = receptionistService.getReceptionistById(id);
        return ResponseEntity.ok(receptionist);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> updateReceptionist(@PathVariable UUID id, @RequestBody UserUpdateDTO data) {
        UserDetailDTO dto = receptionistService.updateReceptionist(id, data);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReceptionist(@PathVariable UUID id) {
        receptionistService.deleteReceptionist(id);
        return ResponseEntity.noContent().build();
    }

}
