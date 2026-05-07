package com.barberflow.modules.users.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "services" , indexes = @Index(name = "idx_service_tenant", columnList = "barbershop_id")) // Índice para optimizar consultas por barbería
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id" )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false) // <--- ¡Esto es clave para ti!
    private Barbershop barbershop;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @Positive(message = "El precio debe ser un mayor a 0")
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    @Positive(message = "La duración debe ser un mayor a 0")
    private Integer durationMinutes;
}