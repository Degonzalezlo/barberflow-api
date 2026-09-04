package com.barberflow.modules.users.domain.entities;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Entity
@Table(name = "users" , indexes = @Index(name = "idx_user_email", columnList = "email")) // Índice para optimizar búsquedas por email
@AllArgsConstructor 
@NoArgsConstructor
@Getter
@Setter
public class User implements org.springframework.security.core.userdetails.UserDetails { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = true) // <--- superusuario administra todo el sistema, no pertenece a una barbería específica
    private Barbershop barbershop;

    public void setBarbershop(Barbershop barbershop) {
        this.barbershop = barbershop;
    }

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // ADMIN, BARBER, CLIENT


    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    // ---  MÉTODOS OBLIGATORIOS DE SPRING SECURITY ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Esto le pone la etiqueta de rol al token (ej: ROLE_ADMIN)
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return email; // Spring usará tu email como el identificador principal
    }

    @Override
    public String getPassword() {
        return password; // Le decimos a Spring dónde está la clave encriptada
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // La cuenta no expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // La cuenta no está bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Las credenciales no expiran
    }

    @Override
    public boolean isEnabled() {
        return isActive; // Si desactivas al usuario, el token deja de funcionar
    }
    


    
}
