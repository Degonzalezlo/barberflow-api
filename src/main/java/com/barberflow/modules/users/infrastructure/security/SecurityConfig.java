package com.barberflow.modules.users.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // 2. Exclusivo de SUPER_ADMIN (Gestión global de la plataforma)
                .requestMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")

                // 3. Exclusivo de ADMIN de sede y SUPER_ADMIN (Gestión de barberos)
                .requestMatchers("/api/v1/barbers/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // 4. Productos: Barbero solo lee (GET); Admin/SuperAdmin alteran (POST, PUT, DELETE)
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAnyRole("ADMIN", "BARBER", "SUPER_ADMIN")
                .requestMatchers("/api/v1/products/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // 5. Ventas: Barbero solo registra en caja (POST); Admin/SuperAdmin consultan historial, editan o anulan (GET, PUT, DELETE)
                .requestMatchers(HttpMethod.POST, "/api/v1/sales/**").hasAnyRole("ADMIN", "BARBER", "SUPER_ADMIN")
                .requestMatchers("/api/v1/sales/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // 6. Servicios (Lectura pública para autenticados, modificación solo para Admins)
                .requestMatchers(HttpMethod.GET, "/api/v1/services/**").hasAnyRole("ADMIN", "BARBER", "CLIENT", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/services/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/services/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/services/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // 7. Citas: Cliente solo crea (POST) y lee sus propias citas (GET); Admin/Barbero/SuperAdmin leen todas las citas (GET) y alteran (PUT, DELETE)
                .requestMatchers("/api/v1/appointments/**").hasAnyRole("ADMIN", "CLIENT", "BARBER", "SUPER_ADMIN")

                // 8. Reseñas: Cliente solo crea (POST); Admin/Barbero/SuperAdmin leen (GET)
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").hasAnyRole("ADMIN", "BARBER", "CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").hasRole("CLIENT")

                // 9. Cualquier otra ruta requerirá autenticación
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
