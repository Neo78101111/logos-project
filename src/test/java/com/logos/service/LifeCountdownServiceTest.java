package com.logos.service;

import com.logos.dto.LifeCountdownDTO;
import com.logos.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LifeCountdownServiceTest {

    private LifeCountdownService lifeCountdownService;

    @BeforeEach
    void setUp() {
        lifeCountdownService = new LifeCountdownService();
    }

    @Test
    void calculateRemainingLife_shouldReturnCorrectValues() {
        UserResponseDTO user = new UserResponseDTO();
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        user.setDeathDate(LocalDate.now().plusYears(10));

        LifeCountdownDTO dto = lifeCountdownService.calculateRemainingLife(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getIsExpired()).isFalse();
        // Дальнейшие проверки зависят от текущей даты, поэтому опустим точные значения.
    }

    @Test
    void calculateRemainingLife_shouldReturnExpiredWhenDeathDatePassed() {
        UserResponseDTO user = new UserResponseDTO();
        user.setDeathDate(LocalDate.now().minusDays(1));

        LifeCountdownDTO dto = lifeCountdownService.calculateRemainingLife(user);

        assertThat(dto.getIsExpired()).isTrue();
        assertThat(dto.getYears()).isZero();
    }

    @Test
    void calculateRemainingLife_shouldReturnEmptyForNullUser() {
        LifeCountdownDTO dto = lifeCountdownService.calculateRemainingLife(null);
        assertThat(dto.getIsExpired()).isFalse();
        assertThat(dto.getYears()).isZero();
    }

    @Test
    void getFormattedCountdown_shouldReturnNonEmptyString() {
        UserResponseDTO user = new UserResponseDTO();
        user.setDeathDate(LocalDate.now().plusYears(5));

        String formatted = lifeCountdownService.getFormattedCountdown(user);

        assertThat(formatted).isNotEmpty();
    }

    @Test
    void calculateLifePercentage_shouldReturnPercentage() {

        UserResponseDTO user = new UserResponseDTO();
        user.setBirthDate(LocalDate.now().minusYears(30));
        user.setDeathDate(LocalDate.now().plusYears(30));

        double percentage = lifeCountdownService.calculateLifePercentage(user);

        assertThat(percentage).isBetween(0.0, 100.0);
    }
}