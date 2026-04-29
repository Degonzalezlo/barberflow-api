package com.barberflow.modules.users.domain.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.Review;

@Repository
public interface IReviewRepository extends JpaRepository<Review , Long> {

    // Ver todas las reseñas de una barbería (Ordenadas por la más reciente)
    List<Review> findByBarbershopBarbershopIdOrderByCreatedAtDesc(Long id);
    
}
