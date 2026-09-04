package com.barberflow.modules.users.domain.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.Sale;

@Repository
public interface ISaleRepository extends JpaRepository<Sale, Long> {

    // 1. Fetch all barbershop sales with JOIN FETCH to prevent N+1 issues
    @Query("""
        SELECT DISTINCT s FROM Sale s
        LEFT JOIN FETCH s.barber
        LEFT JOIN FETCH s.appointment
        LEFT JOIN FETCH s.details
        WHERE s.barbershop.barbershopId = :barbershopId
        ORDER BY s.saleDate DESC
    """)
    List<Sale> findByBarbershopBarbershopIdOrderBySaleDateDesc(@Param("barbershopId") Long barbershopId);

    // 2. FIXED: Returned List<Sale> instead of Optional<Sale> to avoid NonUniqueResultException
    @Query("""
        SELECT DISTINCT s FROM Sale s 
        LEFT JOIN FETCH s.details
        WHERE s.barber.barberId = :barberId 
          AND s.barbershop.barbershopId = :barbershopId
        ORDER BY s.saleDate DESC
    """)
    List<Sale> findByBarberIdAndBarbershopBarbershopId(
            @Param("barberId") Long barberId, 
            @Param("barbershopId") Long barbershopId
    );

    // 3. Date range filtering with JOIN FETCH
    @Query("""
        SELECT DISTINCT s FROM Sale s
        LEFT JOIN FETCH s.barber
        LEFT JOIN FETCH s.details
        WHERE s.barbershop.barbershopId = :barbershopId
          AND s.saleDate BETWEEN :startDate AND :endDate
        ORDER BY s.saleDate DESC
    """)
    List<Sale> findByBarbershopBarbershopIdAndSaleDateBetweenOrderBySaleDateDesc(
            @Param("barbershopId") Long barbershopId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate
    );

    // 4. Filter by barber and date range
    @Query("""
        SELECT DISTINCT s FROM Sale s 
        LEFT JOIN FETCH s.details
        WHERE s.barbershop.barbershopId = :barbershopId 
          AND s.barber.barberId = :barberId 
          AND s.saleDate BETWEEN :startDate AND :endDate
        ORDER BY s.saleDate DESC
    """)
    List<Sale> findByBarbershopBarbershopIdAndBarberIdAndSaleDateBetween(
            @Param("barbershopId") Long barbershopId,
            @Param("barberId") Long barberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 5. Commissions report query (working correctly)
    @Query("""
        SELECT 
            b.barberId AS barberId,
            b.name AS barberName,
            (
                SELECT COUNT(DISTINCT s2.appointment.id) 
                FROM Sale s2 
                WHERE s2.barber.barberId = b.barberId 
                  AND s2.barbershop.barbershopId = :barbershopId 
                  AND s2.saleDate BETWEEN :start AND :end 
                  AND s2.appointment IS NOT NULL
            ) AS completedAppointments,
            (
                SELECT COALESCE(SUM(s3.appointment.service.price), 0) 
                FROM Sale s3 
                WHERE s3.barber.barberId = b.barberId 
                  AND s3.barbershop.barbershopId = :barbershopId 
                  AND s3.saleDate BETWEEN :start AND :end
            ) AS totalServices,
            (
                SELECT COALESCE(SUM(sd.subtotal), 0) 
                FROM Sale s4 
                JOIN s4.details sd 
                WHERE s4.barber.barberId = b.barberId 
                  AND s4.barbershop.barbershopId = :barbershopId 
                  AND s4.saleDate BETWEEN :start AND :end
            ) AS totalProducts,
            COALESCE(b.serviceCommissionRate, 0.50) AS serviceRate,
            COALESCE(b.productCommissionRate, 0.10) AS productRate
        FROM Barber b
        WHERE b.barbershop.barbershopId = :barbershopId
    """)
    List<BarberSalesSummaryProjection> getSalesSummaryByBarber(
            @Param("barbershopId") Long barbershopId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    interface BarberSalesSummaryProjection {
        Long getBarberId();
        String getBarberName();
        Long getCompletedAppointments();
        BigDecimal getTotalServices();
        BigDecimal getTotalProducts();
        BigDecimal getServiceRate();
        BigDecimal getProductRate();
    }
}