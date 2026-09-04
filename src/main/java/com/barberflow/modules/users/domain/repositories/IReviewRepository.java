package com.barberflow.modules.users.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.Review;

@Repository
public interface IReviewRepository extends JpaRepository<Review , Long> {

    // Ver todas las reseñas de una barbería (Ordenadas por la más reciente)
    List<Review> findByBarbershopBarbershopIdOrderByCreatedAtDesc(Long id);

    Optional<Review> findByAppointmentId(Long appointmentId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.barbershop.id = :barbershopId")
    Double findAverageRatingByBarbershopId(@Param("barbershopId") Long barbershopId);
}
    

