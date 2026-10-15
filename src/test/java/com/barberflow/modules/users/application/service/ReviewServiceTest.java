package com.barberflow.modules.users.application.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barberflow.modules.users.application.dtos.ReviewDTOs.CreateReviewRequest;
import com.barberflow.modules.users.application.dtos.ReviewDTOs.ReviewResponse;
import com.barberflow.modules.users.application.services.ReviewService;
import com.barberflow.modules.users.domain.entities.Appointment;
import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Review;
import com.barberflow.modules.users.domain.repositories.IAppointmentRepository;
import com.barberflow.modules.users.domain.repositories.IReviewRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private IAppointmentRepository appointmentRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Appointment appointment;
    private Barbershop barbershop;
    private CreateReviewRequest request;

    @BeforeEach
    void setUp() {
        barbershop = new Barbershop();
        barbershop.setBarbershopId(1L);
        barbershop.setNit("123456789");

        Barber barber = new Barber();
        barber.setBarberId(1L);
        barber.setName("Juan Perez");

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setBarber(barber);
        appointment.setBarbershop(barbershop);
        appointment.setStatus("COMPLETED");

        request = new CreateReviewRequest(1L, 5, "Excelente servicio");
    }

    @Test
    @DisplayName("Debe crear la reseña exitosamente cuando la cita está COMPLETED")
    void createReview_Success() {
        // Given (Preparación de Mocks)
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(reviewRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());

        Review savedReview = Review.builder()
                .id(100L)
                .barbershop(barbershop)
                .appointment(appointment)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // When (Ejecución del método real)
        ReviewResponse response = reviewService.createReview(request);

        // Then (Aserciones)
        assertNotNull(response);
        verify(appointmentRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).findByAppointmentId(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando la cita no existe")
    void createReview_ThrowsException_WhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(request)
        );

        assertEquals("Cita no encontrada", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException cuando la cita NO está COMPLETED")
    void createReview_ThrowsException_WhenStatusNotCompleted() {
        // Given
        appointment.setStatus("CANCELLED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(request)
        );

        assertEquals("Solo se pueden crear reseñas para citas completadas", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException cuando la cita ya tiene reseña")
    void createReview_ThrowsException_WhenReviewAlreadyExists() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(reviewRepository.findByAppointmentId(1L)).thenReturn(Optional.of(new Review()));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(request)
        );

        assertEquals("Esta cita ya cuenta con una reseña registrada", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }
}