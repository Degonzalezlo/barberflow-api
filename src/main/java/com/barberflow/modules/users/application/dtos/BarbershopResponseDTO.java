package com.barberflow.modules.users.application.dtos;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.SubscriptionPlan;

@Data
@Builder
public class BarbershopResponseDTO {

    private Long barbershopId;
    private String nit;
    private String businessName;
    private String address;
    private String phone;
    private Boolean isActive;
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime createdAt;

    public static BarbershopResponseDTO fromEntity(Barbershop barbershop) {
        return BarbershopResponseDTO.builder()
                .barbershopId(barbershop.getBarbershopId())
                .nit(barbershop.getNit())
                .businessName(barbershop.getBusinessName())
                .address(barbershop.getAddress())
                .phone(barbershop.getPhone())
                .isActive(barbershop.getIsActive())
                .subscriptionPlan(barbershop.getSubscriptionPlan())
                .createdAt(barbershop.getCreatedAt())
                .build();
    }
}