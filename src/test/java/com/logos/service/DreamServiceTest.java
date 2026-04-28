package com.logos.service;

import com.logos.dto.DreamGoalRequestDTO;
import com.logos.dto.DreamGoalResponseDTO;
import com.logos.dto.DreamRequestDTO;
import com.logos.dto.DreamResponseDTO;
import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import com.logos.entity.User;
import com.logos.exception.ResourceNotFoundException;
import com.logos.mapper.DreamGoalMapper;
import com.logos.mapper.DreamMapper;
import com.logos.repository.DreamGoalRepository;
import com.logos.repository.DreamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DreamServiceTest {

    @Mock
    private DreamRepository dreamRepository;
    @Mock
    private DreamGoalRepository goalRepository;
    @Mock
    private DreamMapper dreamMapper;
    @Mock
    private DreamGoalMapper goalMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private DreamService dreamService;

    private User user;
    private Dream dream;
    private DreamRequestDTO dreamRequest;
    private DreamResponseDTO dreamResponse;
    private final Long userId = 1L;
    private final Long dreamId = 10L;

    @BeforeEach
    void setUp() {
        user = User.builder().id(userId).username("testuser").build();
        dream = Dream.builder()
                .id(dreamId)
                .user(user)
                .title("My Dream")
                .description("Description")
                .status(Dream.DreamStatus.IN_PROGRESS)
                .build();
        dreamRequest = new DreamRequestDTO();
        dreamRequest.setTitle("My Dream");
        dreamRequest.setDescription("Description");
        dreamResponse = new DreamResponseDTO();
        dreamResponse.setId(dreamId);
        dreamResponse.setTitle("My Dream");
    }

    // ---------- createDream ----------
    @Test
    void createDream_shouldReturnSavedDream() {
        when(userService.getUserReferenceById(userId)).thenReturn(user);
        when(dreamMapper.toEntity(dreamRequest, user)).thenReturn(dream);
        when(dreamRepository.save(dream)).thenReturn(dream);
        when(dreamMapper.toResponseDTO(dream)).thenReturn(dreamResponse);

        DreamResponseDTO result = dreamService.createDream(userId, dreamRequest);

        assertThat(result).isEqualTo(dreamResponse);
        verify(dreamRepository).save(dream);
    }

    // ---------- getUserDreams ----------
    @Test
    void getUserDreams_shouldReturnListOfDreams() {
        when(userService.getUserReferenceById(userId)).thenReturn(user);
        List<Dream> dreams = List.of(dream);
        when(dreamRepository.findByUserWithGoals(eq(user), any(Sort.class))).thenReturn(dreams);
        when(dreamMapper.toResponseDTO(dream)).thenReturn(dreamResponse);

        List<DreamResponseDTO> result = dreamService.getUserDreams(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dreamResponse);
    }

    // ---------- getDream ----------
    @Test
    void getDream_whenExistsAndAccessible_shouldReturnDream() {
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(dream));
        when(dreamMapper.toResponseDTO(dream)).thenReturn(dreamResponse);

        DreamResponseDTO result = dreamService.getDream(dreamId, userId);

        assertThat(result).isEqualTo(dreamResponse);
    }

    @Test
    void getDream_whenNotFound_shouldThrowResourceNotFoundException() {
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dreamService.getDream(dreamId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dream not found");
    }

    @Test
    void getDream_whenUserNotOwner_shouldThrowSecurityException() {
        Dream otherUserDream = Dream.builder().id(dreamId).user(User.builder().id(99L).build()).build();
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(otherUserDream));

        assertThatThrownBy(() -> dreamService.getDream(dreamId, userId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access denied");
    }

    // ---------- updateDream ----------
    @Test
    void updateDream_shouldUpdateAndReturn() {
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(dream));
        doNothing().when(dreamMapper).updateEntity(dream, dreamRequest);
        when(dreamRepository.save(dream)).thenReturn(dream);
        when(dreamMapper.toResponseDTO(dream)).thenReturn(dreamResponse);

        DreamResponseDTO result = dreamService.updateDream(dreamId, userId, dreamRequest);

        assertThat(result).isEqualTo(dreamResponse);
        verify(dreamMapper).updateEntity(dream, dreamRequest);
    }

    // ---------- deleteDream ----------
    @Test
    void deleteDream_shouldCallRepositoryDelete() {
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(dream));
        dreamService.deleteDream(dreamId, userId);
        verify(dreamRepository).delete(dream);
    }

    // ---------- addGoal ----------
    @Test
    void addGoal_shouldSaveAndReturnGoal() {
        DreamGoalRequestDTO goalRequest = new DreamGoalRequestDTO();
        goalRequest.setTitle("New goal");
        goalRequest.setDeadline(LocalDate.now().plusDays(5));
        DreamGoal goal = DreamGoal.builder().id(20L).title("New goal").build();
        DreamGoalResponseDTO goalResponse = new DreamGoalResponseDTO();
        goalResponse.setId(20L);

        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(dream));
        when(goalMapper.toEntity(goalRequest, dream)).thenReturn(goal);
        when(goalRepository.save(goal)).thenReturn(goal);
        when(goalMapper.toResponseDTO(goal)).thenReturn(goalResponse);

        var result = dreamService.addGoal(dreamId, userId, goalRequest);

        assertThat(result.getId()).isEqualTo(20L);
        verify(goalRepository).save(goal);
    }

    // ---------- updateGoal ----------
    @Test
    void updateGoal_shouldModifyAndReturn() {
        Long goalId = 30L;
        DreamGoalRequestDTO goalRequest = new DreamGoalRequestDTO();
        goalRequest.setTitle("Updated");
        goalRequest.setDeadline(LocalDate.now());

        DreamGoal goal = DreamGoal.builder().id(goalId).dream(dream).title("Old").build();
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        DreamGoalResponseDTO goalResponse = new DreamGoalResponseDTO();
        goalResponse.setId(goalId);
        when(goalRepository.save(goal)).thenReturn(goal);
        when(goalMapper.toResponseDTO(goal)).thenReturn(goalResponse);

        var result = dreamService.updateGoal(dreamId, goalId, userId, goalRequest);

        assertThat(result.getId()).isEqualTo(goalId);
        assertThat(goal.getTitle()).isEqualTo("Updated");
        verify(goalRepository).save(goal);
    }

    @Test
    void updateGoal_whenGoalNotBelongToDream_shouldThrowSecurityException() {
        Long goalId = 30L;
        Dream otherDream = Dream.builder().id(99L).build();
        DreamGoal goal = DreamGoal.builder().id(goalId).dream(otherDream).build();
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> dreamService.updateGoal(dreamId, goalId, userId, new DreamGoalRequestDTO()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Цель не принадлежит этой мечте");
    }

    // ---------- toggleGoalComplete ----------
    @Test
    void toggleGoalComplete_shouldFlipCompleted() {
        Long goalId = 40L;
        DreamGoal goal = DreamGoal.builder().id(goalId).dream(dream).completed(false).build();
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);
        when(goalMapper.toResponseDTO(goal)).thenReturn(new DreamGoalResponseDTO());

        dreamService.toggleGoalComplete(dreamId, goalId, userId);

        assertThat(goal.isCompleted()).isTrue();
        verify(goalRepository).save(goal);
    }

    // ---------- achieveDream ----------
    @Test
    void achieveDream_shouldSetStatusAchievedAndSave() {
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(dream));
        when(dreamRepository.save(dream)).thenReturn(dream);
        when(dreamMapper.toResponseDTO(dream)).thenReturn(dreamResponse);

        dreamService.achieveDream(dreamId, userId, "resultUrl", "Great!");

        assertThat(dream.getStatus()).isEqualTo(Dream.DreamStatus.ACHIEVED);
        assertThat(dream.getResultImageUrl()).isEqualTo("resultUrl");
        assertThat(dream.getAchievementComment()).isEqualTo("Great!");
    }

    @Test
    void achieveDream_whenAlreadyAchieved_shouldThrowIllegalStateException() {
        dream.setStatus(Dream.DreamStatus.ACHIEVED);
        when(dreamRepository.findByIdWithGoals(dreamId)).thenReturn(Optional.of(dream));

        assertThatThrownBy(() -> dreamService.achieveDream(dreamId, userId, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже достигнута");
    }
}