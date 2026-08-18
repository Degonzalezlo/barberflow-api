package com.barberflow.modules.users.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.ServiceEntity;


@Repository
public interface IServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
    
    List<ServiceEntity> findByBarbershopBarbershopId(Long barbershopId);

    boolean existsByNameAndBarbershopBarbershopId(String name, Long barbershopId);
}
