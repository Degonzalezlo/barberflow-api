package com.barberflow.modules.users.domain.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "barbershops")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Barbershop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long barbershopId;

    @Column(unique = true, nullable = false)
    private String nit;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    private String address;
    private String phone;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // RELACIONES (El "Abanico" de la barbería)
    
    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<User> users;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<ServiceEntity> services;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Sale> sales;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Coupon> coupons;

    
}
