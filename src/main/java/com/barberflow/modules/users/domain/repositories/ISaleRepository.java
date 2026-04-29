package com.barberflow.modules.users.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barberflow.modules.users.domain.entities.Sale;

@Repository
public interface ISaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByBarbershopBarbershopIdAndSaleDateBetween(Long barbershopId, java.time.LocalDateTime start, java.time.LocalDateTime end);

}
