package com.barberflow.modules.users.application.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponDTO {

    @NotBlank(message = "El código del cupón es obligatorio")
    @Size(max = 20, message = "El código no puede superar los 20 caracteres")
    private String code;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @Min(value = 1, message = "El descuento mínimo es del 1%")
    @Max(value = 100, message = "El descuento máximo es del 100%")
    private Integer discountPercent;

    @NotNull(message = "La fecha de expiración es obligatoria")
    @Future(message = "La fecha de expiración debe ser una fecha futura")
    private LocalDate expiryDate;

    // Opcional: si es null, se maneja como cupón global o se asigna en el servicio
    private Long barbershopId;
}