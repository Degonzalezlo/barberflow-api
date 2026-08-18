package com.barberflow.modules.users.application.services;

import org.springframework.stereotype.Service;
import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.CouponResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateCouponDTO;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Coupon;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.ICouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final ICouponRepository couponRepository;
    private final IBarbershopRepository barbershopRepository;

    @Transactional
    public CouponResponseDTO createCoupon(CreateCouponDTO dto) {
        String cleanCode = dto.getCode().toUpperCase().trim();

        // 1. Validar si la barbería existe (si viene especificada)
        Barbershop shop = null;
        if (dto.getBarbershopId() != null) {
            shop = barbershopRepository.findById(dto.getBarbershopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + dto.getBarbershopId()));
        }

        // 2. Validar que no exista un cupón con el mismo código en esa barbería o a nivel global
        boolean exists = (shop != null) 
                ? couponRepository.existsByCodeAndBarbershopId(cleanCode, shop.getBarbershopId())
                : couponRepository.existsByCodeAndBarbershopIsNull(cleanCode);

        if (exists) {
            throw new BusinessRuleException("El código de cupón '" + cleanCode + "' ya existe.");
        }

        // 3. Crear entidad con el patrón Builder
        Coupon coupon = Coupon.builder()
                .code(cleanCode)
                .discountPercent(dto.getDiscountPercent())
                .expiryDate(dto.getExpiryDate())
                .barbershop(shop)
                .isActive(true)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        // 4. Mapear a DTO de salida
        return CouponResponseDTO.fromEntity(savedCoupon);
    }

    @Transactional
    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado con ID: " + couponId));

        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }
}