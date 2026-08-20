package com.barberflow.modules.users.infrastructure.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.domain.repositories.IUserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        // Verifica si ya existe un usuario con rol SUPER_ADMIN o con el email configurado
        if (!userRepository.existsByEmail(superAdminEmail)) {
            User superAdmin = User.builder()
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .role(UserRole.SUPER_ADMIN)
                    .isActive(true)
                    .build();

            userRepository.save(superAdmin);
            log.info("✅ Account SUPER_ADMIN successfully initialized with email: {}", superAdminEmail);
        } else {
            log.info("ℹ️ Account SUPER_ADMIN already exists in database. Skipping initialization.");
        }
    }
}
