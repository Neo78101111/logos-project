package com.logos.controller.web;

import com.logos.dto.CalendarMonthDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.service.CalendarService;
import com.logos.service.LifeCountdownService;
import com.logos.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private final CalendarService calendarService;
    private final LifeCountdownService countdownService;

    // Welcome страница - доступна только неаутентифицированным пользователям
    @GetMapping({"/", "/welcome"})
    public String welcome(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // Если пользователь уже аутентифицирован, перенаправляем в calendar
            return "redirect:/calendar";
        }
        return "welcome";
    }

    /**
     * Страница календаря - главная страница приложения
     */
    @GetMapping("/calendar")
    public String calendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date, // ← ДОБАВЛЕН ПАРАМЕТР ДАТЫ
            Model model) {

        if (userDetails == null) {
            return "redirect:/welcome";
        }

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());

        if (date == null) {
            date = LocalDate.now();
        }

        // Получаем данные месяца для указанной даты
        CalendarMonthDTO monthData = calendarService.getMonthDTO(
                user.getId(),
                date.getYear(),
                date.getMonthValue()
        );

        model.addAttribute("monthData", monthData);
        model.addAttribute("lifeCountdown", countdownService.calculateRemainingLife(user));
        model.addAttribute("user", user);

        return "calendar";
    }

    /**
     * Страница мечт
     */
    @GetMapping("/dreams")
    public String dreams(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/welcome";
        }

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("lifeCountdown", countdownService.calculateRemainingLife(user));
        return "dreams";
    }

    /**
     * Страница профиля пользователя
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/welcome";
        }

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("lifeCountdown", countdownService.calculateRemainingLife(user));
        return "profile";
    }

    /**
     * Страница 403 (доступ запрещен)
     */
    @GetMapping("/access-denied")
    public String accessDenied(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
        }
        return "error/403";
    }
}