package com.barberflow.modules.users.infrastructure.presentation;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberflow.modules.auth.dto.AuthResponse;
import com.barberflow.modules.auth.dto.LoginRequest;
import com.barberflow.modules.auth.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth") // Ruta base para autenticación
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Llamamos al servicio y devolvemos una respuesta 200 OK con el token
        return ResponseEntity.ok(authService.login(request));
    }
}
