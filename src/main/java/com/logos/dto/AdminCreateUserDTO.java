package com.logos.dto;

import com.logos.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminCreateUserDTO {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Имя пользователя может содержать только латинские буквы, цифры и подчеркивание")
    private String username;

    @NotNull(message = "Роль обязательна") //выбор роли пользователя доступен только админу
    private User.Role role;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;

    @NotNull(message = "Дата смерти обязательна")
    @Future(message = "Дата смерти должна быть в будущем")
    private LocalDate deathDate;

    @AssertTrue(message = "Дата смерти должна быть после даты рождения")
    public boolean isDeathDateAfterBirthDate() {
        if (birthDate == null || deathDate == null) return true;
        return deathDate.isAfter(birthDate);
    }

    @AssertTrue(message = "Дата смерти должна быть не более чем через 120 лет после рождения")
    public boolean isDeathDateReasonable() {
        if (birthDate == null || deathDate == null) return true;
        return deathDate.isBefore(birthDate.plusYears(120));
    }
}
