package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.CouponResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateCouponDTO;
import com.barberflow.modules.users.application.services.CouponService;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva la cadena de filtros JWT para aislar la prueba unitaria del controller
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean 
    private JwtAuthFilter jwtAuthFilter; // Mock del JwtAuthFilter para evitar la validación de JWT en las pruebas

    private CreateCouponDTO createCouponDTO;
    private CouponResponseDTO couponResponseDTO;

    @BeforeEach
    void setUp() {
        createCouponDTO = new CreateCouponDTO();
        createCouponDTO.setCode("DESCUENTO10");
        createCouponDTO.setDiscountPercent(10);
        createCouponDTO.setExpiryDate(LocalDate.now().plusMonths(1));
        createCouponDTO.setBarbershopId(1L);

        couponResponseDTO = new CouponResponseDTO();
        couponResponseDTO.setId(100L);
        couponResponseDTO.setCode("DESCUENTO10");
        couponResponseDTO.setDiscountPercent(10);
        couponResponseDTO.setExpiryDate(createCouponDTO.getExpiryDate());
        couponResponseDTO.setIsActive(true);
    }

    // ==========================================
    // POST /api/v1/coupons
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/coupons - Retorna 201 Created al crear un cupón exitosamente")
    @WithMockUser(roles = "ADMIN")
    void createCoupon_Returns201Created() throws Exception {
        // Given
        when(couponService.createCoupon(any(CreateCouponDTO.class))).thenReturn(couponResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCouponDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.code").value("DESCUENTO10"))
                .andExpect(jsonPath("$.discountPercent").value(10))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(couponService, times(1)).createCoupon(any(CreateCouponDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/coupons - Retorna 500 Internal Server Error cuando el código ya existe (Capturado por el try-catch)")
    @WithMockUser(roles = "ADMIN")
    void createCoupon_Returns500_WhenBusinessRuleFails() throws Exception {
        // Given
        when(couponService.createCoupon(any(CreateCouponDTO.class)))
                .thenThrow(new BusinessRuleException("El código de cupón 'DESCUENTO10' ya existe."));

        // When & Then
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCouponDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("El código de cupón 'DESCUENTO10' ya existe."));
    }

    @Test
    @DisplayName("POST /api/v1/coupons - Retorna 500 Internal Server Error cuando la barbería no existe")
    @WithMockUser(roles = "ADMIN")
    void createCoupon_Returns500_WhenBarbershopNotFound() throws Exception {
        // Given
        when(couponService.createCoupon(any(CreateCouponDTO.class)))
                .thenThrow(new ResourceNotFoundException("Barbería no encontrada con ID: 1"));

        // When & Then
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCouponDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Barbería no encontrada con ID: 1"));
    }

    // ==========================================
    // PATCH /api/v1/coupons/{id}/deactivate
    // ==========================================

    @Test
    @DisplayName("PATCH /api/v1/coupons/100/deactivate - Retorna 204 No Content al desactivar exitosamente")
    @WithMockUser(roles = "ADMIN")
    void deactivateCoupon_Returns204NoContent() throws Exception {
        // Given
        doNothing().when(couponService).deactivateCoupon(100L);

        // When & Then
        mockMvc.perform(patch("/api/v1/coupons/100/deactivate"))
                .andExpect(status().isNoContent());

        verify(couponService, times(1)).deactivateCoupon(100L);
    }
}