package com.barberflow.modules.users.application.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.barberflow.modules.users.application.dtos.ServiceRequestDTO;
import com.barberflow.modules.users.application.dtos.ServiceResponseDTO;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.ServiceEntity;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IServiceEntityRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ServiceEntityService {

    private final IServiceEntityRepository serviceRepository;
    private final IBarbershopRepository barbershopRepository;

    public ServiceResponseDTO createService(ServiceRequestDTO dto) {
        // 1. Validar que la barbería existe
        Barbershop barbershop = barbershopRepository.findById(dto.getBarbershopId())
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        // 2. Mapear DTO a Entity (puedes usar el Builder que pusiste en tu entidad)
        ServiceEntity service = ServiceEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .durationMinutes(dto.getDurationMinutes())
                .barbershop(barbershop)
                .build();

        ServiceEntity savedEntity = serviceRepository.save(service);

        return mapToResponseDTO(savedEntity);
    }

    public List<ServiceResponseDTO> getServicesByBarbershop(Long barbershopId) {
        return serviceRepository.findByBarbershopBarbershopId(barbershopId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ServiceResponseDTO mapToResponseDTO(ServiceEntity entity) {
        ServiceResponseDTO response = new ServiceResponseDTO();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setPrice(entity.getPrice());
        response.setDurationMinutes(entity.getDurationMinutes());
        response.setBarbershopId(entity.getBarbershop().getBarbershopId());
        response.setBarbershopName(entity.getBarbershop().getBusinessName());
        return response;
    }
    
}
