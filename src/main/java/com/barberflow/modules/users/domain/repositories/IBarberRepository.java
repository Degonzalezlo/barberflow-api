package com.barberflow.modules.users.domain.repositories;


import com.barberflow.modules.users.domain.entities.Barber;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


public interface IBarberRepository extends JpaRepository<Barber, Long> {
    
    // Este método es vital para BarberFlow:
    // Permite obtener todos los barberos que trabajan en una sede específica
    List<Barber> findByBarbershopBarbershopId(Long barbershopId);
    
    // También podrías buscar por el ID del usuario vinculado
    Optional<Barber> findByUserUserId(Long userId);

    Optional<Barber> findByBarberIdAndBarbershopBarbershopId(Long barberId, Long barbershopId);
}
