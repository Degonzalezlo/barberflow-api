package com.barberflow.modules.users.application.dtos;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ServiceResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    // En lugar de toda la entidad Barbershop, solo enviamos datos clave
    private Long barbershopId;
    private String barbershopName;
    
}
