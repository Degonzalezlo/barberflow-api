package com.barberflow.modules.users.application.services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.barberflow.modules.users.application.dtos.ServiceRequestDTO;
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

    public ServiceEntity createService(ServiceRequestDTO dto) {
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

        return serviceRepository.save(service);
    }

    public List<ServiceEntity> getServicesByBarbershop(Long barbershopId) {
        return serviceRepository.findByBarbershopBarbershopId(barbershopId);
    }
    
}
