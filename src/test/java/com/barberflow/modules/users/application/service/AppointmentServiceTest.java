package com.barberflow.modules.users.application.service;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.modules.users.application.dtos.AppointmentRequestDTO;
import com.barberflow.modules.users.application.dtos.AppointmentResponseDTO;
import com.barberflow.modules.users.application.services.AppointmentService;
import com.barberflow.modules.users.domain.entities.*;
import com.barberflow.modules.users.domain.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAppointmentRepository appointmentRepository;

    @Mock
    private IBarbershopRepository barbershopRepository;

    @Mock
    private IClientRepository clientRepository;

    @Mock
    private IBarberRepository barberRepository;

    @Mock
    private IServiceEntityRepository serviceRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User clientUser;
    private User adminUser;
    private Barbershop barbershop;
    private Client client;
    private Barber barber;
    private ServiceEntity service;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        barbershop = new Barbershop();
        barbershop.setBarbershopId(1L);
        barbershop.setBusinessName("BarberFlow HQ");

        clientUser = new User();
        clientUser.setUserId(10L);
        clientUser.setEmail("cliente@barberflow.com");
        clientUser.setRole(UserRole.CLIENT);

        adminUser = new User();
        adminUser.setUserId(20L);
        adminUser.setEmail("admin@barberflow.com");
        adminUser.setRole(UserRole.ADMIN);
        barbershop.setUsers(List.of(adminUser));

        client = new Client();
        client.setClientId(100L);
        client.setFullName("Juan Pérez");
        client.setUser(clientUser);

        User barberUser = new User();
        barberUser.setUserId(30L);
        barberUser.setEmail("barbero@barberflow.com");
        barberUser.setRole(UserRole.BARBER);

        barber = new Barber();
        barber.setBarberId(50L);
        barber.setName("Carlos El Barbero");
        barber.setBarbershop(barbershop);
        barber.setUser(barberUser);

        service = new ServiceEntity();
        service.setId(200L);
        service.setName("Corte y Barba");
        service.setPrice(new BigDecimal("35000.00"));
        service.setDurationMinutes(30);

        appointment = Appointment.builder()
                .id(500L)
                .barbershop(barbershop)
                .client(client)
                .barber(barber)
                .service(service)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .status("Scheduled")
                .build();
    }

    // ==========================================
    // PRUEBAS DE CREACIÓN (createAppointment)
    // ==========================================

    @Test
    @DisplayName("Crear cita exitosamente cuando el usuario es CLIENT")
    void createAppointment_AsClient_Success() {
        // Given
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setBarbershopId(1L);
        requestDTO.setBarberId(50L);
        requestDTO.setServiceId(200L);
        requestDTO.setAppointmentDate(LocalDate.now().plusDays(1));
        requestDTO.setStartTime(LocalTime.of(10, 0));

        when(userRepository.findByEmail(clientUser.getEmail())).thenReturn(Optional.of(clientUser));
        when(barbershopRepository.findById(1L)).thenReturn(Optional.of(barbershop));
        when(clientRepository.findByUserEmail(clientUser.getEmail())).thenReturn(Optional.of(client));
        when(barberRepository.findById(50L)).thenReturn(Optional.of(barber));
        when(serviceRepository.findById(200L)).thenReturn(Optional.of(service));
        when(appointmentRepository.findActiveAppointmentsByBarberAndDate(any(), any())).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentResponseDTO response = appointmentService.createAppointment(requestDTO, clientUser.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("Juan Pérez", response.getClientName());
        assertEquals("Carlos El Barbero", response.getBarberName());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Lanzar BusinessRuleException cuando el barbero no pertenece a la barbería seleccionada")
    void createAppointment_ThrowsException_WhenBarberDoesNotBelongToBarbershop() {
        // Given
        Barbershop otherBarbershop = new Barbershop();
        otherBarbershop.setBarbershopId(99L);
        barber.setBarbershop(otherBarbershop);

        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setBarbershopId(1L);
        requestDTO.setBarberId(50L);

        when(userRepository.findByEmail(clientUser.getEmail())).thenReturn(Optional.of(clientUser));
        when(barbershopRepository.findById(1L)).thenReturn(Optional.of(barbershop));
        when(clientRepository.findByUserEmail(clientUser.getEmail())).thenReturn(Optional.of(client));
        when(barberRepository.findById(50L)).thenReturn(Optional.of(barber));

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> appointmentService.createAppointment(requestDTO, clientUser.getEmail())
        );

        assertTrue(exception.getMessage().contains("El barbero no pertenece a esta barbería"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar BusinessRuleException cuando hay conflicto de horario (Overlap)")
    void createAppointment_ThrowsException_WhenOverlapDetected() {
        // Given
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setBarbershopId(1L);
        requestDTO.setBarberId(50L);
        requestDTO.setServiceId(200L);
        requestDTO.setAppointmentDate(LocalDate.now().plusDays(1));
        requestDTO.setStartTime(LocalTime.of(10, 15)); // Solapa con la cita existente de 10:00 a 10:30

        when(userRepository.findByEmail(clientUser.getEmail())).thenReturn(Optional.of(clientUser));
        when(barbershopRepository.findById(1L)).thenReturn(Optional.of(barbershop));
        when(clientRepository.findByUserEmail(clientUser.getEmail())).thenReturn(Optional.of(client));
        when(barberRepository.findById(50L)).thenReturn(Optional.of(barber));
        when(serviceRepository.findById(200L)).thenReturn(Optional.of(service));
        when(appointmentRepository.findActiveAppointmentsByBarberAndDate(50L, requestDTO.getAppointmentDate()))
                .thenReturn(List.of(appointment));

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> appointmentService.createAppointment(requestDTO, clientUser.getEmail())
        );

        assertTrue(exception.getMessage().contains("no tiene disponibilidad"));
        verify(appointmentRepository, never()).save(any());
    }

    // ==========================================
    // PRUEBAS DE CANCELACIÓN (cancelAppointment)
    // ==========================================

    @Test
    @DisplayName("Cancelar cita exitosamente cuando el usuario es el dueño de la cita")
    void cancelAppointment_Success() throws AccessDeniedException {
        // Given
        when(userRepository.findByEmail(clientUser.getEmail())).thenReturn(Optional.of(clientUser));
        when(appointmentRepository.findById(500L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentResponseDTO response = appointmentService.cancelAppointment(500L, clientUser.getEmail());

        // Then
        assertNotNull(response);
        assertEquals("Cancelled", response.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Lanzar AccessDeniedException al intentar cancelar una cita de otro usuario")
    void cancelAppointment_ThrowsAccessDenied_WhenUserNotOwner() {
        // Given
        User otherClientUser = new User();
        otherClientUser.setUserId(99L);
        otherClientUser.setEmail("otro@barberflow.com");
        otherClientUser.setRole(UserRole.CLIENT);

        when(userRepository.findByEmail("otro@barberflow.com")).thenReturn(Optional.of(otherClientUser));
        when(appointmentRepository.findById(500L)).thenReturn(Optional.of(appointment));

        // When & Then
        assertThrows(
                AccessDeniedException.class,
                () -> appointmentService.cancelAppointment(500L, "otro@barberflow.com")
        );
        verify(appointmentRepository, never()).save(any());
    }

    // ==========================================
    // PRUEBAS DE CONSULTA AGENDA (getAgenda)
    // ==========================================

    @Test
    @DisplayName("Obtener agenda filtrada correctamente según el rol CLIENT")
    void getAgendaForAuthenticatedUser_Client_Success() {
        // Given
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);

        when(userRepository.findByEmail(clientUser.getEmail())).thenReturn(Optional.of(clientUser));
        when(appointmentRepository.findByClientUserEmailAndAppointmentDateBetweenAndStatusNot(
                clientUser.getEmail(), start, end, "Cancelled"
        )).thenReturn(List.of(appointment));

        // When
        List<AppointmentResponseDTO> agenda = appointmentService.getAgendaForAuthenticatedUser(
                clientUser.getEmail(), start, end
        );

        // Then
        assertNotNull(agenda);
        assertEquals(1, agenda.size());
        assertEquals("Corte y Barba", agenda.get(0).getServiceName());
    }
}
