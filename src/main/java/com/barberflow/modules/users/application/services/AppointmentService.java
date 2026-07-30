package com.barberflow.modules.users.application.services;


import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.AppointmentRequestDTO;
import com.barberflow.modules.users.application.dtos.AppointmentResponseDTO;
import com.barberflow.modules.users.domain.entities.*;
import com.barberflow.modules.users.domain.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AppointmentService {

    // Aquí convergen todos los repositorios necesarios
    private final IUserRepository IUserRepository;
    private final IAppointmentRepository appointmentRepository;
    private final IBarbershopRepository barbershopRepository;
    private final IClientRepository clientRepository; // Asumiendo estos nombres de tus repositorios
    private final IBarberRepository barberRepository;
    private final IServiceEntityRepository serviceRepository;

   @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto, String userEmail) {
        User currentUser = IUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 1. Validar la Barbería
        Barbershop barbershop = barbershopRepository.findById(dto.getBarbershopId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada"));

        // 2. Determinar y validar el Cliente según el Rol
        Client client;
        if (currentUser.getRole() == UserRole.CLIENT) {
            // El cliente se busca a través de su email de usuario (no confía en el DTO)
            client = clientRepository.findByUserEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente no encontrado"));
        } else {
            // ADMIN o BARBER especifican el clientId en el DTO para atención presencial/manual
            if (dto.getClientId() == null) {
                throw new BusinessRuleException("Debe proporcionar el ID del cliente para agendar.");
            }
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        }

        // 3. Validar el Barbero
        Barber barber = barberRepository.findById(dto.getBarberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"));

        // [Regla de Negocio] Validar pertenencia del barbero a la barbería
        if (!barber.getBarbershop().getBarbershopId().equals(barbershop.getBarbershopId())) {
            throw new BusinessRuleException("El barbero no pertenece a esta barbería");
        }

        // 4. Validar el Servicio
        ServiceEntity service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // TODO: En el siguiente paso ejecutamos aquí la verificación de Overlap de horarios
       
        // ... Validar Servicio
        LocalTime newEnd = dto.getStartTime().plusMinutes(service.getDurationMinutes());

        // [Regla de Negocio] Validar Overlap
        validateOverlap(barber.getBarberId(), dto.getAppointmentDate(), dto.getStartTime(), newEnd, null);

// Guardar cita...
        // 5. Construir y guardar la cita
        Appointment appointment = Appointment.builder()
                .barbershop(barbershop)
                .client(client)
                .barber(barber)
                .service(service)
                .appointmentDate(dto.getAppointmentDate())
                .startTime(dto.getStartTime())
                .status("Scheduled")
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return mapToResponseDTO(savedAppointment);
    }

    // ==========================================
    // 2. CANCELAR CITA (Con validación de propiedad)
    // ==========================================
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId, String userEmail) throws AccessDeniedException {
        User currentUser = IUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        // Validar si el usuario tiene derecho a modificar esta cita específica
        validateOwnership(appointment, currentUser);

        appointment.setStatus("Cancelled");
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return mapToResponseDTO(updatedAppointment);
    }

    // ==========================================
    // 3. ACTUALIZAR / REAGENDAR CITA
    // ==========================================
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long appointmentId, AppointmentRequestDTO dto, String userEmail)
                    throws AccessDeniedException {
            User currentUser = IUserRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            Appointment appointment = appointmentRepository.findById(appointmentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

            // 1. Validar que el usuario autenticado sea el dueño/admin de la cita actual
            validateOwnership(appointment, currentUser);

            Barber newBarber = barberRepository.findById(dto.getBarberId())
                            .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"));

            // 2.Si quien edita es un BARBER, solo puede reasignarse a SÍ MISMO
            if (currentUser.getRole() == UserRole.BARBER
                            && !newBarber.getUser().getEmail().equals(currentUser.getEmail())) {
                    throw new BusinessRuleException("Un barbero no puede reasignar sus citas a otro barbero.");
            }
            // 3.Ni el cliente ni el barbero pueden cambiar el cliente de una cita existente
            if (currentUser.getRole() != UserRole.ADMIN) {
                if (dto.getClientId() != null && !appointment.getClient().getClientId().equals(dto.getClientId())) {
                        throw new BusinessRuleException("No está permitido cambiar el cliente asignado a una cita existente.");
                }
            }
            // 4. Si es ADMIN, permitir la reasignación del cliente
            if (currentUser.getRole() == UserRole.ADMIN && dto.getClientId() != null) {
                Client newClient = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + dto.getClientId()));
    
                    appointment.setClient(newClient);
            }

            ServiceEntity newService = serviceRepository.findById(dto.getServiceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

            // 3. Validar Overlap excluyendo la cita actual
            LocalTime newEnd = dto.getStartTime().plusMinutes(newService.getDurationMinutes());
            validateOverlap(newBarber.getBarberId(), dto.getAppointmentDate(), dto.getStartTime(), newEnd,
                            appointmentId);

            // 4. Actualizar cita
            appointment.setBarber(newBarber);
            appointment.setService(newService);
            appointment.setStartTime(dto.getStartTime());
            appointment.setAppointmentDate(dto.getAppointmentDate());

            Appointment updatedAppointment = appointmentRepository.save(appointment);

            return mapToResponseDTO(updatedAppointment);
    }

    // ==========================================
    // 4. CONSULTA DE AGENDA (Ya validada 100%)
    // ==========================================
        @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAgendaForAuthenticatedUser(String email, LocalDate startDate, LocalDate endDate) {
        User user = IUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Appointment> appointments;
        UserRole role = user.getRole();

        switch (role) {
            case BARBER -> appointments = appointmentRepository
                    .findByBarberUserEmailAndAppointmentDateBetweenAndStatusNot(email, startDate, endDate, "Cancelled");

            case CLIENT -> appointments = appointmentRepository
                    .findByClientUserEmailAndAppointmentDateBetweenAndStatusNot(email, startDate, endDate, "Cancelled");

            case ADMIN -> appointments = appointmentRepository
                    .findByBarbershopUsersEmailAndAppointmentDateBetweenAndStatusNot(email, startDate, endDate, "Cancelled");

            default -> throw new BusinessRuleException("Rol no válido");
        }

        return appointments.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // ==========================================
    // HELPER: VALIDACIÓN DE PROPIEDAD POR ROL
    // ==========================================
    private void validateOwnership(Appointment appointment, User currentUser) throws AccessDeniedException {
        switch (currentUser.getRole()) {
            case CLIENT -> {
                if (!appointment.getClient().getUser().getEmail().equals(currentUser.getEmail())) {
                    throw new AccessDeniedException("No tienes permiso para modificar la cita de otro cliente.");
                }
            }
            case BARBER -> {
                if (!appointment.getBarber().getUser().getEmail().equals(currentUser.getEmail())) {
                    throw new AccessDeniedException("No tienes permiso para modificar citas de otro barbero.");
                }
            }
            case ADMIN -> {
                // El ADMIN administra la barbería completa
                boolean belongsToAdminBarbershop = appointment.getBarbershop().getUsers().stream()
                        .anyMatch(u -> u.getEmail().equals(currentUser.getEmail()));

                if (!belongsToAdminBarbershop) {
                    throw new AccessDeniedException("No tienes permiso para gestionar citas de otra barbería.");
                }
            }
          }
        }

        // ==========================================
        // MÉTODO AUXILIAR PARA VALIDAR OVERLAP EN JAVA
        // ==========================================
        private void validateOverlap(Long barberId, LocalDate date, LocalTime newStart, LocalTime newEnd, Long excludeAppointmentId) {
    
         // 1. Obtener todas las citas activas del barbero para ese día
        List<Appointment> dailyAppointments = appointmentRepository
            .findActiveAppointmentsByBarberAndDate(barberId, date);

        // 2. Evaluar solapamiento en memoria
        for (Appointment existing : dailyAppointments) {
        // Si estamos reagendando, ignoramos la cita que estamos editando
                if (excludeAppointmentId != null && existing.getId().equals(excludeAppointmentId)) {
                 continue;
                }

        // Calcular cuándo termina la cita existente
        LocalTime existingStart = existing.getStartTime();
        LocalTime existingEnd = existingStart.plusMinutes(existing.getService().getDurationMinutes());

        // La regla de oro del solapamiento
        boolean overlaps = newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);

        if (overlaps) {
            throw new BusinessRuleException(
                "El barbero seleccionado no tiene disponibilidad. Conflicto con la cita de las " + existingStart );
                }
        }
        }

    // ==========================================
    // MAPEO DTO
    // ==========================================
    private AppointmentResponseDTO mapToResponseDTO(Appointment appointment) {
        AppointmentResponseDTO response = new AppointmentResponseDTO();
        response.setId(appointment.getId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setStatus(appointment.getStatus());

        response.setBarbershopId(appointment.getBarbershop().getBarbershopId());
        response.setClientName(appointment.getClient().getFullName());
        response.setBarberName(appointment.getBarber().getName());
        response.setServiceName(appointment.getService().getName());
        response.setPrice(appointment.getService().getPrice());

        return response;
    }
}