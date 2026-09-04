package com.barberflow.modules.users.application.dtos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;

public record BarberCommissionDTO(
    Long barberId,
    String barberName,
    Long completedAppointmentsCount,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
    BigDecimal totalServicesAmount,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
    BigDecimal totalProductsAmount,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
    BigDecimal commissionServices,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
    BigDecimal commissionProducts,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.00")
    BigDecimal totalPayout
) {}