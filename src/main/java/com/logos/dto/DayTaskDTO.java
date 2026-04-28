package com.logos.dto;

import com.logos.entity.DayTask;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DayTaskDTO { // для создания/обновления задачи дня

    @NotBlank(message = "Название задачи не может быть пустым")
    @Size(max = 200, message = "Название задачи не должно превышать 200 символов")
    private String title;
    private String description;
    private DayTask.Priority priority = DayTask.Priority.MEDIUM;
    private boolean completed = false;

}
