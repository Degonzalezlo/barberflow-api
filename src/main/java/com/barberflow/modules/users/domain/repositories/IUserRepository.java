package com.barberflow.modules.users.domain.repositories;



import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByBarbershopBarbershopId(Long barbershopId);
    
    Boolean existsByEmail(String email);

    Boolean existsByBarbershopAndRole(Barbershop barbershop, UserRole role);

    Optional<User> findByUserIdAndBarbershopBarbershopId(Long userId, Long barbershopId);

    
}
