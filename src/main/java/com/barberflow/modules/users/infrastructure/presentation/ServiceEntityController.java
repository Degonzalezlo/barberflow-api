package com.barberflow.modules.users.infrastructure.presentation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberflow.modules.users.application.dtos.CreateServiceDTO;
import com.barberflow.modules.users.application.dtos.ServiceRequestDTO;
import com.barberflow.modules.users.application.dtos.ServiceResponseDTO;
import com.barberflow.modules.users.application.services.ServiceEntityService;
import com.barberflow.modules.users.domain.entities.ServiceEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceEntityController {

    private final ServiceEntityService serviceManagement;

    /**
     * Crear un nuevo servicio de barbería.
     * PROTEGIDO: Solo usuarios con ROL ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceEntity> createService(@Valid @RequestBody CreateServiceDTO dto) {
        ServiceEntity createdService = serviceManagement.createService(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    /**
     * Consultar catálogo de servicios por barbería.
     * LIBRE / PERMITIDO para usuarios autenticados.
     */
    @GetMapping("/barbershop/{barbershopId}")
    public ResponseEntity<List<ServiceEntity>> getServicesByBarbershop(@PathVariable Long barbershopId) {
        return ResponseEntity.ok(serviceManagement.findAllByBarbershop(barbershopId));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        serviceManagement.deactivateService(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}
