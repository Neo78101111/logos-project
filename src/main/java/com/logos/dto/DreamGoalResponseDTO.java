package com.logos.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DreamGoalResponseDTO {
    private Long id;
    private String title;
    private LocalDate deadline;
    private boolean completed;
}