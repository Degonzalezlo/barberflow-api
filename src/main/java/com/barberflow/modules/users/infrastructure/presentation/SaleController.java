package com.barberflow.modules.users.infrastructure.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.barberflow.modules.users.application.dtos.BarberCommissionDTO;
import com.barberflow.modules.users.application.dtos.SaleRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleResponseDTO;
import com.barberflow.modules.users.application.services.SaleService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    // 1. Registrar una nueva venta (Servicio, Productos o Mixto)
    @PostMapping
    public ResponseEntity<SaleResponseDTO> processSale(
            @Valid @RequestBody SaleRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SaleResponseDTO response = saleService.processSale(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Obtener el historial completo de ventas de la barbería
    @GetMapping
    public ResponseEntity<List<SaleResponseDTO>> getSalesByBarbershop(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<SaleResponseDTO> sales = saleService.getSalesByBarbershop(userDetails.getUsername());
        return ResponseEntity.ok(sales);
    }

    // 3. Consultar ventas filtradas por rango de fechas (Cierre de caja)
    @GetMapping("/range")
    public ResponseEntity<List<SaleResponseDTO>> getSalesByDateRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        List<SaleResponseDTO> sales = saleService.getSalesByDateRange(userDetails.getUsername(), start, end);
        return ResponseEntity.ok(sales);
    }

    // 4. Obtener reporte consolidado de comisiones por barbero en un rango de fechas
    @GetMapping("/commissions")
    public ResponseEntity<List<BarberCommissionDTO>> getBarberCommissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        List<BarberCommissionDTO> commissions = saleService.calculateBarberCommissions(userDetails.getUsername(), start, end);
        return ResponseEntity.ok(commissions);
    }
}