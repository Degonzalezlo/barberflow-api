package com.barberflow.modules.users.application.dtos;

import com.barberflow.modules.users.domain.entities.Coupon;
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
public class CouponResponseDTO {

    private Long id;
    private Long barbershopId;
    private String code;
    private Integer discountPercent;
    private LocalDate expiryDate;
    private Boolean isActive;
    private boolean isExpired;

    public static CouponResponseDTO fromEntity(Coupon coupon) {
    if (coupon == null) return null;

    boolean expired = coupon.getExpiryDate() != null 
            && LocalDate.now().isAfter(coupon.getExpiryDate());

    return CouponResponseDTO.builder()
            .id(coupon.getId())
            .barbershopId(coupon.getBarbershop() != null ? coupon.getBarbershop().getBarbershopId() : null)
            .code(coupon.getCode())
            .discountPercent(coupon.getDiscountPercent())
            .expiryDate(coupon.getExpiryDate())
            .isActive(coupon.getIsActive())
            .isExpired(expired)
            .build();
}
}
