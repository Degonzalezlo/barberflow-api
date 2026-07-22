package com.barberflow.modules.users.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonFormat(pattern = "yyyy-MM-dd")
public class AppointmentResponseDTO {
    private Long id;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private String status;
    
    // Información aplanada para el cliente (No genera bucles)
    private Long barbershopId;
    private String clientName;
    private String barberName;
    private String serviceName;
    private BigDecimal price; // Mostramos el precio histórico o actual de la cita
}