package com.barberflow.modules.users.infrastructure.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberflow.modules.users.application.dtos.CouponResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateCouponDTO;
import com.barberflow.modules.users.application.services.CouponService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CreateCouponDTO dto) {
    try {
        CouponResponseDTO response = couponService.createCoupon(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
        e.printStackTrace(); // Imprime la traza completa en la consola de IntelliJ/Eclipse
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCoupon(
        @PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
    
}
