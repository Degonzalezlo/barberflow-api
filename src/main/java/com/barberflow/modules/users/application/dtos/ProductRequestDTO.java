package com.barberflow.modules.users.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 100, message = "El nombre del producto no puede superar los 100 caracteres")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un valor mayor a cero")
    private BigDecimal price;

    @NotNull(message = "La cantidad inicial en stock es obligatoria")
    @PositiveOrZero(message = "La cantidad en stock no puede ser negativa")
    private Integer stockQuantity;

    @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
    private String category;
}