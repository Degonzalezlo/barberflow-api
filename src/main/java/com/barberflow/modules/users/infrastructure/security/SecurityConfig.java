package com.barberflow.modules.users.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable()) // Deshabilitamos CORS para desarrollo local
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para uso con JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 🔐 APIs REST sin estado
            .authorizeHttpRequests(auth -> auth
            // 1. Rutas públicas
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/users/register").permitAll()
            .requestMatchers("/error").permitAll()

            // 2. Exclusivo de SUPER_ADMIN
            .requestMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")

            // 3. Exclusivo de ADMIN de sede
            .requestMatchers("/api/v1/sales/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/barbers/**").hasRole("ADMIN")

            // 4. Roles operativos (ADMIN, CLIENT, BARBER)
            .requestMatchers("/api/v1/services/**").hasAnyRole("ADMIN", "CLIENT", "BARBER")
            .requestMatchers("/api/v1/appointments/**").hasAnyRole("ADMIN", "CLIENT", "BARBER")

            // 5. Cualquier otra ruta requerirá autenticación
            .anyRequest().authenticated()
        )
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
