package com.barberflow.modules.users.application.dtos;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class ServiceRequestDTO {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private Long barbershopId;
    
}
