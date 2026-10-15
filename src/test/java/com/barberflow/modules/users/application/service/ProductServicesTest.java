package com.barberflow.modules.users.application.service;


import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barberflow.modules.users.application.dtos.ProductRequestDTO;
import com.barberflow.modules.users.application.dtos.ProductResponseDTO;
import com.barberflow.modules.users.application.services.ProductService;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Product;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IProductRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private User admin;
    private Barbershop barbershop;
    private Product product;

    @BeforeEach
    void setUp() {
        barbershop = new Barbershop();
        barbershop.setBarbershopId(1L);
        barbershop.setBusinessName("BarberFlow HQ");

        admin = new User();
        admin.setUserId(10L);
        admin.setEmail("admin@barberflow.com");
        admin.setBarbershop(barbershop);

        product = new Product();
        product.setId(100L);
        product.setName("Pomada Mate");
        product.setPrice(new BigDecimal("25000.00"));
        product.setStockQuantity(10);
        product.setCategory("Styling");
        product.setBarbershop(barbershop);
    }

    @Test
    @DisplayName("Crear producto exitosamente asignándolo a la sede del Admin")
    void createProduct_Success() {
        // Given
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Pomada Mate");
        requestDTO.setPrice(new BigDecimal("25000.00"));
        requestDTO.setStockQuantity(10);
        requestDTO.setCategory("Styling");

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDTO response = productService.createProduct(requestDTO, admin.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Pomada Mate", response.getName());
        assertEquals(1L, response.getBarbershopId());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Obtener todo el inventario de la sede del Admin")
    void getInventoryByAdmin_Success() {
        // Given
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findProductsByBarbershopBarbershopId(1L)).thenReturn(List.of(product));

        // When
        List<ProductResponseDTO> inventory = productService.getInventoryByAdmin(admin.getEmail());

        // Then
        assertNotNull(inventory);
        assertEquals(1, inventory.size());
        assertEquals("Pomada Mate", inventory.get(0).getName());
        verify(productRepository, times(1)).findProductsByBarbershopBarbershopId(1L);
    }

    @Test
    @DisplayName("Incrementar stock cuando quantityChange es mayor a 0")
    void updateStock_Increase_Success() {
        // Given
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findByIdAndBarbershopBarbershopId(100L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProductResponseDTO response = productService.updateStock(100L, 5, admin.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(15, response.getStockQuantity());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Reducir stock cuando quantityChange es menor a 0")
    void updateStock_Decrease_Success() {
        // Given
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findByIdAndBarbershopBarbershopId(100L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProductResponseDTO response = productService.updateStock(100L, -3, admin.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(7, response.getStockQuantity());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Lanzar EntityNotFoundException si el producto no pertenece a la sede al actualizar stock")
    void updateStock_ThrowsException_WhenProductNotFound() {
        // Given
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findByIdAndBarbershopBarbershopId(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> productService.updateStock(999L, 5, admin.getEmail())
        );

        assertTrue(exception.getMessage().contains("Producto no encontrado"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener productos con bajo stock usando el umbral por defecto (5) cuando threshold es null")
    void getLowStockProducts_UsesDefaultThreshold_WhenNull() {
        // Given
        product.setStockQuantity(2);
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findByBarbershopBarbershopIdAndStockQuantityLessThan(1L, 5))
                .thenReturn(List.of(product));

        // When
        List<ProductResponseDTO> lowStockList = productService.getLowStockProducts(admin.getEmail(), null);

        // Then
        assertNotNull(lowStockList);
        assertEquals(1, lowStockList.size());
        verify(productRepository, times(1)).findByBarbershopBarbershopIdAndStockQuantityLessThan(1L, 5);
    }

    @Test
    @DisplayName("Obtener productos con bajo stock usando el umbral personalizado proporcionado")
    void getLowStockProducts_UsesCustomThreshold() {
        // Given
        product.setStockQuantity(8);
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findByBarbershopBarbershopIdAndStockQuantityLessThan(1L, 10))
                .thenReturn(List.of(product));

        // When
        List<ProductResponseDTO> lowStockList = productService.getLowStockProducts(admin.getEmail(), 10);

        // Then
        assertNotNull(lowStockList);
        assertEquals(1, lowStockList.size());
        verify(productRepository, times(1)).findByBarbershopBarbershopIdAndStockQuantityLessThan(1L, 10);
    }
}
