package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.modules.users.application.dtos.AdminUserResponseDTO;
import com.barberflow.modules.users.application.dtos.BarbershopResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateAdminDTO;
import com.barberflow.modules.users.application.dtos.CreateBarbershopDTO;
import com.barberflow.modules.users.application.services.SuperAdminService;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuperAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class SuperAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SuperAdminService superAdminService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("POST /api/v1/super-admin/barbershops - Debe crear una barbería y retornar 201 Created")
    void createBarbershop_Success() throws Exception {
        // Arrange
        CreateBarbershopDTO inputDto = CreateBarbershopDTO.builder()
                .businessName("BarberFlow Central")
                .nit("900123456-7")
                .address("Calle 10 # 40-20")
                .phone("3001234567")
                .build();

        BarbershopResponseDTO responseDto = BarbershopResponseDTO.builder()
                .barbershopId(1L)
                .nit("900123456-7")
                .businessName("BarberFlow Central")
                .build();
      

        when(superAdminService.createBarbershop(any(CreateBarbershopDTO.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/super-admin/barbershops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.barbershopId").value(1))
                .andExpect(jsonPath("$.nit").value("900123456-7"))
                .andExpect(jsonPath("$.businessName").value("BarberFlow Central"));

        verify(superAdminService).createBarbershop(any(CreateBarbershopDTO.class));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("POST /api/v1/super-admin/users/admins - Debe crear un usuario administrador y retornar 201 Created")
    void createAdminUser_Success() throws Exception {
        // Arrange
        CreateAdminDTO inputDto = CreateAdminDTO.builder()
                .email("admin@barberflow.com")
                .password("AdminPassword123!")
                .barbershopId(1L)
                .build();

        AdminUserResponseDTO responseDto = AdminUserResponseDTO.builder()
                .userId(10L)
                .email("admin@barberflow.com")
                .build();

        when(superAdminService.createAdminUser(any(CreateAdminDTO.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/super-admin/users/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.email").value("admin@barberflow.com"));

        verify(superAdminService).createAdminUser(any(CreateAdminDTO.class));
    }
}
