package com.logos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.Period;

@Data
public class RegisterDTO {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Имя пользователя может содержать только буквы, цифры и подчеркивание")
    private String username;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Пароль должен содержать как минимум одну букву и одну цифру")
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;

    @NotNull(message = "Дата смерти обязательна")
    @Future(message = "Дата смерти должна быть в будущем")
    private LocalDate deathDate;

    @AssertTrue(message = "Пароли не совпадают")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "Вы должны быть старше 13 лет")
    public boolean isAgeValid() {
        if (birthDate == null) return true;
        return Period.between(birthDate, LocalDate.now()).getYears() >= 13;
    }

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