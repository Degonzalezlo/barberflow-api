package com.barberflow.modules.users.application.dtos;


import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {
    private Long barbershopId;
    private Long clientId;
    private Long barberId;
    private Long serviceId;
    private Long couponId; // Puede ser null si no aplican descuento
    private LocalDate appointmentDate; // Formato en Postman: "2026-05-20"
    private LocalTime startTime;       // Formato en Postman: "14:30:00"
}
