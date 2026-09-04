package com.barberflow.modules.users.application.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barberflow.modules.users.application.dtos.BarberCommissionDTO;
import com.barberflow.modules.users.application.dtos.SaleItemRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleItemResponseDTO;
import com.barberflow.modules.users.application.dtos.SaleRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleResponseDTO;
import com.barberflow.modules.users.domain.entities.Appointment;
import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Product;
import com.barberflow.modules.users.domain.entities.Sale;
import com.barberflow.modules.users.domain.entities.SaleDetail;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IAppointmentRepository;
import com.barberflow.modules.users.domain.repositories.IBarberRepository;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IProductRepository;
import com.barberflow.modules.users.domain.repositories.ISaleRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor // 👈 Sin @Builder aquí para permitir la inyección de Spring
public class SaleService {

    private final ISaleRepository saleRepository;
    private final IProductRepository productRepository;
    private final IAppointmentRepository appointmentRepository;
    private final IUserRepository userRepository;
    private final IBarberRepository  barberRepository;
    private final IBarbershopRepository barbershopRepository;

    @Transactional
    public SaleResponseDTO processSale(SaleRequestDTO dto, String adminEmail) {
        User admin = getAdminByEmail(adminEmail);
        Barbershop barbershop = admin.getBarbershop();
        Long barbershopId = barbershop.getBarbershopId();

        // 1. Validar Barbero por barber_id si se recibe en el DTO (Opcional)
        Barber barber = null;
        if (dto.getBarberId() != null) {
            barber = barberRepository.findByBarberIdAndBarbershopBarbershopId(dto.getBarberId(), barbershopId)
                    .orElseThrow(() -> new EntityNotFoundException("El barbero con ID " + dto.getBarberId() + " no existe o no pertenece a tu sede"));
        }
        
        // 2. Validar Cliente (Opcional)
        User client = null;
        if (dto.getClientId() != null) {
            client = userRepository.findById(dto.getClientId()).orElse(null);
        }

        Appointment appointment = null;
        BigDecimal totalSaleAmount = BigDecimal.ZERO;

        // 3. Procesar Cita / Servicio (Opcional)
        if (dto.getAppointmentId() != null) {
            appointment = appointmentRepository.findByIdAndBarbershopBarbershopId(dto.getAppointmentId(), barbershopId)
                    .orElseThrow(() -> new EntityNotFoundException("La cita no existe o no pertenece a tu sede"));

            if ("CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                throw new IllegalStateException("No se puede cobrar una cita cancelada");
            }

            if ("COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
                throw new IllegalStateException("La cita ya ha sido cobrada y finalizada previamente");
            }

            totalSaleAmount = totalSaleAmount.add(appointment.getService().getPrice());

            // Asignar el barbero desde la cita si no se pasó explícitamente en el DTO
            if (barber == null && appointment.getBarber() != null) {
                barber = appointment.getBarber(); // 👈 Se asigna directamente la entidad Barber de la cita
            }

            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);
        }

        // 4. Instanciar Cabecera de Venta con la entidad Barber
        Sale sale = Sale.builder()
                .barbershop(barbershop)
                .barber(barber) // 👈 Relación a Barber
                .client(client)
                .appointment(appointment)
                .paymentMethod(dto.getPaymentMethod())
                .totalPrice(BigDecimal.ZERO)
                .build();

        // 5. Procesar Productos / Inventario
        if (dto.getProducts() != null && !dto.getProducts().isEmpty()) {
            for (SaleItemRequestDTO itemDTO : dto.getProducts()) {
                Product product = productRepository.findByIdAndBarbershopBarbershopId(itemDTO.getProductId(), barbershopId)
                        .orElseThrow(() -> new EntityNotFoundException("Producto ID " + itemDTO.getProductId() + " no encontrado en tu sede"));

                // Descontar Stock
                product.reduceStock(itemDTO.getQuantity());
                productRepository.save(product);

                // Congelar precio histórico
                BigDecimal unitPrice = product.getPrice();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                SaleDetail detail = SaleDetail.builder()
                        .product(product)
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(unitPrice)
                        .subtotal(subtotal)
                        .build();

                sale.addDetail(detail);
                totalSaleAmount = totalSaleAmount.add(subtotal);
            }
        }

        // 6. Validar que la venta no esté vacía
        if (totalSaleAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La venta debe incluir al menos una cita o un producto");
        }

