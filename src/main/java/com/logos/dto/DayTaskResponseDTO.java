package com.logos.dto;

import com.logos.entity.DayTask;
import lombok.Data;

@Data
public class DayTaskResponseDTO { // для отображения задачи

    private Long id;
    private String title;
    private String description;
    private DayTask.Priority priority;
    private boolean completed;
}
