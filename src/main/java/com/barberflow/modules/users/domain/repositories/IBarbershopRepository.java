package com.barberflow.modules.users.domain.repositories;


import com.barberflow.modules.users.domain.entities.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IBarbershopRepository extends JpaRepository<Barbershop, Long> {

    Optional<Barbershop> findByNit(String nit);

    boolean existsByNit(String nit);

    Optional<Barbershop> findByBusinessNameContainingIgnoreCase(String name);


}
