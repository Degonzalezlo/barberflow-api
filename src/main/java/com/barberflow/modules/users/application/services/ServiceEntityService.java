package com.barberflow.modules.users.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.CreateServiceDTO;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.ServiceEntity;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IServiceEntityRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEntityService {

    private final IServiceEntityRepository serviceRepository;
    private final IBarbershopRepository barbershopRepository;

    @Transactional
    public ServiceEntity createService(CreateServiceDTO dto) {
        // 1. Validar que la barbería existe
        Barbershop shop = barbershopRepository.findById(dto.getBarbershopId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + dto.getBarbershopId()));

        // 2. Validar que no exista un servicio con el mismo nombre en la misma barbería
        if (serviceRepository.existsByNameAndBarbershopBarbershopId(dto.getName(), dto.getBarbershopId())) {
            throw new BusinessRuleException("Ya existe un servicio llamado '" + dto.getName() + "' en esta barbería.");
        }

        // 3. Crear y guardar la entidad
        ServiceEntity service = new ServiceEntity();
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setDurationMinutes(dto.getDurationMinutes());
        service.setBarbershop(shop);
        

        return serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> findAllByBarbershop(Long barbershopId) {
        return serviceRepository.findByBarbershopBarbershopId(barbershopId);
    }

    @Transactional
    public void deactivateService(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId));

        service.setIsActive(false);
        serviceRepository.save(service);
    }
}
    

