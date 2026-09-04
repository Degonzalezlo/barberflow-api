package com.barberflow.modules.users.infrastructure.presentation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.barberflow.modules.users.application.dtos.ProductRequestDTO;
import com.barberflow.modules.users.application.dtos.ProductResponseDTO;
import com.barberflow.modules.users.application.services.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // CREACIÓN: Exclusiva de ADMIN y SUPER_ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ProductResponseDTO createdProduct = productService.createProduct(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // LECTURA DE INVENTARIO: Disponible para ADMIN, BARBER y SUPER_ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER', 'SUPER_ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getInventory(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<ProductResponseDTO> inventory = productService.getInventoryByAdmin(userDetails.getUsername());
        return ResponseEntity.ok(inventory);
    }

    // AJUSTE DE STOCK: Exclusivo de ADMIN y SUPER_ADMIN
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateStock(
            @PathVariable Long id,
            @RequestParam int quantityChange,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ProductResponseDTO updatedProduct = productService.updateStock(id, quantityChange, userDetails.getUsername());
        return ResponseEntity.ok(updatedProduct);
    }

    // CONSULTA DE BAJO STOCK: Exclusivo de ADMIN y SUPER_ADMIN
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getLowStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer threshold) {
        
        List<ProductResponseDTO> lowStockProducts = productService.getLowStockProducts(userDetails.getUsername(), threshold);
        return ResponseEntity.ok(lowStockProducts);
    }
}
