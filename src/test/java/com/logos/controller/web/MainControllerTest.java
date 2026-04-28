package com.logos.controller.web;

import com.logos.dto.CalendarMonthDTO;
import com.logos.dto.LifeCountdownDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.service.CalendarService;
import com.logos.service.LifeCountdownService;
import com.logos.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CalendarService calendarService;

    @MockitoBean
    private LifeCountdownService countdownService;

    private final UserResponseDTO testUserDTO = createTestUserDTO();

    private UserResponseDTO createTestUserDTO() {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setDeathDate(LocalDate.of(2090, 1, 1));
        user.setRole(User.Role.USER);
        user.setActive(true);
        return user;
    }

    private CalendarMonthDTO createFilledCalendarMonthDTO(int year, int month) {
        CalendarMonthDTO dto = new CalendarMonthDTO();
        dto.setYear(year);
        dto.setMonth(month);
        dto.setMonthName("March 2026");
        dto.setToday(LocalDate.now());
        dto.setDays(List.of());
        dto.setTotalDays(31);
        dto.setEvaluatedDays(0);
        dto.setAverageSatisfaction(0.0);
        dto.setCompletedTasks(0);
        dto.setTotalTasks(0);
        dto.setPreviousMonth(LocalDate.of(year, month, 1).minusMonths(1));
        dto.setNextMonth(LocalDate.of(year, month, 1).plusMonths(1));
        return dto;
    }

    @Test
    void welcome_whenNotAuthenticated_shouldReturnWelcomeView() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"));
    }

    @Test
    void welcome_whenAuthenticated_shouldRedirectToCalendar() throws Exception {
        mockMvc.perform(get("/welcome").with(user("testuser").roles("USER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/calendar"));
    }

    @Test
    void calendar_withoutDate_shouldUseCurrentDate() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUserDTO);
        given(calendarService.getMonthDTO(eq(1L), anyInt(), anyInt()))
                .willReturn(createFilledCalendarMonthDTO(2026, 3));
        given(countdownService.calculateRemainingLife(testUserDTO))
                .willReturn(LifeCountdownDTO.empty());

        mockMvc.perform(get("/calendar").with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("monthData", "lifeCountdown", "user"));
    }

    @Test
    void calendar_withDate_shouldUseProvidedDate() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUserDTO);
        given(calendarService.getMonthDTO(1L, 2026, 3))
                .willReturn(createFilledCalendarMonthDTO(2026, 3));
        given(countdownService.calculateRemainingLife(testUserDTO))
                .willReturn(LifeCountdownDTO.empty());

        mockMvc.perform(get("/calendar").param("date", "2026-03-05").with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("monthData", "lifeCountdown", "user"));
    }

    @Test
    void dreams_shouldReturnDreamsView() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUserDTO);
        given(countdownService.calculateRemainingLife(testUserDTO))
                .willReturn(LifeCountdownDTO.empty());

        mockMvc.perform(get("/dreams").with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "lifeCountdown"));
    }

    @Test
    void profile_shouldReturnProfileView() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUserDTO);
        given(countdownService.calculateRemainingLife(testUserDTO))
                .willReturn(LifeCountdownDTO.empty());

        mockMvc.perform(get("/profile").with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "lifeCountdown"));
    }


    @Test
    void accessDenied_whenAuthenticated_shouldIncludeUserInModel() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUserDTO);

        mockMvc.perform(get("/access-denied").with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void accessDenied_whenNotAuthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/welcome"));
    }
}