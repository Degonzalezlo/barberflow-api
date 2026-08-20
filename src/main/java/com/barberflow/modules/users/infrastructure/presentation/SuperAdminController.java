package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.modules.users.application.dtos.CreateAdminDTO;
import com.barberflow.modules.users.application.dtos.CreateBarbershopDTO;
import com.barberflow.modules.users.application.services.SuperAdminService;
import com.barberflow.modules.users.application.dtos.AdminUserResponseDTO;
import com.barberflow.modules.users.application.dtos.BarbershopResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')") // 🔒 Bloqueado a nivel de clase: Solo SUPER_ADMIN ingresa
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

   @PostMapping("/barbershops")
public ResponseEntity<BarbershopResponseDTO> createBarbershop(@Valid @RequestBody CreateBarbershopDTO dto) {
    BarbershopResponseDTO response = superAdminService.createBarbershop(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

@PostMapping("/users/admins")
public ResponseEntity<AdminUserResponseDTO> createAdminUser(@Valid @RequestBody CreateAdminDTO dto) {
    AdminUserResponseDTO response = superAdminService.createAdminUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
}
