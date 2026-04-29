package com.barberflow.modules.users.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory" , indexes = @Index(name = "idx_product_tenant", columnList = "barbershop_id")) // Indice para revisar inventario por barbería
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = true) // <--- ¡Esto es clave!
    private Barbershop barbershop;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @PositiveOrZero(message = "La cantidad en stock no puede ser negativa")
    private Integer stockQuantity;

    @Column(length = 50)
    private String category;

    @Column(name = "last_restock")
    private LocalDateTime lastRestock;

    // Métodos de negocio (Rich Domain Model)
    public boolean hasStock() {
        return this.stockQuantity > 0;
    }

    public void reduceStock(int quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        }
    }
}