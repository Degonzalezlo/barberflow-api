package com.barberflow.modules.users.application.services;


import com.barberflow.modules.users.application.dtos.AppointmentRequestDTO;
import com.barberflow.modules.users.application.dtos.AppointmentResponseDTO;
import com.barberflow.modules.users.domain.entities.*;
import com.barberflow.modules.users.domain.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        // 1. Validar que la Barbería existe
        Barbershop barbershop = barbershopRepository.findById(dto.getBarbershopId())
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        // 2. Validar que el Cliente existe
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 3. Validar que el Barbero existe
        Barber barber = barberRepository.findById(dto.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

        // 4. Validar que el Servicio existe
        ServiceEntity service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        // [Regla de Negocio] Validar que el barbero trabaje en esa barbería
        if (!barber.getBarbershop().getBarbershopId().equals(barbershop.getBarbershopId())) {
            throw new RuntimeException("El barbero no pertenece a esta barbería");
        }

        // 5. Construir la entidad usando tu Builder
        Appointment appointment = Appointment.builder()
                .barbershop(barbershop)
                .client(client)
                .barber(barber)
                .service(service)
                .appointmentDate(dto.getAppointmentDate())
                .startTime(dto.getStartTime())
                .status("Scheduled") // Valor por defecto
                .build();

        // 6. Guardar en la base de datos
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 7. Retornar el ResponseDTO mapeado de forma limpia
        return mapToResponseDTO(savedAppointment);
    }

    @Transactional
        public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                        .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
                // Cambiar el estado a "Cancelled"
                appointment.setStatus("Cancelled");
                Appointment updatedAppointment = appointmentRepository.save(appointment);
        
                return mapToResponseDTO(updatedAppointment);
        }

     @Transactional
     public AppointmentResponseDTO updateAppointment(Long appointmentId,AppointmentRequestDTO dto) {
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        Barber newBarber = barberRepository.findById(dto.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));
                
        ServiceEntity newService = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));     

        // Actualizar la fecha y hora de la cita
        appointment.setBarber(newBarber);
        appointment.setService(newService);
        appointment.setStartTime(dto.getStartTime());
        appointment.setAppointmentDate(dto.getAppointmentDate());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return mapToResponseDTO(updatedAppointment);
     }

    public List<AppointmentResponseDTO> getAgendaForAuthenticatedUser(String email, LocalDate startDate, LocalDate endDate) {
        // Buscar usuario por email
        User user = IUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));  

        List<Appointment> appointments;
        UserRole role = user.getRole();

        switch (role) {
        case BARBER -> appointments = appointmentRepository
                .findByBarberUserEmailAndAppointmentDateBetweenAndStatusNot(email, startDate, endDate, "Cancelled");
                
        case CLIENT -> appointments = appointmentRepository
                .findByClientUserEmailAndAppointmentDateBetweenAndStatusNot(email, startDate, endDate, "Cancelled");
                
        case ADMIN -> appointments = appointmentRepository
                .findByBarbershopUsersEmailAndAppointmentDateBetweenAndStatusNot(email, startDate, endDate, "Cancelled");
                
        default -> throw new RuntimeException("Invalid role");
        }

        return appointments.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // Método auxiliar para el mapeo limpio hacia Postman (Evita JSON infinito)
    private AppointmentResponseDTO mapToResponseDTO(Appointment appointment) {
        AppointmentResponseDTO response = new AppointmentResponseDTO();
        response.setId(appointment.getId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setStatus(appointment.getStatus());
        
        // Extraemos solo los datos planos que el cliente necesita leer
        response.setBarbershopId(appointment.getBarbershop().getBarbershopId());
        response.setClientName(appointment.getClient().getFullName()); // Ajusta según los atributos de tu clase Client
        response.setBarberName(appointment.getBarber().getName());   // Ajusta según tu clase Barber
        response.setServiceName(appointment.getService().getName());
        response.setPrice(appointment.getService().getPrice());
        
        return response;
    }
}
