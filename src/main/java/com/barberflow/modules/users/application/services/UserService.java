package com.barberflow.modules.users.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.users.application.dtos.UserResponseDTO;
import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.domain.repositories.IBarberRepository;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IClientRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final IBarbershopRepository barbershopRepository;
    private final IClientRepository clientRepository;
    private final IBarberRepository barberRepository;
    private final PasswordEncoder passwordEncoder;

    
    @Transactional
    public UserResponseDTO registerUserFromDTO(UserRegistrationDTO dto) {

    // 1. Validar correo duplicado
    if (userRepository.existsByEmail(dto.getEmail())) {
        throw new BusinessRuleException("El correo ya está registrado.");
    }

    // 2. Buscar la barbería (si aplica)
    Barbershop shop = null;
    if (dto.getBarbershopId() != null) {
        shop = barbershopRepository.findById(dto.getBarbershopId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + dto.getBarbershopId()));
    }

    // 3. Crear y guardar las credenciales en la tabla `users`
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setRole(dto.getRole());
    user.setBarbershop(shop);
    user.setIsActive(true);

    User savedUser = userRepository.save(user);

    // Guardar el perfil correspondiente (Client / Barber)
    if (dto.getRole() == UserRole.CLIENT) {
        Client client = new Client();
        client.setUser(savedUser);
        client.setFullName(dto.getName());
        client.setPhone(dto.getPhone());
        clientRepository.save(client);

    } else if (dto.getRole() == UserRole.BARBER) {
        if (shop == null) {
            throw new BusinessRuleException("Es obligatorio asignar una barbería válida para registrar a un barbero.");
        }

        Barber barber = new Barber();
        barber.setUser(savedUser);
        barber.setBarbershop(shop);
        barber.setName(dto.getName());
        barber.setPhone(dto.getPhone());
        barber.setIsActive(true);
        barberRepository.save(barber);
    }

    // Retornamos el DTO en lugar de la entidad directa
    return mapToUserResponseDTO(savedUser);
    }

    @Transactional
    public void deactivateBarber(Long barberId) {
    Barber barber = barberRepository.findById(barberId)
            .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con ID: " + barberId));

    barber.setIsActive(false);

    if (barber.getUser() != null) {
        barber.getUser().setIsActive(false);
    }

    barberRepository.save(barber);
    }
    
    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .barbershopId(user.getBarbershop() != null ? user.getBarbershop().getBarbershopId() : null)
                .barbershopName(user.getBarbershop() != null ? user.getBarbershop().getBusinessName() : null)
                .build();
    }
}