package com.logos.service;

import com.logos.dto.*;
import com.logos.entity.Day;
import com.logos.entity.DayEvaluation;
import com.logos.entity.DayTask;
import com.logos.entity.User;
import com.logos.exception.UserNotFoundException;
import com.logos.mapper.DayMapper;
import com.logos.repository.DayRepository;
import com.logos.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CalendarService {

    private final DayRepository dayRepository;
    private final UserRepository userRepository;
    private final DayMapper dayMapper;

    public Day.TemporalStatus calculateTemporalStatus(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) return Day.TemporalStatus.PAST;
        if (date.isAfter(today)) return Day.TemporalStatus.FUTURE;
        return Day.TemporalStatus.TODAY;
    }

    // Получить дни месяца (entity)
    public List<Day> getMonthDays(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<Day> existingDays = dayRepository.findByUserIdAndDateBetween(userId, start, end);
        Map<LocalDate, Day> dayMap = existingDays.stream()
                .collect(Collectors.toMap(Day::getDate, d -> d));

        List<Day> result = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Day day = dayMap.get(date);
            if (day == null) {
                day = createNewDay(user, date);
            }
            result.add(day);
        }
        return result;
    }

    // Получить DTO месяца
    public CalendarMonthDTO getMonthDTO(Long userId, int year, int month) {
        List<Day> days = getMonthDays(userId, year, month);
        List<DayResponseDTO> dayDTOs = days.stream()
                .map(day -> dayMapper.toDayResponseDTO(day, calculateTemporalStatus(day.getDate())))
                .toList();

        // Статистика
        int evaluatedDays = (int) days.stream()
                .filter(d -> d.getEvaluation() != null)
                .count();

        double averageSatisfaction = days.stream()
                .filter(d -> d.getEvaluation() != null)
                .mapToInt(d -> d.getEvaluation().getDaySatisfactionScore())
                .average()
                .orElse(0.0);

        // Заменяем goals → tasks
        int completedTasks = (int) days.stream()
                .flatMap(d -> d.getTasks().stream())
                .filter(DayTask::isCompleted)
                .count();

        int totalTasks = (int) days.stream()
                .mapToInt(d -> d.getTasks().size())
                .sum();

        CalendarMonthDTO monthDTO = new CalendarMonthDTO();
        monthDTO.setYear(year);
        monthDTO.setMonth(month);
        monthDTO.setToday(LocalDate.now());
        monthDTO.setDays(dayDTOs);
        monthDTO.setMonthName(getMonthName(month) + " " + year);
        monthDTO.setPreviousMonth(LocalDate.of(year, month, 1).minusMonths(1));
        monthDTO.setNextMonth(LocalDate.of(year, month, 1).plusMonths(1));

        monthDTO.setTotalDays(days.size());
        monthDTO.setEvaluatedDays(evaluatedDays);
        monthDTO.setAverageSatisfaction(averageSatisfaction);
        monthDTO.setCompletedTasks(completedTasks);
        monthDTO.setTotalTasks(totalTasks);

        return monthDTO;
    }

    // Получить DTO конкретного дня
    public DayResponseDTO getDayDTO(Long userId, LocalDate date) {
        Day day = getOrCreateDay(userId, date);
        return dayMapper.toDayResponseDTO(day, calculateTemporalStatus(day.getDate()));
    }

    // Добавить задачу на день
    public DayResponseDTO addTaskToDay(Long userId, LocalDate date, DayTaskDTO taskDTO) {  // переименовано
        Day day = getOrCreateDay(userId, date);

        if (calculateTemporalStatus(date) == Day.TemporalStatus.PAST) {
            throw new IllegalArgumentException("Нельзя добавлять задачи в прошлые дни");
        }

        DayTask task = dayMapper.toDayTask(taskDTO, day);
        day.getTasks().add(task);
        Day savedDay = dayRepository.save(day);
        log.info("Добавлена задача '{}' на день {}", task.getTitle(), date);

        return dayMapper.toDayResponseDTO(savedDay, calculateTemporalStatus(savedDay.getDate()));
    }

    // Выполнить задачу
    public DayResponseDTO completeTask(Long userId, LocalDate date, Long taskId) {  // переименовано
        Day day = getOrCreateDay(userId, date);

        if (calculateTemporalStatus(date) == Day.TemporalStatus.PAST) {
            throw new IllegalArgumentException("Нельзя изменять задачи в прошлых днях");
        }

        DayTask task = day.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

        task.setCompleted(true);
        Day savedDay = dayRepository.save(day);
        log.info("Задача {} отмечена выполненной для дня {}", taskId, date);

        return dayMapper.toDayResponseDTO(savedDay, calculateTemporalStatus(savedDay.getDate()));
    }

    // Обновить задачу дня
    public DayResponseDTO updateDayTask(Long userId, LocalDate date, Long taskId, DayTaskDTO taskDTO) { // переименовано
        Day day = getOrCreateDay(userId, date);

        DayTask task = day.getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }
        task.setCompleted(taskDTO.isCompleted());

        Day savedDay = dayRepository.save(day);
        log.info("Обновлена задача '{}' на день {}", task.getTitle(), date);
        return dayMapper.toDayResponseDTO(savedDay, calculateTemporalStatus(savedDay.getDate()));
    }

    // Оценить день
    public DayResponseDTO evaluateDay(Long userId, LocalDate date, DayEvaluationDTO evaluationDTO) {
        Day day = getOrCreateDay(userId, date);

        if (calculateTemporalStatus(date) != Day.TemporalStatus.TODAY) {
            throw new IllegalArgumentException("Оценить можно только сегодняшний день");
        }
        if (day.getEvaluation() != null) {
            throw new IllegalArgumentException("День уже оценен");
        }

        DayEvaluation evaluation = dayMapper.toDayEvaluation(evaluationDTO, day);
        day.setEvaluation(evaluation);
        Day savedDay = dayRepository.save(day);
        log.info("Пользователь {} оценил день {}", userId, date);

        return dayMapper.toDayResponseDTO(savedDay, calculateTemporalStatus(savedDay.getDate()));
    }

    // Удалить задачу дня
    public void deleteDayTask(Long userId, LocalDate date, Long taskId) {   // переименовано
        Day day = dayRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalArgumentException("День не найден"));

        if (calculateTemporalStatus(date) == Day.TemporalStatus.PAST) {
            throw new IllegalArgumentException("Нельзя удалять задачи из прошлых дней");
        }

        boolean removed = day.getTasks().removeIf(t -> t.getId().equals(taskId));
        if (removed) {
            dayRepository.save(day);
            log.info("Удалена задача {} из дня {}", taskId, date);
        } else {
            throw new IllegalArgumentException("Задача не найдена");
        }
    }

    // Вспомогательные методы
    private Day getOrCreateDay(Long userId, LocalDate date) {
        return dayRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));
                    return createNewDay(user, date);
                });
    }

    private Day createNewDay(User user, LocalDate date) {
        Day day = Day.builder()
                .user(user)
                .date(date)
                .tasks(new ArrayList<>())
                .build();
        return dayRepository.save(day);
    }


    private String getMonthName(int month) {
        return switch(month) {
            case 1 -> "Январь";
            case 2 -> "Февраль";
            case 3 -> "Март";
            case 4 -> "Апрель";
            case 5 -> "Май";
            case 6 -> "Июнь";
            case 7 -> "Июль";
            case 8 -> "Август";
            case 9 -> "Сентябрь";
            case 10 -> "Октябрь";
            case 11 -> "Ноябрь";
            case 12 -> "Декабрь";
            default -> "Неизвестно";
        };
    }
}