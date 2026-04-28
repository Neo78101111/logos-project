package com.logos.controller.api;

import com.logos.dto.*;
import com.logos.entity.User;
import com.logos.service.CalendarService;
import com.logos.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CalendarService calendarService;

    @MockitoBean
    private UserService userService;

    private final UserResponseDTO testUser = createTestUser();

    private UserResponseDTO createTestUser() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setUsername("testuser");
        dto.setRole(User.Role.USER);
        return dto;
    }

    private CalendarMonthDTO createMonthDTO() {
        CalendarMonthDTO dto = new CalendarMonthDTO();
        dto.setYear(2026);
        dto.setMonth(3);
        dto.setDays(List.of());
        return dto;
    }

    private DayResponseDTO createDayDTO() {
        DayResponseDTO dto = new DayResponseDTO();
        dto.setDate(LocalDate.of(2026, 3, 5));
        dto.setTasks(List.of());
        return dto;
    }

    @Test
    void getMonth_shouldReturnMonthData() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(calendarService.getMonthDTO(eq(1L), eq(2026), eq(3))).willReturn(createMonthDTO());

        mockMvc.perform(get("/api/calendar/months/2026/3")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(3));
    }

    @Test
    void getDay_shouldReturnDayData() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(calendarService.getDayDTO(eq(1L), eq(LocalDate.of(2026, 3, 5)))).willReturn(createDayDTO());

        mockMvc.perform(get("/api/calendar/days/2026-03-05")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-03-05"));
    }

    @Test
    void evaluateDay_shouldReturnEvaluatedDay() throws Exception {
        DayEvaluationDTO evaluation = new DayEvaluationDTO();
        evaluation.setDaySatisfactionScore(8);
        evaluation.setWisdomScore(7);
        evaluation.setCourageScore(6);
        evaluation.setTemperanceScore(9);
        evaluation.setJusticeScore(5);
        evaluation.setComment("Good day");

        DayResponseDTO evaluated = createDayDTO();

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(calendarService.evaluateDay(eq(1L), eq(LocalDate.of(2026, 3, 5)), any(DayEvaluationDTO.class)))
                .willReturn(evaluated);

        mockMvc.perform(patch("/api/calendar/days/2026-03-05/evaluation")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evaluation)))
                .andExpect(status().isOk());
    }

    @Test
    void addDayTask_shouldReturnUpdatedDay() throws Exception {
        DayTaskDTO taskDTO = new DayTaskDTO();
        taskDTO.setTitle("New Task");
        taskDTO.setDescription("Desc");
        taskDTO.setPriority(com.logos.entity.DayTask.Priority.MEDIUM);
        taskDTO.setCompleted(false);

        DayResponseDTO updatedDay = createDayDTO();

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(calendarService.addTaskToDay(eq(1L), eq(LocalDate.of(2026, 3, 5)), any(DayTaskDTO.class)))
                .willReturn(updatedDay);

        mockMvc.perform(post("/api/calendar/days/2026-03-05/tasks")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateDayTask_shouldReturnUpdatedDay() throws Exception {
        DayTaskDTO taskDTO = new DayTaskDTO();
        taskDTO.setTitle("Updated");
        taskDTO.setPriority(com.logos.entity.DayTask.Priority.MEDIUM);

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(calendarService.updateDayTask(eq(1L), eq(LocalDate.of(2026, 3, 5)), eq(10L), any(DayTaskDTO.class)))
                .willReturn(createDayDTO());

        mockMvc.perform(put("/api/calendar/days/2026-03-05/tasks/10")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDayTask_shouldReturnNoContent() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);

        mockMvc.perform(delete("/api/calendar/days/2026-03-05/tasks/10")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(calendarService).deleteDayTask(1L, LocalDate.of(2026, 3, 5), 10L);
    }

    @Test
    void completeTask_shouldReturnUpdatedDay() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(calendarService.completeTask(eq(1L), eq(LocalDate.of(2026, 3, 5)), eq(10L)))
                .willReturn(createDayDTO());

        mockMvc.perform(patch("/api/calendar/days/2026-03-05/tasks/10")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}