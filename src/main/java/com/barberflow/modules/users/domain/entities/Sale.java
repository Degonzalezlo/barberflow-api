package com.barberflow.modules.users.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales" , indexes = @Index(name = "idx_sale_tenant_date", columnList = "barbershop_id, sale_date")) // Índice para optimizar consultas por barbería y fecha de venta
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false) // <--- ¡Esto es clave para ti!
    private Barbershop barbershop;

    // Relación con el Producto (Muchos ventas pueden tener el mismo producto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Relación con el Cliente (Opcional, según tu SQL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    // Relación con la Cita (Opcional, por si la venta fue durante un servicio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(nullable = false)
    @Positive(message = "La cantidad debe ser un mayor a 0")
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "sale_date", updatable = false)
    @Builder.Default
    private LocalDateTime saleDate = LocalDateTime.now();
}