package com.barberflow.modules.users.domain.repositories;

import com.barberflow.modules.users.domain.entities.Appointment;
import com.barberflow.modules.users.domain.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> { 

    // ✅ Barbero: Appointment -> Barber (b) -> User (u) -> email
    @Query("""
        SELECT a FROM Appointment a 
        JOIN a.barber b 
        JOIN b.user u 
        WHERE u.email = :email 
          AND a.appointmentDate BETWEEN :startDate AND :endDate 
          AND a.status != :status
    """)
    List<Appointment> findByBarberUserEmailAndAppointmentDateBetweenAndStatusNot(
        @Param("email") String email, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate, 
        @Param("status") String status
    );

    // ✅ Cliente: Appointment -> Client (c) -> User (u) -> email
    @Query("""
        SELECT a FROM Appointment a 
        JOIN a.client c 
        JOIN c.user u 
        WHERE u.email = :email 
          AND a.appointmentDate BETWEEN :startDate AND :endDate 
          AND a.status != :status
    """)
    List<Appointment> findByClientUserEmailAndAppointmentDateBetweenAndStatusNot(
        @Param("email") String email, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate, 
        @Param("status") String status
    );

    // ✅ Administrador
    List<Appointment> findByBarbershopUsersEmailAndAppointmentDateBetweenAndStatusNot(
        String email, LocalDate startDate, LocalDate endDate, String status);

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

    Optional<Appointment> findByIdAndBarbershopBarbershopId(Long appointmentId, Long barbershopId);
}