package com.logos.mapper;

import com.logos.dto.DreamGoalResponseDTO;
import com.logos.dto.DreamRequestDTO;
import com.logos.dto.DreamResponseDTO;
import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import com.logos.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DreamMapper {

    private final DreamGoalMapper goalMapper;

    public DreamResponseDTO toResponseDTO(Dream dream) {
        if (dream == null) return null;
        DreamResponseDTO dto = new DreamResponseDTO();
        dto.setId(dream.getId());
        dto.setTitle(dream.getTitle());
        dto.setDescription(dream.getDescription());
        dto.setDreamImageUrl(dream.getDreamImageUrl());
        dto.setResultImageUrl(dream.getResultImageUrl());
        dto.setAchievementComment(dream.getAchievementComment());
        dto.setStatus(dream.getStatus());
        dto.setCreatedAt(dream.getCreatedAt());
        dto.setUpdatedAt(dream.getUpdatedAt());
        dto.setGoals(goalMapper.toResponseDTOList(dream.getGoals()));
        return dto;
    }

    public Dream toEntity(DreamRequestDTO dto, User user) {
        return Dream.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dreamImageUrl(dto.getDreamImageUrl())
                .status(Dream.DreamStatus.IN_PROGRESS)
                .build();
    }

    public void updateEntity(Dream dream, DreamRequestDTO dto) {
        dream.setTitle(dto.getTitle());
        dream.setDescription(dto.getDescription());
        if (dto.getDreamImageUrl() != null) {
            dream.setDreamImageUrl(dto.getDreamImageUrl());
        }
    }
}