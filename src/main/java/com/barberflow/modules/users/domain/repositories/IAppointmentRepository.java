package com.barberflow.modules.users.domain.repositories;

import com.barberflow.modules.users.domain.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


    @Repository
    public interface IAppointmentRepository extends JpaRepository<Appointment, Long> { 

    // Para el Barbero: Navega de Appointment -> Barber -> Email
    List<Appointment> findByBarberUserEmailAndAppointmentDateBetweenAndStatusNot(
        String email, LocalDate startDate, LocalDate endDate, String status);

    // Para el Cliente: Navega de Appointment -> Client -> Email
    List<Appointment> findByClientUserEmailAndAppointmentDateBetweenAndStatusNot(
        String email, LocalDate startDate, LocalDate endDate, String status);

    // Para el Administrador: Navega de Appointment -> Barbershop -> Users -> Email
    // (O si el Admin tiene relación directa con la Barbería)
    List<Appointment> findByBarbershopUsersEmailAndAppointmentDateBetweenAndStatusNot(
        String email, LocalDate startDate, LocalDate endDate, String status);
}

    
