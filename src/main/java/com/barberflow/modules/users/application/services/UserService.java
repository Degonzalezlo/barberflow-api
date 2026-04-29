package com.barberflow.modules.users.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IBarbershopRepository barbershopRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user, Long barbershopId) {
        // 1. Regla: ¿Email duplicado?
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está en uso.");
        }

        // 2. Regla: ¿Existe la barbería a la que se quiere unir?
        Barbershop shop = barbershopRepository.findById(barbershopId)
            .orElseThrow(() -> new RuntimeException("Barbería no encontrada."));

        // 3. SEGURIDAD: Encriptar la contraseña antes de guardar
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 4. Vincular y Guardar
        user.setBarbershop(shop);
        return userRepository.save(user);
    }
}