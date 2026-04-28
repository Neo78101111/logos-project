package com.logos.dto;

import com.logos.entity.Dream.DreamStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DreamResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String dreamImageUrl;
    private String resultImageUrl;
    private String achievementComment;
    private DreamStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DreamGoalResponseDTO> goals;
}