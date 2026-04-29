package com.barberflow.modules.users.application.services;

import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BarbershopService {

    @Autowired
    private IBarbershopRepository barbershopRepository;

    // 1. Lógica para registrar una nueva barbería
    @Transactional
    public Barbershop registerBarbershop(Barbershop barbershop) {
        // REGLA DE NEGOCIO: No pueden existir dos barberías con el mismo NIT
        if (barbershopRepository.findByNit(barbershop.getNit()).isPresent()) {
            throw new RuntimeException("Error: Ya existe una barbería registrada con el NIT: " + barbershop.getNit());
        }

        // REGLA DE NEGOCIO: Toda barbería nueva empieza como "Activa" por defecto
        barbershop.setIsActive(true);

        return barbershopRepository.save(barbershop);
    }

    // 2. Lógica para buscar por ID (útil para el perfil de la barbería)
    public Optional<Barbershop> getBarbershopById(Long id) {
        return barbershopRepository.findById(id);
    }

    // 3. Lógica para listar todas (Solo para ti como SuperAdmin)
    public List<Barbershop> getAllBarbershops() {
        return barbershopRepository.findAll();
    }
}