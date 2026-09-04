package com.barberflow.modules.users.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.barberflow.modules.users.domain.entities.PaymentMethod;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponseDTO {

    private Long saleId;
    private Long barbershopId;
    private Long barberId;
    private String barberName;
    private Long clientId;
    private Long appointmentId;
    private BigDecimal totalPrice;
    private PaymentMethod paymentMethod;
    private LocalDateTime saleDate;
    private List<SaleItemResponseDTO> items;
}
