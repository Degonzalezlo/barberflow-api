package com.barberflow.modules.users.application.services;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;

import com.barberflow.modules.users.application.dtos.AdminUserResponseDTO;
import com.barberflow.modules.users.application.dtos.BarbershopResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateAdminDTO;
import com.barberflow.modules.users.application.dtos.CreateBarbershopDTO;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final IBarbershopRepository barbershopRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

   @Transactional
    public BarbershopResponseDTO createBarbershop(CreateBarbershopDTO dto) {
    if (barbershopRepository.existsByNit(dto.getNit().trim())) {
        throw new BusinessRuleException("Ya existe una barbería registrada con el NIT: " + dto.getNit());
    }

    Barbershop barbershop = new Barbershop();
    barbershop.setNit(dto.getNit().trim());
    barbershop.setBusinessName(dto.getBusinessName().trim());
    barbershop.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);
    barbershop.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
    
    if (dto.getSubscriptionPlan() != null) {
        barbershop.setSubscriptionPlan(dto.getSubscriptionPlan());
    } // Si viene null, la entidad conservará el valor por defecto (SubscriptionPlan.FREE)

    Barbershop savedShop = barbershopRepository.save(barbershop);
    return BarbershopResponseDTO.fromEntity(savedShop);
    }


@Transactional
public AdminUserResponseDTO createAdminUser(CreateAdminDTO dto) {
    String cleanEmail = dto.getEmail().trim().toLowerCase();

    if (userRepository.existsByEmail(cleanEmail)) {
        throw new BusinessRuleException("El email '" + cleanEmail + "' ya se encuentra registrado.");
    }

    Barbershop shop = barbershopRepository.findById(dto.getBarbershopId())
            .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + dto.getBarbershopId()));

    User adminUser = User.builder()
            .email(cleanEmail)
            .password(passwordEncoder.encode(dto.getPassword()))
            .role(UserRole.ADMIN)
            .barbershop(shop)
            .isActive(true)
            .build();

    User savedUser = userRepository.save(adminUser);
    return AdminUserResponseDTO.fromEntity(savedUser);
    }
}