package com.logos.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifeCountdownDTO {
    private Integer years;
    private Integer months;
    private Integer days;
    private Long totalDays;
    private Long weeks;
    private Boolean isExpired;

    // Создает пустой DTO
    public static LifeCountdownDTO empty() {
        return LifeCountdownDTO.builder()
                .years(0)
                .months(0)
                .days(0)
                .totalDays(0L)
                .weeks(0L)
                .isExpired(false)
                .build();
    }

    // Создает DTO для истекшего времени
    public static LifeCountdownDTO expired() {
        return LifeCountdownDTO.builder()
                .years(0)
                .months(0)
                .days(0)
                .totalDays(0L)
                .weeks(0L)
                .isExpired(true)
                .build();
    }
}
