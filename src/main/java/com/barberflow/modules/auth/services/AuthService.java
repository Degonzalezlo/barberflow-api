package com.barberflow.modules.auth.services;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;
import com.barberflow.modules.users.domain.repositories.IBarberRepository; // Asegúrate de tener este import
import com.barberflow.modules.users.domain.repositories.IClientRepository; // Asegúrate de tener este import
import com.barberflow.modules.users.infrastructure.security.JwtService;
import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.auth.dto.AuthResponse; 
import com.barberflow.modules.auth.dto.LoginRequest; 

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserRepository userRepository;
    private final IBarberRepository barberRepository; 
    private final IClientRepository clientRepository; 
    private final IBarbershopRepository barbershopRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(UserRegistrationDTO registrationDTO) {
    // 1. Validamos email
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
        throw new RuntimeException("Email ya registrado");
        }

    // 2. Crear la entidad base (User)
         User user = User.builder()
            .email(registrationDTO.getEmail())
            .password(passwordEncoder.encode(registrationDTO.getPassword()))
            .role(registrationDTO.getRole())
            .isActive(true)
            .build();

    // 3. Orquestación según el Rol
        switch (registrationDTO.getRole()) {
        case ADMIN, BARBER -> registerStaff(user, registrationDTO); // Ambos necesitan barbería
        case CLIENT -> {
            userRepository.save(user); // El cliente no necesita barbería obligatoria
            registerClient(user, registrationDTO);
        }
        default -> throw new RuntimeException("Rol no válido");
        }

    // 4. Generar Token
        String token = jwtService.createToken(user, registrationDTO.getBarbershopId());
        return AuthResponse.builder().token(token).build();
    }

    // Cambiamos el nombre a algo más genérico como "Staff" (Admin o Barber)
    private void registerStaff(User user, UserRegistrationDTO dto) {
        if (dto.getBarbershopId() == null) {
        throw new RuntimeException("El personal debe estar asociado a una barbería");
        }

        Barbershop shop = barbershopRepository.findById(dto.getBarbershopId())
            .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));
    
    // VALIDACIÓN CLAVE: Ahora sí aplica para el ADMIN
        if (dto.getRole() == UserRole.ADMIN) {
            if (userRepository.existsByBarbershopAndRole(shop, UserRole.ADMIN)) {
            throw new RuntimeException("Esta barbería ya tiene un administrador asignado.");
            }
        }
    
        user.setBarbershop(shop); 
        userRepository.save(user); 

    // Solo creamos perfil de Barbero si el rol es BARBER
        if (dto.getRole() == UserRole.BARBER) {
            Barber barber = Barber.builder()
                .name(dto.getName()) 
                .phone(dto.getPhone()) 
                .user(user) 
                .barbershop(shop) 
                .isActive(true)
                .build();
            barberRepository.save(barber);
        }
    }

    private void registerClient(User user, UserRegistrationDTO dto) {
        // El cliente no tiene barbería asignada obligatoriamente al inicio
        userRepository.save(user);

        Client client = Client.builder()
                .fullName(dto.getName()) 
                .phone(dto.getPhone()) 
                .user(user) 
                .build();

        clientRepository.save(client); 
    }


    public AuthResponse login(LoginRequest request) {
        // 1. Autenticación
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Buscar usuario
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3. Obtener el ID de la barbería si es barbero
        // Nota: Si el User no tiene el campo barbershop, lo sacamos del perfil de barbero si es necesario
        Long barbershopId = (user.getBarbershop() != null) ? user.getBarbershop().getBarbershopId() : null;
        String token = jwtService.createToken(user, barbershopId); // Pasamos el ID de la barbería al token para que esté disponible en el filtro de seguridad
             // Aquí depende de si pusiste el campo barbershop en User o no. 
             // Si no está en User, se deja null o se busca en el perfil.
        

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}