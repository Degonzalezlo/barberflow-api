package com.barberflow.modules.users.domain.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.barberflow.modules.users.domain.entities.Client;
import org.springframework.stereotype.Repository;

@Repository
public interface IClientRepository extends JpaRepository <Client, Long> {

    // Spring genera el SQL: SELECT * FROM clients WHERE email = ?
    Optional<Client> findByEmail(String email);

    // Para buscar por teléfono (el campo que indexamos)
    Optional<Client> findByPhone(String phone);
    
    // Para saber si ya existe antes de crearlo
    boolean existsByEmail(String email);
    
}
