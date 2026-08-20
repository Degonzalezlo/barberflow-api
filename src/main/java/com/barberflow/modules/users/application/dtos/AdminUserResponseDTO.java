package com.barberflow.modules.users.application.dtos;

import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponseDTO {

    private Long userId;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private Long barbershopId;
    private String barbershopName;
    private LocalDateTime createdAt;

    public static AdminUserResponseDTO fromEntity(User user) {
        return AdminUserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .barbershopId(user.getBarbershop() != null ? user.getBarbershop().getBarbershopId() : null)
                .barbershopName(user.getBarbershop() != null ? user.getBarbershop().getBusinessName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