        sale.setTotalPrice(totalSaleAmount);
        Sale savedSale = saleRepository.save(sale);

        return mapToResponseDTO(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByBarbershop(String adminEmail) {
        User admin = getAdminByEmail(adminEmail);
        Long barbershopId = admin.getBarbershop().getBarbershopId();

        return saleRepository.findByBarbershopBarbershopIdOrderBySaleDateDesc(barbershopId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByDateRange(String adminEmail, LocalDateTime start, LocalDateTime end) {
        User admin = getAdminByEmail(adminEmail);
        Long barbershopId = admin.getBarbershop().getBarbershopId();

        return saleRepository.findByBarbershopBarbershopIdAndSaleDateBetweenOrderBySaleDateDesc(barbershopId, start, end)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private User getAdminByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + email));
    }

    private SaleResponseDTO mapToResponseDTO(Sale sale) {
        List<SaleItemResponseDTO> itemDTOs = new ArrayList<>();

        // 1. Si la venta incluye una cita, agregamos el servicio a la lista de ítems
        if (sale.getAppointment() != null && sale.getAppointment().getService() != null) {
            itemDTOs.add(SaleItemResponseDTO.builder()
                    .productId(null) // Queda nulo para distinguir que es un servicio
                    .productName("Servicio: " + sale.getAppointment().getService().getName())
                    .quantity(1)
                    .unitPrice(sale.getAppointment().getService().getPrice())
                    .subtotal(sale.getAppointment().getService().getPrice())
                    .build());
        }

        // 2. Mapeamos y agregamos los productos físicos comprados
        if (sale.getDetails() != null && !sale.getDetails().isEmpty()) {
            sale.getDetails().forEach(detail -> 
                itemDTOs.add(SaleItemResponseDTO.builder()
                        .productId(detail.getProduct().getId())
                        .productName(detail.getProduct().getName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .subtotal(detail.getSubtotal())
                        .build())
            );
        }

        // 3. Retornamos el DTO de respuesta con la lista unificada
        return SaleResponseDTO.builder()
                .saleId(sale.getId())
                .barbershopId(sale.getBarbershop().getBarbershopId())
                .barberId(sale.getBarber() != null ? sale.getBarber().getBarberId() : null)
                .barberName(sale.getBarber() != null 
                        ? sale.getBarber().getName() 
                        : "Venta Directa / Mostrador")
                .clientId(sale.getClient() != null ? sale.getClient().getUserId() : null)
                .appointmentId(sale.getAppointment() != null ? sale.getAppointment().getId() : null)
                .totalPrice(sale.getTotalPrice())
                .paymentMethod(sale.getPaymentMethod())
                .saleDate(sale.getSaleDate())
                .items(itemDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BarberCommissionDTO> calculateBarberCommissions(
            String adminEmail, 
            LocalDateTime start, 
            LocalDateTime end
    ) {
        User admin = getAdminByEmail(adminEmail);
        Long barbershopId = admin.getBarbershop().getBarbershopId();

        List<ISaleRepository.BarberSalesSummaryProjection> summaries = 
                saleRepository.getSalesSummaryByBarber(barbershopId, start, end);

        return summaries.stream().map(summary -> {
            BigDecimal servicesTotal = Optional.ofNullable(summary.getTotalServices()).orElse(BigDecimal.ZERO);
            BigDecimal productsTotal = Optional.ofNullable(summary.getTotalProducts()).orElse(BigDecimal.ZERO);

            BigDecimal serviceRate = Optional.ofNullable(summary.getServiceRate()).orElse(new BigDecimal("0.60")); // Default 50% if null
            BigDecimal productRate = Optional.ofNullable(summary.getProductRate()).orElse(new BigDecimal("0.00")); // Default 10% if null

            BigDecimal commissionServices = servicesTotal.multiply(serviceRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal commissionProducts = productsTotal.multiply(productRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalPayout = commissionServices.add(commissionProducts).setScale(2, RoundingMode.HALF_UP);

            return new BarberCommissionDTO(
                    summary.getBarberId(),
                    summary.getBarberName(),
                    summary.getCompletedAppointments(),
                    servicesTotal.setScale(2, RoundingMode.HALF_UP),
                    productsTotal.setScale(2, RoundingMode.HALF_UP),
                    commissionServices,
                    commissionProducts,
                    totalPayout
            );
        }).toList();
    }
}