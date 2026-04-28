package com.logos.service;

import com.logos.dto.DayEvaluationDTO;
import com.logos.dto.DayTaskDTO;
import com.logos.dto.DayResponseDTO;
import com.logos.entity.Day;
import com.logos.entity.DayEvaluation;
import com.logos.entity.DayTask;
import com.logos.entity.User;
import com.logos.exception.UserNotFoundException;
import com.logos.mapper.DayMapper;
import com.logos.repository.DayRepository;
import com.logos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private DayRepository dayRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DayMapper dayMapper;

    @InjectMocks
    private CalendarService calendarService;

    private User user;
    private final Long userId = 1L;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
    }

    // --- getMonthDTO ---

    @Test
    void getMonthDTO_shouldCreateMissingDays() {
        // given
        int year = today.getYear();
        int month = today.getMonthValue();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(dayRepository.findByUserIdAndDateBetween(userId, start, end)).thenReturn(List.of());
        when(dayRepository.save(any(Day.class))).thenAnswer(i -> i.getArgument(0));
        when(dayMapper.toDayResponseDTO(any(Day.class), any(Day.TemporalStatus.class)))
                .thenReturn(new DayResponseDTO());

        // when
        var result = calendarService.getMonthDTO(userId, year, month);

        // then
        assertThat(result.getDays()).hasSize(yearMonth.lengthOfMonth());
        verify(dayRepository, times(yearMonth.lengthOfMonth())).save(any(Day.class));
    }

    @Test
    void getMonthDTO_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.getMonthDTO(userId, 2025, 3))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- addTaskToDay ---

    @Test
    void addTaskToDay_forFutureDay_shouldSucceed() {
        // given
        LocalDate future = today.plusDays(1);
        Day day = Day.builder().id(100L).date(future).tasks(new ArrayList<>()).build();
        DayTaskDTO dto = new DayTaskDTO();
        dto.setTitle("New Task");
        dto.setPriority(DayTask.Priority.HIGH);
        DayTask taskEntity = new DayTask();

        when(dayRepository.findByUserIdAndDate(userId, future)).thenReturn(Optional.of(day));
        when(dayMapper.toDayTask(dto, day)).thenReturn(taskEntity);
        when(dayRepository.save(day)).thenReturn(day);
        when(dayMapper.toDayResponseDTO(any(Day.class), any(Day.TemporalStatus.class)))
                .thenReturn(new DayResponseDTO());

        // when
        DayResponseDTO result = calendarService.addTaskToDay(userId, future, dto);

        // then
        assertThat(result).isNotNull();
        verify(dayRepository).save(day);
        assertThat(day.getTasks()).contains(taskEntity);
    }

    @Test
    void addTaskToDay_forPastDay_shouldThrowException() {
        LocalDate past = today.minusDays(1);
        Day day = Day.builder().date(past).build();

        when(dayRepository.findByUserIdAndDate(userId, past)).thenReturn(Optional.of(day));

        assertThatThrownBy(() -> calendarService.addTaskToDay(userId, past, new DayTaskDTO()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нельзя добавлять задачи в прошлые дни");
    }

    // --- completeTask ---

    @Test
    void completeTask_shouldSetCompletedTrue() {
        // given
        Long taskId = 10L;
        LocalDate todayDate = today;
        DayTask task = DayTask.builder().id(taskId).isCompleted(false).build();
        Day day = Day.builder().id(100L).date(todayDate).tasks(new ArrayList<>(List.of(task))).build();

        when(dayRepository.findByUserIdAndDate(userId, todayDate)).thenReturn(Optional.of(day));
        when(dayRepository.save(day)).thenReturn(day);
        when(dayMapper.toDayResponseDTO(any(Day.class), any(Day.TemporalStatus.class)))
                .thenReturn(new DayResponseDTO());

        // when
        calendarService.completeTask(userId, todayDate, taskId);

        // then
        assertThat(task.isCompleted()).isTrue();
        verify(dayRepository, times(1)).save(day);
    }

    @Test
    void completeTask_forPastDay_shouldThrowException() {
        // given
        LocalDate past = today.minusDays(1);
        Day day = Day.builder().date(past).build();

        when(dayRepository.findByUserIdAndDate(userId, past)).thenReturn(Optional.of(day));
        // when/then
        assertThatThrownBy(() -> calendarService.completeTask(userId, past, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нельзя изменять задачи в прошлых днях");
    }

    @Test
    void completeTask_whenTaskNotFound_shouldThrowException() {
        // given
        Long taskId = 999L;
        Day day = Day.builder().id(100L).date(today).tasks(new ArrayList<>()).build();

        when(dayRepository.findByUserIdAndDate(userId, today)).thenReturn(Optional.of(day));

        // when/then
        assertThatThrownBy(() -> calendarService.completeTask(userId, today, taskId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Задача не найдена");
    }

    // --- deleteDayTask ---

    @Test
    void deleteDayTask_shouldRemoveTask() {
        // given
        Long taskId = 10L;
        DayTask task = DayTask.builder().id(taskId).build();
        List<DayTask> tasks = new ArrayList<>();
        tasks.add(task);
        Day day = Day.builder().id(100L).date(today).tasks(tasks).build();

        when(dayRepository.findByUserIdAndDate(userId, today)).thenReturn(Optional.of(day));
        // when
        calendarService.deleteDayTask(userId, today, taskId);

        // then
        assertThat(day.getTasks()).doesNotContain(task);
        verify(dayRepository).save(day);
    }

    // --- evaluateDay ---

    @Test
    void evaluateDay_forTodayWithoutEvaluation_shouldSucceed() {
        // given
        LocalDate todayDate = today;
        Day day = Day.builder().id(100L).date(todayDate).evaluation(null).build();
        DayEvaluationDTO dto = new DayEvaluationDTO();
        dto.setDaySatisfactionScore(8);
        dto.setWisdomScore(7);
        dto.setCourageScore(6);
        dto.setTemperanceScore(9);
        dto.setJusticeScore(5);
        dto.setComment("Nice");
        DayEvaluation evaluation = new DayEvaluation();

        when(dayRepository.findByUserIdAndDate(userId, todayDate)).thenReturn(Optional.of(day));
        when(dayMapper.toDayEvaluation(dto, day)).thenReturn(evaluation);
        when(dayRepository.save(day)).thenReturn(day);
        when(dayMapper.toDayResponseDTO(any(Day.class), any(Day.TemporalStatus.class)))
                .thenReturn(new DayResponseDTO());

        // when
        calendarService.evaluateDay(userId, todayDate, dto);

        // then
        assertThat(day.getEvaluation()).isEqualTo(evaluation);
        verify(dayRepository, atLeastOnce()).save(any(Day.class));
    }

    @Test
    void evaluateDay_forNotToday_shouldThrowException() {
        LocalDate future = today.plusDays(1);
        Day day = Day.builder().date(future).build();

        when(dayRepository.findByUserIdAndDate(userId, future)).thenReturn(Optional.of(day));

        assertThatThrownBy(() -> calendarService.evaluateDay(userId, future, new DayEvaluationDTO()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Оценить можно только сегодняшний день");
    }

    @Test
    void evaluateDay_whenAlreadyEvaluated_shouldThrowException() {
        // given
        Day day = Day.builder().date(today).evaluation(new DayEvaluation()).build();

        when(dayRepository.findByUserIdAndDate(userId, today)).thenReturn(Optional.of(day));

        // when/then
        assertThatThrownBy(() -> calendarService.evaluateDay(userId, today, new DayEvaluationDTO()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("День уже оценен");
    }

    // --- Тесты для calculateTemporalStatus ---

    @Test
    void calculateTemporalStatus_forPastDate_shouldReturnPAST() {
        LocalDate past = today.minusDays(1);
        assertThat(calendarService.calculateTemporalStatus(past)).isEqualTo(Day.TemporalStatus.PAST);
    }

    @Test
    void calculateTemporalStatus_forToday_shouldReturnTODAY() {
        assertThat(calendarService.calculateTemporalStatus(today)).isEqualTo(Day.TemporalStatus.TODAY);
    }

    @Test
    void calculateTemporalStatus_forFutureDate_shouldReturnFUTURE() {
        LocalDate future = today.plusDays(1);
        assertThat(calendarService.calculateTemporalStatus(future)).isEqualTo(Day.TemporalStatus.FUTURE);
    }
}