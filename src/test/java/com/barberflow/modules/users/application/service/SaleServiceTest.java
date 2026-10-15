package com.barberflow.modules.users.application.service;



import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barberflow.modules.users.application.dtos.SaleItemRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleRequestDTO;
import com.barberflow.modules.users.application.dtos.SaleResponseDTO;
import com.barberflow.modules.users.application.services.SaleService;
import com.barberflow.modules.users.domain.entities.Appointment;
import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.PaymentMethod;
import com.barberflow.modules.users.domain.entities.Product;
import com.barberflow.modules.users.domain.entities.Sale;
import com.barberflow.modules.users.domain.entities.ServiceEntity;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IAppointmentRepository;
import com.barberflow.modules.users.domain.repositories.IBarberRepository;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IProductRepository;
import com.barberflow.modules.users.domain.repositories.ISaleRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private ISaleRepository saleRepository;
    @Mock private IProductRepository productRepository;
    @Mock private IAppointmentRepository appointmentRepository;
    @Mock private IUserRepository userRepository;
    @Mock private IBarberRepository barberRepository;
    @Mock private IBarbershopRepository barbershopRepository;

    @InjectMocks
    private SaleService saleService;

    private User admin;
    private Barbershop barbershop;
    private Product product;
    private Appointment appointment;
    private Barber barber;
    private ServiceEntity serviceEntity;
 

    @BeforeEach
    void setUp() {
        barbershop = new Barbershop();
        barbershop.setBarbershopId(1L);
        barbershop.setNit("123456789");

        admin = new User();
        admin.setUserId(10L);
        admin.setEmail("admin@barberflow.com");
        admin.setBarbershop(barbershop);

        barber = new Barber();
        barber.setBarberId(2L);
        barber.setName("Carlos El Barbero");

        product = new Product();
        product.setId(100L);
        product.setName("Cera para Cabello");
        product.setPrice(new BigDecimal("30000.00"));
        product.setStockQuantity(10); // Stock inicial

        serviceEntity = new ServiceEntity();
        serviceEntity.setId(50L);
        serviceEntity.setName("Corte de Cabello");
        serviceEntity.setPrice(new BigDecimal("25000.00"));

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setBarbershop(barbershop);
        appointment.setBarber(barber);
        appointment.setService(serviceEntity);
        appointment.setStatus("PENDING");

    }

    @Test
    @DisplayName("Procesar venta con producto exitosamente y reducir stock")
    void processSale_Success_WithProduct() {
        // Given
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setPaymentMethod(PaymentMethod.CASH);
        
        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductId(100L);
        item.setQuantity(2);
        dto.setProducts(List.of(item));

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(productRepository.findByIdAndBarbershopBarbershopId(100L, 1L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale saved = invocation.getArgument(0);
            saved.setId(1000L);
            return saved;
        });

        // When
        SaleResponseDTO response = saleService.processSale(dto, admin.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(8, product.getStockQuantity()); // Verificamos reducción de stock (10 - 2)
        assertEquals(new BigDecimal("60000.00"), response.getTotalPrice()); // 30000 * 2
        verify(productRepository, times(1)).save(product);
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    @DisplayName("Procesar venta de cita completándola correctamente")
    void processSale_Success_WithAppointment() {
        // Given
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setAppointmentId(1L);
        dto.setPaymentMethod(PaymentMethod.TRANSFER);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(appointmentRepository.findByIdAndBarbershopBarbershopId(1L, 1L)).thenReturn(Optional.of(appointment));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale saved = invocation.getArgument(0);
            saved.setId(1001L);
            return saved;
        });

        // When
        SaleResponseDTO response = saleService.processSale(dto, admin.getEmail());

        // Then
        assertNotNull(response);
        assertEquals("COMPLETED", appointment.getStatus()); // Estado cambió a COMPLETED
        assertEquals(new BigDecimal("25000.00"), response.getTotalPrice());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Lanzar IllegalStateException si la cita ya fue cobrada previamente (COMPLETED)")
    void processSale_ThrowsException_WhenAppointmentAlreadyCompleted() {
        // Given
        appointment.setStatus("COMPLETED");
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setAppointmentId(1L);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(appointmentRepository.findByIdAndBarbershopBarbershopId(1L, 1L)).thenReturn(Optional.of(appointment));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> saleService.processSale(dto, admin.getEmail())
        );

        assertEquals("La cita ya ha sido cobrada y finalizada previamente", exception.getMessage());
        verify(saleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar IllegalArgumentException cuando la venta no incluye productos ni citas")
    void processSale_ThrowsException_WhenSaleIsEmpty() {
        // Given
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setPaymentMethod(PaymentMethod.CASH);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> saleService.processSale(dto, admin.getEmail())
        );

        assertEquals("La venta debe incluir al menos una cita o un producto", exception.getMessage());
        verify(saleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar EntityNotFoundException cuando el barbero no pertenece a la sede")
    void processSale_ThrowsException_WhenBarberNotInBarbershop() {
        // Given
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setBarberId(99L);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(barberRepository.findByBarberIdAndBarbershopBarbershopId(99L, 1L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> saleService.processSale(dto, admin.getEmail())
        );

        assertTrue(exception.getMessage().contains("no pertenece a tu sede"));
        verify(saleRepository, never()).save(any());
    }
    @Test
    @DisplayName("Lanzar EntityNotFoundException cuando el producto no existe o no pertenece a la sede")
    void processSale_ThrowsException_WhenProductNotFound() {
    // Given
    SaleRequestDTO dto = new SaleRequestDTO();
    dto.setPaymentMethod(PaymentMethod.CASH);

    SaleItemRequestDTO item = new SaleItemRequestDTO();
    item.setProductId(999L);
    item.setQuantity(1);
    dto.setProducts(List.of(item));

    when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
    when(productRepository.findByIdAndBarbershopBarbershopId(999L, 1L)).thenReturn(Optional.empty());

    // When & Then
    EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> saleService.processSale(dto, admin.getEmail())
    );

    assertTrue(exception.getMessage().contains("no encontrado en tu sede"));
    verify(saleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar excepción cuando la cantidad excede el stock disponible")
    void processSale_ThrowsException_WhenInsufficientStock() {
    // Given
    SaleRequestDTO dto = new SaleRequestDTO();
    dto.setPaymentMethod(PaymentMethod.CASH);

    SaleItemRequestDTO item = new SaleItemRequestDTO();
    item.setProductId(100L);
    item.setQuantity(15); // Hay solo 10 en stock
    dto.setProducts(List.of(item));

    when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
    when(productRepository.findByIdAndBarbershopBarbershopId(100L, 1L)).thenReturn(Optional.of(product));

    // When & Then
    // Captura la excepción genérica lanzada por tu entidad Product (ej. IllegalArgumentException o IllegalStateException)
    Exception exception = assertThrows(
            Exception.class,
            () -> saleService.processSale(dto, admin.getEmail())
    );

    assertNotNull(exception.getMessage());
    verify(saleRepository, never()).save(any());
    verify(productRepository, never()).save(any());
    }
}
