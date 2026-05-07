package com.barberflow.modules.users.infrastructure.presentation;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberflow.modules.auth.dto.AuthResponse;
import com.barberflow.modules.auth.dto.LoginRequest;
import com.barberflow.modules.auth.services.AuthService;
import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth") // Ruta base para autenticación
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRegistrationDTO request) {
        // Usamos 201 Created para registros exitosos
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Llamamos al servicio y devolvemos una respuesta 200 OK con el token
        return ResponseEntity.ok(authService.login(request));
    }
}
