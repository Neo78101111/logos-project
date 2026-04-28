package com.logos.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CalendarMonthDTO {

    //Данные дней
    private int year;
    private int month;
    private String monthName;
    private LocalDate today;
    private List<DayResponseDTO> days;

    //Статистика
    private int totalDays;
    private int evaluatedDays;
    private double averageSatisfaction;
    private int completedTasks;
    private int totalTasks;

    //Навигация
    private LocalDate previousMonth;
    private LocalDate nextMonth;
}