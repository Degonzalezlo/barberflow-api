package com.barberflow.modules.users.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.barberflow.modules.users.application.dtos.ReviewDTOs.CreateReviewRequest;
import com.barberflow.modules.users.application.dtos.ReviewDTOs.ReviewResponse;
import com.barberflow.modules.users.domain.entities.Appointment;
import com.barberflow.modules.users.domain.entities.Review;
import com.barberflow.modules.users.domain.repositories.IAppointmentRepository;
import com.barberflow.modules.users.domain.repositories.IReviewRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final IReviewRepository reviewRepository;
    private final IAppointmentRepository appointmentRepository;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if(!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Solo se pueden crear reseñas para citas completadas");
        }

        if (reviewRepository.findByAppointmentId(request.appointmentId()).isPresent()) {
            throw new IllegalStateException("Esta cita ya cuenta con una reseña registrada");
        }

        Review review = Review.builder()
                .barbershop(appointment.getBarbershop())
                .appointment(appointment)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByBarbershop(Long barbershopId) {
        return reviewRepository.findByBarbershopBarbershopIdOrderByCreatedAtDesc(barbershopId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ReviewResponse mapToResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBarbershop().getBarbershopId(),
                review.getAppointment().getId(),
                review.getAppointment().getClient() != null ? review.getAppointment().getClient().getFullName() : "Cliente General",
                review.getAppointment().getBarber().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
