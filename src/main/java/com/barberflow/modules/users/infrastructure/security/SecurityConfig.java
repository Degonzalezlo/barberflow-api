package com.barberflow.modules.users.infrastructure.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para poder usar Postman sin tokens complejos
            .authorizeHttpRequests(auth -> auth
                // Permitimos el acceso público a la ruta de registro
                .requestMatchers("/api/users/register").permitAll() 

                // Solo los ADMIN pueden ver ventas y gestionar barberos
                .requestMatchers("/api/sales/**").hasRole("ADMIN")
                .requestMatchers("/api/barbers/**").hasRole("ADMIN")
            
            // Clientes y Admin pueden ver servicios y agendar
                .requestMatchers("/api/services/**").hasAnyRole("ADMIN", "CLIENT")
                .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "CLIENT")
                // Cualquier otra ruta de la API requerirá autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // Este Bean es el que usa tu UserService para encriptar las claves
        return new BCryptPasswordEncoder();
    }
}
