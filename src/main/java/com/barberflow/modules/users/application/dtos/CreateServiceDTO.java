package com.barberflow.modules.users.application.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateServiceDTO {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Min(value = 20, message = "La duración mínima es de 20 minutos")
    private Integer durationMinutes;

    @NotNull(message = "El ID de la barbería es obligatorio")
    private Long barbershopId;
}
