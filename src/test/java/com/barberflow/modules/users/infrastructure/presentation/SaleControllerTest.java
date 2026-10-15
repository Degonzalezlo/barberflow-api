package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.modules.users.application.dtos.BarberCommissionDTO;
import com.barberflow.modules.users.application.dtos.SaleItemRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleResponseDTO;
import com.barberflow.modules.users.application.services.SaleService;
import com.barberflow.modules.users.domain.entities.PaymentMethod;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.barberflow.modules.users.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtService jwtTService;

    @MockitoBean 
    private UserDetails userDetails;

    private final String USERNAME = "admin@barberflow.com";

    @Nested
    @DisplayName("Process Sale Endpoint Tests")
    class ProcessSaleEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("POST /api/v1/sales - Should process sale and return 201 Created")
        void processSale_Success() throws Exception {
            // Arrange
            SaleItemRequestDTO itemDTO = SaleItemRequestDTO.builder()
                    .productId(100L)
                    .quantity(2)
                    .build();

            SaleRequestDTO requestDTO = SaleRequestDTO.builder()
                    .products(List.of(itemDTO))
                    .paymentMethod(PaymentMethod.CASH)
                    .build();

            SaleResponseDTO responseDTO = SaleResponseDTO.builder()
                    .saleId(500L)
                    .totalPrice(new BigDecimal("30.00"))
                    .paymentMethod(PaymentMethod.CASH)
                    .build();

            when(saleService.processSale(any(SaleRequestDTO.class), eq(USERNAME)))
                    .thenReturn(responseDTO);

            // Act & Assert
            mockMvc.perform(post("/api/v1/sales")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.saleId").value(500))
                    .andExpect(jsonPath("$.totalPrice").value(30.00))
                    .andExpect(jsonPath("$.paymentMethod").value("CASH"));

            verify(saleService).processSale(any(SaleRequestDTO.class), eq(USERNAME));
        }
    }

    @Nested
    @DisplayName("Get Sales History Endpoint Tests")
    class GetSalesHistoryEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("GET /api/v1/sales - Should return barbershop sales history and 200 OK")
        void getSalesByBarbershop_Success() throws Exception {
            // Arrange
            SaleResponseDTO sale1 = SaleResponseDTO.builder()
                    .saleId(1L)
                    .totalPrice(new BigDecimal("25.00"))
                    .build();

            SaleResponseDTO sale2 = SaleResponseDTO.builder()
                    .saleId(2L)
                    .totalPrice(new BigDecimal("50.00"))
                    .build();

            when(saleService.getSalesByBarbershop(USERNAME))
                    .thenReturn(List.of(sale1, sale2));

            // Act & Assert
            mockMvc.perform(get("/api/v1/sales")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].saleId").value(1))
                    .andExpect(jsonPath("$[1].saleId").value(2));

            verify(saleService).getSalesByBarbershop(USERNAME);
        }
    }

    @Nested
    @DisplayName("Date Range Sales Endpoint Tests")
    class DateRangeSalesEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("GET /api/v1/sales/range - Should return sales within date range and 200 OK")
        void getSalesByDateRange_Success() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.of(2026, 9, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 9, 30, 23, 59, 59);

            SaleResponseDTO sale = SaleResponseDTO.builder()
                    .saleId(10L)
                    .totalPrice(new BigDecimal("40.00"))
                    .build();

            when(saleService.getSalesByDateRange(eq(USERNAME), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(sale));

            // Act & Assert
            mockMvc.perform(get("/api/v1/sales/range")
                            .param("start", start.format(DateTimeFormatter.ISO_DATE_TIME))
                            .param("end", end.format(DateTimeFormatter.ISO_DATE_TIME))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].saleId").value(10))
                    .andExpect(jsonPath("$[0].totalPrice").value(40.00));

            verify(saleService).getSalesByDateRange(eq(USERNAME), any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Barber Commissions Endpoint Tests")
    class BarberCommissionsEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("GET /api/v1/sales/commissions - Should return commissions report and 200 OK")
        void getBarberCommissions_Success() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.of(2026, 9, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 9, 30, 23, 59, 59);

            BarberCommissionDTO commissionDTO = new BarberCommissionDTO(
            20L,
            "Carlos Perez",
            5L,                                  // completedAppointmentsCount
            new BigDecimal("100.00"),            // totalServicesAmount
            new BigDecimal("60.00"),             // totalProductsAmount
            new BigDecimal("50.00"),             // commissionServices
            new BigDecimal("0.00"),              // commissionProducts
            new BigDecimal("60.00")              // totalPayout
            );
                   
            when(saleService.calculateBarberCommissions(eq(USERNAME), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(commissionDTO));

            // Act & Assert
            mockMvc.perform(get("/api/v1/sales/commissions")
                            .param("start", start.format(DateTimeFormatter.ISO_DATE_TIME))
                            .param("end", end.format(DateTimeFormatter.ISO_DATE_TIME))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].barberId").value(20))
                    .andExpect(jsonPath("$[0].barberName").value("Carlos Perez"))
                    .andExpect(jsonPath("$[0].totalPayout").value(60.00));

            verify(saleService).calculateBarberCommissions(eq(USERNAME), any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }
}