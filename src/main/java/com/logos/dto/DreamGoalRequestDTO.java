package com.logos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DreamGoalRequestDTO {
    @NotBlank
    private String title;

    @NotNull
    private LocalDate deadline;
}