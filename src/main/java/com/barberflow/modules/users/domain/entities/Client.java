package com.barberflow.modules.users.domain.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "clients", indexes = @Index(name = "idx_client_phone", columnList = "phone")) // Índice para optimizar búsquedas por teléfono
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user; // Aquí está la conexión al login

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", unique = true, nullable = false, length = 20)
    private String phone;

    
}
