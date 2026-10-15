package com.barberflow.modules.users.application.service;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.CouponResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateCouponDTO;
import com.barberflow.modules.users.application.services.CouponService;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Coupon;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.ICouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private ICouponRepository couponRepository;

    @Mock
    private IBarbershopRepository barbershopRepository;

    @InjectMocks
    private CouponService couponService;

    private Barbershop barbershop;
    private CreateCouponDTO createCouponDTO;
    private Coupon savedCoupon;

    @BeforeEach
    void setUp() {
        barbershop = new Barbershop();
        barbershop.setBarbershopId(1L);
        barbershop.setBusinessName("BarberFlow HQ");

        createCouponDTO = new CreateCouponDTO();
        createCouponDTO.setCode("  descuento10  ");
        createCouponDTO.setDiscountPercent(10);
        createCouponDTO.setExpiryDate(LocalDate.now().plusMonths(1));
        createCouponDTO.setBarbershopId(1L);

        savedCoupon = Coupon.builder()
                .id(100L)
                .code("DESCUENTO10")
                .discountPercent(10)
                .expiryDate(createCouponDTO.getExpiryDate())
                .barbershop(barbershop)
                .isActive(true)
                .build();
    }

    // ==========================================
    // PRUEBAS DE CREACIÓN (createCoupon)
    // ==========================================

    @Test
    @DisplayName("Crear cupón específico de una barbería exitosamente")
    void createCoupon_ForBarbershop_Success() {
        // Given
        when(barbershopRepository.findById(1L)).thenReturn(Optional.of(barbershop));
        when(couponRepository.existsByCodeAndBarbershopId("DESCUENTO10", 1L)).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // When
        CouponResponseDTO response = couponService.createCoupon(createCouponDTO);

        // Then
        assertNotNull(response);
        assertEquals("DESCUENTO10", response.getCode());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Crear cupón global (sin barbería asignada) exitosamente")
    void createCoupon_Global_Success() {
        // Given
        createCouponDTO.setBarbershopId(null);

        Coupon globalCoupon = Coupon.builder()
                .id(101L)
                .code("DESCUENTO10")
                .discountPercent(10)
                .expiryDate(createCouponDTO.getExpiryDate())
                .barbershop(null)
                .isActive(true)
                .build();

        when(couponRepository.existsByCodeAndBarbershopIsNull("DESCUENTO10")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(globalCoupon);

        // When
        CouponResponseDTO response = couponService.createCoupon(createCouponDTO);

        // Then
        assertNotNull(response);
        assertEquals("DESCUENTO10", response.getCode());
        verify(barbershopRepository, never()).findById(any());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException si la barbería no existe")
    void createCoupon_ThrowsException_WhenBarbershopNotFound() {
        // Given
        when(barbershopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> couponService.createCoupon(createCouponDTO)
        );

        assertTrue(exception.getMessage().contains("Barbería no encontrada con ID: 1"));
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar BusinessRuleException cuando el código de cupón ya existe en la barbería")
    void createCoupon_ThrowsException_WhenCodeAlreadyExistsForBarbershop() {
        // Given
        when(barbershopRepository.findById(1L)).thenReturn(Optional.of(barbershop));
        when(couponRepository.existsByCodeAndBarbershopId("DESCUENTO10", 1L)).thenReturn(true);

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> couponService.createCoupon(createCouponDTO)
        );

        assertTrue(exception.getMessage().contains("ya existe"));
        verify(couponRepository, never()).save(any());
    }

    // ==========================================
    // PRUEBAS DE DESACTIVACIÓN (deactivateCoupon)
    // ==========================================

    @Test
    @DisplayName("Desactivar cupón exitosamente")
    void deactivateCoupon_Success() {
        // Given
        when(couponRepository.findById(100L)).thenReturn(Optional.of(savedCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // When
        couponService.deactivateCoupon(100L);

        // Then
        assertFalse(savedCoupon.getIsActive());
        verify(couponRepository, times(1)).save(savedCoupon);
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al intentar desactivar un cupón inexistente")
    void deactivateCoupon_ThrowsException_WhenCouponNotFound() {
        // Given
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> couponService.deactivateCoupon(999L)
        );

        assertTrue(exception.getMessage().contains("Cupón no encontrado con ID: 999"));
        verify(couponRepository, never()).save(any());
    }
}