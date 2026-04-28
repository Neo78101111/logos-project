package com.logos.service;


import com.logos.dto.LifeCountdownDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LifeCountdownService {

    /**
     * Рассчитать оставшееся время жизни
     */
    public LifeCountdownDTO calculateRemainingLife(UserResponseDTO user) {
        if (user == null || user.getDeathDate() == null) {
            return LifeCountdownDTO.empty();
        }

        LocalDate today = LocalDate.now();
        LocalDate deathDate = user.getDeathDate();

        // Если дата смерти уже прошла
        if (deathDate.isBefore(today)) {
            return LifeCountdownDTO.expired();
        }

        // Рассчитываем Period между датами
        Period period = Period.between(today, deathDate);

        // Дополнительно считаем точные дни
        long totalDays = ChronoUnit.DAYS.between(today, deathDate);
        long weeks = totalDays / 7;

        return LifeCountdownDTO.builder()
                .years(period.getYears())
                .months(period.getMonths())
                .days(period.getDays())
                .totalDays(totalDays)
                .weeks(weeks)
                .isExpired(false)
                .build();
    }

    /**
     * Получить форматированную строку для отображения
     */
    public String getFormattedCountdown(UserResponseDTO user) {
        LifeCountdownDTO countdown = calculateRemainingLife(user);

        if (countdown.getIsExpired()) {
            return "Время истекло";
        }

        if (countdown.getYears() > 0) {
            return String.format("%d лет, %d месяцев, %d дней",
                    countdown.getYears(), countdown.getMonths(), countdown.getDays());
        } else if (countdown.getMonths() > 0) {
            return String.format("%d месяцев, %d дней",
                    countdown.getMonths(), countdown.getDays());
        } else {
            return String.format("%d дней", countdown.getDays());
        }
    }

    /**
     * Рассчитать процент прожитой жизни
     */
    public double calculateLifePercentage(UserResponseDTO user) {
        if (user == null || user.getBirthDate() == null || user.getDeathDate() == null) {
            return 0.0;
        }

        LocalDate birthDate = user.getBirthDate();
        LocalDate deathDate = user.getDeathDate();
        LocalDate today = LocalDate.now();

        // Общая продолжительность жизни в днях
        long totalLifeDays = ChronoUnit.DAYS.between(birthDate, deathDate);

        // Прожитые дни
        long livedDays = ChronoUnit.DAYS.between(birthDate, today);

        // Процент
        return ((double) livedDays / totalLifeDays) * 100;
    }
}
