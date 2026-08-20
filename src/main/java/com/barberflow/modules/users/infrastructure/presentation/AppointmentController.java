package com.barberflow.modules.users.infrastructure.presentation;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barberflow.modules.users.application.dtos.AppointmentRequestDTO;
import com.barberflow.modules.users.application.dtos.AppointmentResponseDTO;
import com.barberflow.modules.users.application.services.AppointmentService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/api/v1/appointments")
@AllArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // 1. Endpoint para CREAR una cita
    @PostMapping("/book")
    public ResponseEntity<AppointmentResponseDTO> bookAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AppointmentRequestDTO dto) {
        String email = userDetails.getUsername(); // Obtener el email del usuario autenticado
        AppointmentResponseDTO response = appointmentService.createAppointment(dto, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Enpoint para Cancelar una cita
    @PatchMapping("/cancel/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) throws AccessDeniedException {
        String email = userDetails.getUsername(); // Obtener el email del usuario autenticado
        AppointmentResponseDTO response = appointmentService.cancelAppointment(appointmentId, email);
        return ResponseEntity.ok(response);
    }

    // 3. Endpoint para ACTUALIZAR una cita
    @PutMapping("/update/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId,
            @RequestBody AppointmentRequestDTO dto) throws AccessDeniedException {
        String email = userDetails.getUsername(); // Obtener el email del usuario autenticado
        AppointmentResponseDTO response = appointmentService.updateAppointment(appointmentId, dto, email);
        return ResponseEntity.ok(response);
    }

    // 4. Endpoint para VER LA AGENDA de un día específico
    @GetMapping("/agenda")
    public ResponseEntity<List<AppointmentResponseDTO>> getAgenda(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value ="endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String email = userDetails.getUsername(); // Obtener el email del usuario autenticado
        
        LocalDate finalEndDate = endDate != null ? endDate : startDate; // Si no se proporciona endDate, usar startDate
        
        List<AppointmentResponseDTO> agenda = appointmentService.getAgendaForAuthenticatedUser(email, startDate, finalEndDate);
        return ResponseEntity.ok(agenda);
    }
}