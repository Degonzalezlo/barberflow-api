package com.barberflow.modules.users.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews" , indexes = @Index(name = "idx_review_tenant", columnList = "barbershop_id")) // Índice para consultar reviews por barbería
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false) // <---
    private Barbershop barbershop;

    // Relación One-to-One con Appointment
    // En la DB es appointment_id, aquí es el objeto completo
    @OneToOne
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointment_id", unique = true, nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}