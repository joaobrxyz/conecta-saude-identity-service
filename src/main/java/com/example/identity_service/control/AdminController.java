package com.example.identity_service.control;

import com.example.identity_service.dto.details.UserDetailDTO;
import com.example.identity_service.dto.register.UserRegisterDTO;
import com.example.identity_service.dto.update.UserUpdateDTO;
import com.example.identity_service.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admins")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid UserRegisterDTO data) {
        adminService.registerAdmin(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDetailDTO>> listAdmins(@PageableDefault(size = 10, sort = {"nome"}) Pageable pageable) {
        Page<UserDetailDTO> page = adminService.listAdmins(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> getAdminById(@PathVariable("id") UUID id) {
        UserDetailDTO dto = adminService.getAdminById(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailDTO> updateAdmin(@PathVariable("id") UUID id, @RequestBody UserUpdateDTO data) {
        UserDetailDTO dto = adminService.updateAdmin(id, data);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdmin(AuthenticatedPrincipal principal) {
        UUID id = UUID.fromString(principal.getName());
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

}
