package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.modules.users.application.dtos.ProductRequestDTO;
import com.barberflow.modules.users.application.dtos.ProductResponseDTO;
import com.barberflow.modules.users.application.services.ProductService;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private final String USERNAME = "admin@barberflow.com";

    @Nested
    @DisplayName("Create Product Endpoint Tests")
    class CreateProductEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("POST /api/v1/products - Should create product and return 201 Created")
        void createProduct_Success() throws Exception {
            ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                    .name("Pomade")
                    .price(new BigDecimal("15.00"))
                    .stockQuantity(10)
                    .build();

            ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                    .id(1L)
                    .name("Pomade")
                    .price(new BigDecimal("15.00"))
                    .stockQuantity(10)
                    .build();

            when(productService.createProduct(any(ProductRequestDTO.class), anyString()))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Pomade"))
                    .andExpect(jsonPath("$.price").value(15.00))
                    .andExpect(jsonPath("$.stockQuantity").value(10));

            verify(productService).createProduct(any(ProductRequestDTO.class), anyString());
        }
    }

    @Nested
    @DisplayName("Get Inventory Endpoint Tests")
    class GetInventoryEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("GET /api/v1/products - Should return inventory list and 200 OK")
        void getInventory_Success() throws Exception {
            ProductResponseDTO p1 = ProductResponseDTO.builder().id(1L).name("Pomade").build();
            ProductResponseDTO p2 = ProductResponseDTO.builder().id(2L).name("Shampoo").build();

            when(productService.getInventoryByAdmin(anyString()))
                    .thenReturn(List.of(p1, p2));

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));

            verify(productService).getInventoryByAdmin(anyString());
        }
    }

    @Nested
    @DisplayName("Update Stock Endpoint Tests")
    class UpdateStockEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("PATCH /api/v1/products/{id}/stock - Should update stock and return 200 OK")
        void updateStock_Success() throws Exception {
            ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                    .id(1L)
                    .name("Pomade")
                    .stockQuantity(15)
                    .build();

            when(productService.updateStock(eq(1L), eq(5), anyString()))
                    .thenReturn(responseDTO);

            mockMvc.perform(patch("/api/v1/products/{id}/stock", 1L)
                            .param("quantityChange", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.stockQuantity").value(15));

            verify(productService).updateStock(eq(1L), eq(5), anyString());
        }
    }

    @Nested
    @DisplayName("Get Low Stock Endpoint Tests")
    class GetLowStockEndpointTests {

        @Test
        @WithMockUser(username = USERNAME)
        @DisplayName("GET /api/v1/products/low-stock - Should return low stock products and 200 OK")
        void getLowStock_Success() throws Exception {
            ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                    .id(1L)
                    .name("Beard Oil")
                    .stockQuantity(2)
                    .build();

            when(productService.getLowStockProducts(anyString(), eq(5)))
                    .thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/v1/products/low-stock")
                            .param("threshold", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].stockQuantity").value(2));

            verify(productService).getLowStockProducts(anyString(), eq(5));
        }
    }
}
