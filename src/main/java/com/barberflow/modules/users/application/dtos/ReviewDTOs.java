package com.barberflow.modules.users.application.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewDTOs {
    
    public record CreateReviewRequest(
        @NotNull(message = "El id de la cita es obligatorio")
        Long appointmentId,

        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        Integer rating,

        String comment
    ) {}

    public record ReviewResponse(
        Long reviewId,
        Long barbershopId,
        Long appointmentId,
        String clientName,
        String barberName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
    ) {}
    
}
