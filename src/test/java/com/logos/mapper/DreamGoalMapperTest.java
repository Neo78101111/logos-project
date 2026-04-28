package com.logos.mapper;

import com.logos.dto.DreamGoalRequestDTO;
import com.logos.dto.DreamGoalResponseDTO;
import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreamGoalMapperTest {

    private final DreamGoalMapper mapper = new DreamGoalMapper();

    @Test
    void toResponseDTO_shouldMapAllFields() {
        DreamGoal goal = DreamGoal.builder()
                .id(1L)
                .title("Test goal")
                .deadline(LocalDate.of(2026, 12, 31))
                .completed(true)
                .build();

        DreamGoalResponseDTO dto = mapper.toResponseDTO(goal);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Test goal");
        assertThat(dto.getDeadline()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(dto.isCompleted()).isTrue();
    }

    @Test
    void toResponseDTOList_shouldMapList() {
        DreamGoal goal1 = DreamGoal.builder().id(1L).build();
        DreamGoal goal2 = DreamGoal.builder().id(2L).build();
        List<DreamGoal> goals = List.of(goal1, goal2);

        List<DreamGoalResponseDTO> dtos = mapper.toResponseDTOList(goals);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toEntity_shouldCreateGoalFromDtoAndDream() {
        Dream dream = Dream.builder().id(10L).build();
        DreamGoalRequestDTO dto = new DreamGoalRequestDTO();
        dto.setTitle("New goal");
        dto.setDeadline(LocalDate.of(2026, 10, 1));

        DreamGoal goal = mapper.toEntity(dto, dream);

        assertThat(goal.getDream()).isEqualTo(dream);
        assertThat(goal.getTitle()).isEqualTo("New goal");
        assertThat(goal.getDeadline()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(goal.isCompleted()).isFalse();
    }
}