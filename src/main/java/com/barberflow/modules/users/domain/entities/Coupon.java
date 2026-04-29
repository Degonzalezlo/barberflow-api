package com.barberflow.modules.users.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupons", indexes = @Index(name = "idx_unique_coupon_tenant", columnList = "barbershop_id")) // Índice para evitar duplicados de código por barbería
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = true)
    private Barbershop barbershop;


    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Un pequeño método de negocio "The Man" style:
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
}