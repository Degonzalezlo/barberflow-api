package com.barberflow.modules.users.infrastructure.presentation;


import com.barberflow.modules.users.application.dtos.AppointmentRequestDTO;
import com.barberflow.modules.users.application.dtos.AppointmentResponseDTO;
import com.barberflow.modules.users.application.services.AppointmentService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter; // Mock del filtro de seguridad

    private static final String USER_EMAIL = "cliente@barberflow.com";

    @Test
    @WithMockUser(username = USER_EMAIL)
    @DisplayName("POST /api/v1/appointments/book - Debe agendar cita y retornar 201 Created")
    void bookAppointment_Success() throws Exception {
        // Arrange
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        // Llenamos campos obligatorios según las reglas de validación del DTO
        dto.setBarbershopId(1L);
        dto.setBarberId(2L);
        dto.setServiceId(3L);
        dto.setAppointmentDate(LocalDate.of(2026, 05, 20));
        dto.setStartTime(LocalTime.of(14, 30));

        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO();
        responseDTO.setId(100L);
        responseDTO.setStatus("COMPLETED");

        when(appointmentService.createAppointment(any(AppointmentRequestDTO.class), eq(USER_EMAIL)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(appointmentService).createAppointment(any(AppointmentRequestDTO.class), eq(USER_EMAIL));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    @DisplayName("PATCH /api/v1/appointments/cancel/{id} - Debe cancelar cita y retornar 200 OK")
    void cancelAppointment_Success() throws Exception {
        // Arrange
        Long appointmentId = 100L;
        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO();
        responseDTO.setId(appointmentId);
        responseDTO.setStatus("CANCELLED");

        when(appointmentService.cancelAppointment(appointmentId, USER_EMAIL))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/appointments/cancel/{appointmentId}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(appointmentService).cancelAppointment(appointmentId, USER_EMAIL);
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    @DisplayName("PUT /api/v1/appointments/update/{id} - Debe actualizar cita y retornar 200 OK")
    void updateAppointment_Success() throws Exception {
        // Arrange
        Long appointmentId = 100L;
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setBarbershopId(1L);
        dto.setBarberId(2L);
        dto.setServiceId(3L);
        dto.setAppointmentDate(LocalDate.of(2026, 05, 21));
        dto.setStartTime(LocalTime.of(16, 00));

        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO();
        responseDTO.setId(appointmentId);
        responseDTO.setStatus("SCHEDULED");

        when(appointmentService.updateAppointment(eq(appointmentId), any(AppointmentRequestDTO.class), eq(USER_EMAIL)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/appointments/update/{appointmentId}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService).updateAppointment(eq(appointmentId), any(AppointmentRequestDTO.class), eq(USER_EMAIL));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    @DisplayName("GET /api/v1/appointments/agenda - Debe consultar agenda de un día y usar startDate como fallback")
    void getAgenda_SingleDate_Success() throws Exception {
        // Arrange
        LocalDate today = LocalDate.of(2026, 05, 21);

        AppointmentResponseDTO item = new AppointmentResponseDTO();
        item.setId(100L);

        when(appointmentService.getAgendaForAuthenticatedUser(USER_EMAIL, today, today))
                .thenReturn(List.of(item));

        // Act & Assert (Pasamos únicamente startDate)
        mockMvc.perform(get("/api/v1/appointments/agenda")
                        .param("startDate", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));

        // Verificamos que el controlador asignó finalEndDate = startDate al ser endDate nulo
        verify(appointmentService).getAgendaForAuthenticatedUser(USER_EMAIL, today, today);
    }
}