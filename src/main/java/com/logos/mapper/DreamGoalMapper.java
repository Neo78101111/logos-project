package com.logos.mapper;

import com.logos.dto.DreamGoalRequestDTO;
import com.logos.dto.DreamGoalResponseDTO;
import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DreamGoalMapper {

    public DreamGoalResponseDTO toResponseDTO(DreamGoal goal) {
        if (goal == null) return null;
        DreamGoalResponseDTO dto = new DreamGoalResponseDTO();
        dto.setId(goal.getId());
        dto.setTitle(goal.getTitle());
        dto.setDeadline(goal.getDeadline());
        dto.setCompleted(goal.isCompleted());
        return dto;
    }

    public List<DreamGoalResponseDTO> toResponseDTOList(List<DreamGoal> goals) {
        return goals.stream().map(this::toResponseDTO).toList();
    }

    public DreamGoal toEntity(DreamGoalRequestDTO dto, Dream dream) {
        return DreamGoal.builder()
                .dream(dream)
                .title(dto.getTitle())
                .deadline(dto.getDeadline())
                .completed(false)
                .build();
    }
}