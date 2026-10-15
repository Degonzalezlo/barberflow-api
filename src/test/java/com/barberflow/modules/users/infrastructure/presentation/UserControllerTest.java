package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.users.application.dtos.UserResponseDTO;
import com.barberflow.modules.users.application.services.UserService;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /api/v1/users/register - Debe registrar un cliente exitosamente y retornar 201 Created")
    void registerClient_Success() throws Exception {
        UserRegistrationDTO inputDto = new UserRegistrationDTO();
        inputDto.setEmail("cliente@example.com");
        inputDto.setPassword("Password123!");
        inputDto.setName("Carlos Perez");
        inputDto.setPhone("3001234567");

        UserResponseDTO responseDto = new UserResponseDTO();
        responseDto.setUserId(1L);
        responseDto.setEmail("cliente@example.com");
        responseDto.setRole(UserRole.CLIENT);

        when(userService.registerUserFromDTO(any(UserRegistrationDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("cliente@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        verify(userService).registerUserFromDTO(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/barbers - Debe registrar un barbero exitosamente y retornar 201 Created")
    void registerBarber_Success() throws Exception {
        UserRegistrationDTO inputDto = new UserRegistrationDTO();
        inputDto.setEmail("barbero@example.com");
        inputDto.setPassword("Password123!");
        inputDto.setName("Mateo Barbero");
        inputDto.setPhone("3009876543");
        inputDto.setBarbershopId(10L);

        UserResponseDTO responseDto = new UserResponseDTO();
        responseDto.setUserId(2L);
        responseDto.setEmail("barbero@example.com");
        responseDto.setRole(UserRole.BARBER);

        when(userService.registerUserFromDTO(any(UserRegistrationDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.email").value("barbero@example.com"))
                .andExpect(jsonPath("$.role").value("BARBER"));

        verify(userService).registerUserFromDTO(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/barbers/{barberId}/deactivate - Debe desactivar al barbero y retornar 204 No Content")
    void deactivateBarber_Success() throws Exception {
        Long barberId = 5L;

        doNothing().when(userService).deactivateBarber(barberId);

        mockMvc.perform(patch("/api/v1/users/barbers/{barberId}/deactivate", barberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService).deactivateBarber(barberId);
    }
}