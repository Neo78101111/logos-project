package com.logos.dto;

import com.logos.entity.Day;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DayResponseDTO {
    private Long id;
    private LocalDate date;
    private Day.TemporalStatus temporalStatus;
    private List<DayTaskResponseDTO> tasks;
    private DayEvaluationResponseDTO evaluation;
    // Вычисляемые поля для UI
    private boolean canEdit;
    private boolean canEvaluate;
}
