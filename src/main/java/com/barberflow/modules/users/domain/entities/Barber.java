package com.barberflow.modules.users.domain.entities;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "barbers", indexes = @Index(name = "idx_barber_tenant", columnList = "barbershop_id")) // Indice para conocer Barbers por barbería
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barber_id")
    private Long barberId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user; // Conexión obligatoria al login

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop; // Barbería a la que pertenece el barbero    

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", unique = true, nullable = false, length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "service_commission_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal serviceCommissionRate = new BigDecimal("0.60"); // 60%

    @Column(name = "product_commission_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal productCommissionRate = new BigDecimal("0.00"); // 0%

   
}
