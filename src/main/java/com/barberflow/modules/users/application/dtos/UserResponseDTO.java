package com.barberflow.modules.users.application.dtos;

import com.barberflow.modules.users.domain.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long userId;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Datos simplificados de la barbería (sin bucles)
    private Long barbershopId;
    private String barbershopName;
}
