package com.barberflow.modules.users.domain.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.Coupon;

@Repository
public interface ICouponRepository extends JpaRepository<Coupon, Long> {

    @Query("SELECT COUNT(c) > 0 FROM Coupon c WHERE UPPER(c.code) = UPPER(:code) AND c.barbershop.barbershopId = :barbershopId")
    boolean existsByCodeAndBarbershopId(@Param("code") String code, @Param("barbershopId") Long barbershopId);

    @Query("SELECT COUNT(c) > 0 FROM Coupon c WHERE UPPER(c.code) = UPPER(:code) AND c.barbershop IS NULL")
    boolean existsByCodeAndBarbershopIsNull(@Param("code") String code);
}
