package com.barberflow.modules.users.infrastructure.presentation;


import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.users.application.dtos.UserResponseDTO;
import com.barberflow.modules.users.application.services.UserService;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Registro PÚBLICO para clientes.
     * Forzamos el rol a CLIENT para evitar escalación de privilegios.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerClient(@Valid @RequestBody UserRegistrationDTO dto) {
        dto.setRole(UserRole.CLIENT);
        dto.setBarbershopId(null); // Un cliente público no requiere barbershopId en el registro initial

        UserResponseDTO response = userService.registerUserFromDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registro PROTEGIDO para barberos.
     * Exclusivo para usuarios con ROL ADMIN.
     */
    @PostMapping("/barbers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> registerBarber(@Valid @RequestBody UserRegistrationDTO dto) {
        dto.setRole(UserRole.BARBER); // Forzamos el rol a BARBER

        UserResponseDTO response = userService.registerUserFromDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/barbers/{barberId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateBarber(@PathVariable Long barberId) {
        userService.deactivateBarber(barberId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}
   