package com.barberflow.modules.users.domain.repositories;

import com.barberflow.modules.users.domain.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
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
/**
     * PostgreSQL Compatible:
     * Busca TODAS las citas de ese barbero en ese día, para luego filtrarlas en Java 
     * o hacer un cruce simple.
     * La forma más limpia en Postgres sin funciones nativas engorrosas es extraer 
     * la lista del día y evaluar el overlap en un método default o en el Service.
     */
    @Query("""
        SELECT a 
        FROM Appointment a 
        WHERE a.barber.barberId = :barberId 
          AND a.appointmentDate = :date 
          AND a.status != 'Cancelled'
    """)
    List<Appointment> findActiveAppointmentsByBarberAndDate(
        @Param("barberId") Long barberId,
        @Param("date") LocalDate date
    );
}