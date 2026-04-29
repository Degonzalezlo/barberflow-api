package com.barberflow.modules.users.domain.repositories;

import com.barberflow.modules.users.domain.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> { 

List<Appointment> findByBarbershopBarbershopIdAndAppointmentDateBetween(Long id, LocalDateTime start, LocalDateTime end);

}
