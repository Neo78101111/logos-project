package com.logos.service;

import com.logos.dto.*;
import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import com.logos.entity.User;
import com.logos.exception.ResourceNotFoundException;
import com.logos.mapper.DreamGoalMapper;
import com.logos.mapper.DreamMapper;
import com.logos.repository.DreamGoalRepository;
import com.logos.repository.DreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DreamService {

    private final DreamRepository dreamRepository;
    private final DreamGoalRepository goalRepository;
    private final DreamMapper dreamMapper;
    private final DreamGoalMapper goalMapper;
    private final UserService userService;

    // Создать мечту
    @Transactional
    public DreamResponseDTO createDream(Long userId, DreamRequestDTO dto) {
        log.info("Создание мечты пользователем ID={}, название: {}", userId, dto.getTitle());
        User user = userService.getUserReferenceById(userId);
        Dream dream = dreamMapper.toEntity(dto, user);
        dream = dreamRepository.save(dream);
        log.info("Мечта создана: ID={}, название={}", dream.getId(), dream.getTitle());
        return dreamMapper.toResponseDTO(dream);
    }

    // Получить все мечты пользователя
    public List<DreamResponseDTO> getUserDreams(Long userId) {
        log.debug("Запрос всех мечт пользователя ID={}", userId);
        User user = userService.getUserReferenceById(userId);
        List<Dream> dreams = dreamRepository.findByUserWithGoals(user, Sort.by(Sort.Direction.DESC, "createdAt"));
        return dreams.stream().map(dreamMapper::toResponseDTO).toList();
    }

    // Получить одну мечту
    public DreamResponseDTO getDream(Long dreamId, Long userId) {
        log.debug("Получение мечты ID={} пользователем ID={}", dreamId, userId);
        Dream dream = findDreamAndCheckAccess(dreamId, userId);
        return dreamMapper.toResponseDTO(dream);
    }

    // Обновить мечту
    @Transactional
    public DreamResponseDTO updateDream(Long dreamId, Long userId, DreamRequestDTO dto) {
        log.info("Обновление мечты ID={} пользователем ID={}", dreamId, userId);
        Dream dream = findDreamAndCheckAccess(dreamId, userId);
        dreamMapper.updateEntity(dream, dto);
        dream = dreamRepository.save(dream);
        log.info("Мечта ID={} обновлена", dreamId);
        return dreamMapper.toResponseDTO(dream);
    }

    // Удалить мечту
    @Transactional
    public void deleteDream(Long dreamId, Long userId) {
        log.info("Удаление мечты ID={} пользователем ID={}", dreamId, userId);
        Dream dream = findDreamAndCheckAccess(dreamId, userId);
        dreamRepository.delete(dream);
        log.info("Мечта ID={} удалена", dreamId);
    }

    // Добавить цель к мечте
    @Transactional
    public DreamGoalResponseDTO addGoal(Long dreamId, Long userId, DreamGoalRequestDTO dto) {
        log.info("Добавление цели к мечте ID={} пользователем ID={}, цель: {}", dreamId, userId, dto.getTitle());
        Dream dream = findDreamAndCheckAccess(dreamId, userId);
        DreamGoal goal = goalMapper.toEntity(dto, dream);
        goal = goalRepository.save(goal);
        // Синхронизация двунаправленной связи
        dream.getGoals().add(goal);
        log.info("Цель добавлена: ID={}, название={}", goal.getId(), goal.getTitle());
        return goalMapper.toResponseDTO(goal);
    }

    // Обновить цель (с проверкой принадлежности dreamId)
    @Transactional
    public DreamGoalResponseDTO updateGoal(Long dreamId, Long goalId, Long userId, DreamGoalRequestDTO dto) {
        log.info("Обновление цели ID={} (мечта ID={}) пользователем ID={}", goalId, dreamId, userId);
        DreamGoal goal = findGoalAndCheckAccess(dreamId, goalId, userId);
        goal.setTitle(dto.getTitle());
        goal.setDeadline(dto.getDeadline());
        goal = goalRepository.save(goal);
        log.info("Цель ID={} обновлена", goalId);
        return goalMapper.toResponseDTO(goal);
    }

    // Переключить статус выполнения цели
    @Transactional
    public DreamGoalResponseDTO toggleGoalComplete(Long dreamId, Long goalId, Long userId) {
        log.info("Переключение статуса цели ID={} (мечта ID={}) пользователем ID={}", goalId, dreamId, userId);
        DreamGoal goal = findGoalAndCheckAccess(dreamId, goalId, userId);
        goal.setCompleted(!goal.isCompleted());
        goal = goalRepository.save(goal);
        log.info("Цель ID={} теперь completed={}", goalId, goal.isCompleted());
        return goalMapper.toResponseDTO(goal);
    }

    // Удалить цель
    @Transactional
    public void deleteGoal(Long dreamId, Long goalId, Long userId) {
        log.info("Удаление цели ID={} (мечта ID={}) пользователем ID={}", goalId, dreamId, userId);
        DreamGoal goal = findGoalAndCheckAccess(dreamId, goalId, userId);
        // Синхронизация двунаправленной связи
        Dream dream = goal.getDream();
        dream.getGoals().remove(goal);
        goalRepository.delete(goal);
        log.info("Цель ID={} удалена", goalId);
    }

    // Завершить мечту
    @Transactional
    public DreamResponseDTO achieveDream(Long dreamId, Long userId, String resultImageUrl, String achievementComment) {
        log.info("Достижение мечты ID={} пользователем ID={}", dreamId, userId);
        Dream dream = findDreamAndCheckAccess(dreamId, userId);
        if (dream.getStatus() == Dream.DreamStatus.ACHIEVED) {
            throw new IllegalStateException("Мечта уже достигнута");
        }
        dream.setStatus(Dream.DreamStatus.ACHIEVED);
        dream.setResultImageUrl(resultImageUrl);
        dream.setAchievementComment(achievementComment);
        dream = dreamRepository.save(dream);
        log.info("Мечта ID={} отмечена как достигнутая", dreamId);
        return dreamMapper.toResponseDTO(dream);
    }

    // Вспомогательные методы проверки доступа
    private Dream findDreamAndCheckAccess(Long dreamId, Long userId) {
        Dream dream = dreamRepository.findByIdWithGoals(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Dream not found"));
        if (!dream.getUser().getId().equals(userId)) {
            log.warn("Попытка несанкционированного доступа к мечте ID={} пользователем ID={}", dreamId, userId);
            throw new SecurityException("Access denied");
        }
        return dream;
    }

    private DreamGoal findGoalAndCheckAccess(Long dreamId, Long goalId, Long userId) {
        DreamGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.getDream().getId().equals(dreamId)) {
            log.warn("Цель ID={} не принадлежит мечте ID={}", goalId, dreamId);
            throw new SecurityException("Цель не принадлежит этой мечте");
        }
        if (!goal.getDream().getUser().getId().equals(userId)) {
            log.warn("Попытка несанкционированного доступ к цели ID={} пользователем ID={}", goalId, userId);
            throw new SecurityException("Access denied");
        }
        return goal;
    }
}