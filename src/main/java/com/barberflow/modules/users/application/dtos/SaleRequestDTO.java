package com.barberflow.modules.users.application.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.barberflow.modules.users.domain.entities.PaymentMethod;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleRequestDTO {

    private Long appointmentId; // Opcional (si se liquida una cita agendada)

  
    private Long barberId;

    private Long clientId; // Opcional (cliente recurrente o casual)

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @Valid
    private List<SaleItemRequestDTO> products; // Opcional (lista de productos comprados)
}