package com.logos.config;


import com.logos.entity.User;
import com.logos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminIfNotExists();
        createTestUserIfNotExists();
    }

    private void createAdminIfNotExists() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .deathDate(LocalDate.of(2100, 1, 1))
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("Создан администратор: admin / Admin123!");
        }
    }

    private void createTestUserIfNotExists() {
        if (userRepository.findByUsername("testuser").isEmpty()) {
            User testUser = User.builder()
                    .username("testuser")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("User123!"))
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .deathDate(LocalDate.of(2100, 1, 1))
                    .role(User.Role.USER)
                    .active(true)
                    .build();

            userRepository.save(testUser);
            log.info("Создан тестовый пользователь: testuser / User123!");
        }
    }
}
