package com.barberflow.modules.users.infrastructure.presentation;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Configuramos MockMvc en modo Standalone conectando directamente el Controller y el Advice
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- Dummy Controller privado e interno para simular las excepciones ---
    @RestController
    static class TestController {

        @GetMapping("/test/illegal-argument")
        void throwIllegalArgument() {
            throw new IllegalArgumentException("Parámetro inválido de prueba");
        }

        @GetMapping("/test/access-denied")
        void throwAccessDenied() {
            throw new AccessDeniedException("Acceso denegado de prueba");
        }

        @GetMapping("/test/runtime")
        void throwRuntime() {
            throw new RuntimeException("Error de negocio genérico");
        }

        @PostMapping("/test/validation")
        void throwValidation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/global-exception")
        void throwGlobalException() throws Exception {
            throw new Exception("Error inesperado en servidor");
        }
    }

    record TestRequest(@NotNull(message = "El campo nombre es obligatorio") String name) {}

    // --- CASOS DE PRUEBA ---

    @Test
    @DisplayName("Should handle IllegalArgumentException and return 400 Bad Request")
    void handleIllegalArgumentException_Success() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parámetro inválido de prueba"))
                .andExpect(jsonPath("$.path").value("/test/illegal-argument"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException and return 403 Forbidden")
    void handleAccessDeniedException_Success() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acceso denegado de prueba"))
                .andExpect(jsonPath("$.path").value("/test/access-denied"));
    }

    @Test
    @DisplayName("Should handle RuntimeException and return 400 Business Error")
    void handleRuntimeException_Success() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("Error de negocio genérico"))
                .andExpect(jsonPath("$.path").value("/test/runtime"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400 Bad Request with field errors")
    void handleMethodArgumentNotValidException_Success() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error de validación: name: El campo nombre es obligatorio"))
                .andExpect(jsonPath("$.path").value("/test/validation"));
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500 Internal Server Error")
    void handleGlobalException_Success() throws Exception {
        mockMvc.perform(get("/test/global-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocurrió un error inesperado en el servidor."))
                .andExpect(jsonPath("$.path").value("/test/global-exception"));
    }
}