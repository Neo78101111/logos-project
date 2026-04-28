package com.logos.dto;


import lombok.Data;

@Data
public class DayEvaluationResponseDTO { // для отображения оценки

    private Long id;
    private Integer daySatisfactionScore;
    private Integer wisdomScore;
    private Integer courageScore;
    private Integer temperanceScore;
    private Integer justiceScore;
    private String comment;
}
