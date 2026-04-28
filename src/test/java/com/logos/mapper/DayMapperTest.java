package com.logos.mapper;

import com.logos.dto.DayTaskResponseDTO;          // импорт обновлён
import com.logos.dto.DayResponseDTO;
import com.logos.dto.DayEvaluationResponseDTO;
import com.logos.entity.Day;
import com.logos.entity.DayTask;                   // DayGoal → DayTask
import com.logos.entity.DayEvaluation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DayMapperTest {

    private DayMapper dayMapper;

    @BeforeEach
    void setUp() {
        dayMapper = new DayMapper();
    }

    @Test
    void toDayResponseDTO_shouldMapAllFields() {
        // given
        LocalDate today = LocalDate.now();
        Day day = Day.builder()
                .id(1L)
                .date(today)
                .tasks(List.of(
                        DayTask.builder()
                                .id(10L)
                                .title("Test task")
                                .description("Description")
                                .isCompleted(false)
                                .priority(DayTask.Priority.HIGH)
                                .build()
                ))
                .evaluation(DayEvaluation.builder()
                        .id(20L)
                        .daySatisfactionScore(8)
                        .wisdomScore(7)
                        .courageScore(6)
                        .temperanceScore(9)
                        .justiceScore(5)
                        .comment("Good day")
                        .build())
                .build();

        // when
        DayResponseDTO dto = dayMapper.toDayResponseDTO(day, Day.TemporalStatus.TODAY);

        // then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDate()).isEqualTo(today);
        assertThat(dto.getTemporalStatus()).isEqualTo(Day.TemporalStatus.TODAY);
        assertThat(dto.getTasks()).hasSize(1);
        DayTaskResponseDTO taskDto = dto.getTasks().get(0);
        assertThat(taskDto.getId()).isEqualTo(10L);
        assertThat(taskDto.getTitle()).isEqualTo("Test task");
        assertThat(taskDto.isCompleted()).isFalse();
        assertThat(taskDto.getPriority()).isEqualTo(DayTask.Priority.HIGH);

        DayEvaluationResponseDTO evalDto = dto.getEvaluation();
        assertThat(evalDto.getId()).isEqualTo(20L);
        assertThat(evalDto.getDaySatisfactionScore()).isEqualTo(8);
        assertThat(evalDto.getComment()).isEqualTo("Good day");

        // проверка разрешений
        assertThat(dto.isCanEdit()).isTrue();   // сегодня
        assertThat(dto.isCanEvaluate()).isFalse(); // уже есть оценка
    }

    @Test
    void toDayResponseDTO_withNullEvaluation_shouldReturnNullEvaluation() {
        // given
        Day day = Day.builder()
                .id(1L)
                .date(LocalDate.now())
                .tasks(List.of())
                .evaluation(null)
                .build();

        // when
        DayResponseDTO dto = dayMapper.toDayResponseDTO(day, Day.TemporalStatus.TODAY);

        // then
        assertThat(dto.getEvaluation()).isNull();
    }

    @Test
    void toDayTaskResponseDTO_shouldMapCorrectly() {
        // given
        DayTask task = DayTask.builder()
                .id(5L)
                .title("Task")
                .description("Desc")
                .isCompleted(true)
                .priority(DayTask.Priority.LOW)
                .build();

        // when
        DayTaskResponseDTO dto = dayMapper.toDayTaskResponseDTO(task); // toDayGoalResponseDTO → toDayTaskResponseDTO

        // then
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getTitle()).isEqualTo("Task");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.isCompleted()).isTrue();
        assertThat(dto.getPriority()).isEqualTo(DayTask.Priority.LOW);
    }

    @Test
    void toDayEvaluationResponseDTO_shouldMapCorrectly() {
        // given
        DayEvaluation eval = DayEvaluation.builder()
                .id(7L)
                .daySatisfactionScore(9)
                .wisdomScore(8)
                .courageScore(7)
                .temperanceScore(6)
                .justiceScore(10)
                .comment("Great")
                .build();

        // when
        DayEvaluationResponseDTO dto = dayMapper.toDayEvaluationResponseDTO(eval);

        // then
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getDaySatisfactionScore()).isEqualTo(9);
        assertThat(dto.getWisdomScore()).isEqualTo(8);
        assertThat(dto.getCourageScore()).isEqualTo(7);
        assertThat(dto.getTemperanceScore()).isEqualTo(6);
        assertThat(dto.getJusticeScore()).isEqualTo(10);
        assertThat(dto.getComment()).isEqualTo("Great");
    }
}