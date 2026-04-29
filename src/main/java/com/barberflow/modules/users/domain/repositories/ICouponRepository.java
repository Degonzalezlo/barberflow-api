package com.barberflow.modules.users.domain.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.Coupon;

@Repository
public interface ICouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeAndBarbershopBarbershopId(String code, Long barbershopId);
    
}
