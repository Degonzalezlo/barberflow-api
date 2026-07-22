package com.barberflow.modules.users.infrastructure.presentation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.barberflow.modules.users.application.dtos.ServiceRequestDTO;
import com.barberflow.modules.users.application.dtos.ServiceResponseDTO;
import com.barberflow.modules.users.application.services.ServiceEntityService;
import com.barberflow.modules.users.domain.entities.ServiceEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceEntityController {

    private final ServiceEntityService serviceEntityService;

    @PostMapping("/create")
    public ResponseEntity<ServiceResponseDTO> createService(@RequestBody ServiceRequestDTO dto) {
            ServiceResponseDTO response = serviceEntityService.createService(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/shop/{barbershopId}")
    public ResponseEntity<List<ServiceResponseDTO>> listServices(@PathVariable Long barbershopId) {
        List<ServiceResponseDTO> services = serviceEntityService.getServicesByBarbershop(barbershopId);
        return ResponseEntity.ok(services);
    }
}
