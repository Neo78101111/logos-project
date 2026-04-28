package com.logos.mapper;

import com.logos.dto.*;
import com.logos.entity.Day;
import com.logos.entity.DayEvaluation;
import com.logos.entity.DayTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DayMapper {

    // Day → DayResponseDTO
    public DayResponseDTO toDayResponseDTO(Day day, Day.TemporalStatus temporalStatus) {
        if (day == null) return null;

        DayResponseDTO dto = new DayResponseDTO();
        dto.setId(day.getId());
        dto.setDate(day.getDate());
        dto.setTemporalStatus(temporalStatus);
        dto.setTasks(toDayTaskResponseDTOList(day.getTasks()));
        dto.setEvaluation(toDayEvaluationResponseDTO(day.getEvaluation()));

        dto.setCanEdit(canEditDay(day));
        dto.setCanEvaluate(canEvaluateDay(day));

        return dto;
    }

    // DayTask → DayTaskResponseDTO
    public DayTaskResponseDTO toDayTaskResponseDTO(DayTask task) {
        if (task == null) return null;

        DayTaskResponseDTO dto = new DayTaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCompleted(task.isCompleted());
        dto.setPriority(task.getPriority());
        return dto;
    }

    // List<DayTask> → List<DayTaskResponseDTO>
    public List<DayTaskResponseDTO> toDayTaskResponseDTOList(List<DayTask> tasks) {
        if (tasks == null) return List.of();
        return tasks.stream().map(this::toDayTaskResponseDTO).toList();
    }

    // DayEvaluation → DayEvaluationResponseDTO
    public DayEvaluationResponseDTO toDayEvaluationResponseDTO(DayEvaluation evaluation) {
        if (evaluation == null) return null;

        DayEvaluationResponseDTO dto = new DayEvaluationResponseDTO();
        dto.setId(evaluation.getId());
        dto.setDaySatisfactionScore(evaluation.getDaySatisfactionScore());
        dto.setWisdomScore(evaluation.getWisdomScore());
        dto.setCourageScore(evaluation.getCourageScore());
        dto.setTemperanceScore(evaluation.getTemperanceScore());
        dto.setJusticeScore(evaluation.getJusticeScore());
        dto.setComment(evaluation.getComment());
        return dto;
    }

    // DayEvaluationDTO → DayEvaluation
    public DayEvaluation toDayEvaluation(DayEvaluationDTO dto, Day day) {
        if (dto == null) return null;

        return DayEvaluation.builder()
                .daySatisfactionScore(dto.getDaySatisfactionScore())
                .wisdomScore(dto.getWisdomScore())
                .courageScore(dto.getCourageScore())
                .temperanceScore(dto.getTemperanceScore())
                .justiceScore(dto.getJusticeScore())
                .comment(dto.getComment())
                .build();
    }

    // DayTaskDTO → DayTask
    public DayTask toDayTask(DayTaskDTO dto, Day day) {
        if (dto == null) return null;

        return DayTask.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .isCompleted(dto.isCompleted())
                .priority(dto.getPriority())
                .build();
    }

    private boolean canEditDay(Day day) {
        if (day == null || day.getDate() == null) return false;
        return !day.getDate().isBefore(LocalDate.now());
    }

    private boolean canEvaluateDay(Day day) {
        if (day == null || day.getDate() == null) return false;
        return day.getDate().isEqual(LocalDate.now()) && day.getEvaluation() == null;
    }
}