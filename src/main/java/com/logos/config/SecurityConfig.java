package com.logos.config;

import com.logos.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Настройка сессий
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                )

                .authorizeHttpRequests(auth -> auth
                        // Статические ресурсы (CSS, JS, изображения)
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        // HTML страницы и формы (публичные)
                        .requestMatchers(
                                "/",                    // welcome
                                "/welcome",            // welcome с формами
                                "/register"            // обработка формы регистрации
                        ).permitAll()

                        // REST API эндпоинты (публичные)
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/users/username-availability",
                                "/api/users/email-availability"
                        ).permitAll()

                        // Административные эндпоинты
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")


                        .requestMatchers("/uploads/**").authenticated()

                        // Всё остальное требует аутентификации
                        .anyRequest().authenticated()
                )

                // Настройка формы входа
                .formLogin(form -> form
                        .loginPage("/welcome")              // Страница с формой
                        .loginProcessingUrl("/login")       // POST обработка формы
                        .defaultSuccessUrl("/calendar", true) // После успешного входа
                        .failureUrl("/welcome?error=true")  // При ошибке
                        .usernameParameter("username")      // Имя поля username
                        .passwordParameter("password")      // Имя поля password
                        .permitAll()                        // Доступно всем
                )

                // Настройка выхода
                .logout(logout -> logout
                        .logoutUrl("/logout")               // POST для выхода
                        .logoutSuccessUrl("/welcome")       // После выхода
                        .invalidateHttpSession(true)        // Удалить сессию
                        .deleteCookies("JSESSIONID")        // Удалить куки
                        .permitAll()                        // Доступно всем
                )

                .userDetailsService(customUserDetailsService)

                // Обработка ошибок доступа
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied") // Страница 403
                );

        return http.build();
    }
}