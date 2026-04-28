package com.logos.controller.api;

import com.logos.dto.*;
import com.logos.entity.User;
import com.logos.service.CalendarService;
import com.logos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final UserService userService;

    @GetMapping("/months/{year}/{month}")
    public ResponseEntity<CalendarMonthDTO> getMonth(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        CalendarMonthDTO monthData = calendarService.getMonthDTO(user.getId(), year, month);
        return ResponseEntity.ok(monthData);
    }

    @GetMapping("/days/{date}")
    public ResponseEntity<DayResponseDTO> getDay(
            @PathVariable LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        DayResponseDTO day = calendarService.getDayDTO(user.getId(), date);
        return ResponseEntity.ok(day);
    }

    @PatchMapping("/days/{date}/evaluation")
    public ResponseEntity<DayResponseDTO> evaluateDay(
            @PathVariable LocalDate date,
            @Valid @RequestBody DayEvaluationDTO evaluationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        DayResponseDTO evaluatedDay = calendarService.evaluateDay(user.getId(), date, evaluationDTO);
        return ResponseEntity.ok(evaluatedDay);
    }

    @PostMapping("/days/{date}/tasks")
    public ResponseEntity<DayResponseDTO> addDayTask(
            @PathVariable LocalDate date,
            @Valid @RequestBody DayTaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        DayResponseDTO day = calendarService.addTaskToDay(user.getId(), date, taskDTO);
        return ResponseEntity.ok(day);
    }

    @PutMapping("/days/{date}/tasks/{taskId}")
    public ResponseEntity<DayResponseDTO> updateDayTask(
            @PathVariable LocalDate date,
            @PathVariable Long taskId,
            @Valid @RequestBody DayTaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        DayResponseDTO day = calendarService.updateDayTask(user.getId(), date, taskId, taskDTO);
        return ResponseEntity.ok(day);
    }

    @DeleteMapping("/days/{date}/tasks/{taskId}")
    public ResponseEntity<Void> deleteDayTask(
            @PathVariable LocalDate date,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        calendarService.deleteDayTask(user.getId(), date, taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/days/{date}/tasks/{taskId}")
    public ResponseEntity<DayResponseDTO> completeTask(
            @PathVariable LocalDate date,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        DayResponseDTO updatedDay = calendarService.completeTask(user.getId(), date, taskId);
        return ResponseEntity.ok(updatedDay);
    }
}