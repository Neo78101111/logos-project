package com.logos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DayEvaluationDTO { // для создания оценки

    @NotNull(message = "Оценка общей удовлетворенности обязательна")
    @Min(value = 1, message = "Оценка должна быть от 1 до 10")
    @Max(value = 10, message = "Оценка должна быть от 1 до 10")
    private Integer daySatisfactionScore; // общая удовлетворенность днем

    @NotNull(message = "Оценка Мудрости обязательна")
    @Min(value = 1, message = "Оценка должна быть от 1 до 10")
    @Max(value = 10, message = "Оценка должна быть от 1 до 10")
    private Integer wisdomScore; // оценка Мудрости

    @NotNull(message = "Оценка Мужества обязательна")
    @Min(value = 1, message = "Оценка должна быть от 1 до 10")
    @Max(value = 10, message = "Оценка должна быть от 1 до 10")
    private Integer courageScore; // оценка Мужества

    @NotNull(message = "Оценка Умеренности обязательна")
    @Min(value = 1, message = "Оценка должна быть от 1 до 10")
    @Max(value = 10, message = "Оценка должна быть от 1 до 10")
    private Integer temperanceScore; // оценка Умеренности

    @NotNull(message = "Оценка Справедливости обязательна")
    @Min(value = 1, message = "Оценка должна быть от 1 до 10")
    @Max(value = 10, message = "Оценка должна быть от 1 до 10")
    private Integer justiceScore; // оценка Справедливости

    private String comment;

}
