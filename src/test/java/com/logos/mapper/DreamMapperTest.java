package com.logos.mapper;

import com.logos.dto.DreamGoalResponseDTO;
import com.logos.dto.DreamRequestDTO;
import com.logos.dto.DreamResponseDTO;
import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import com.logos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DreamMapperTest {

    @Mock
    private DreamGoalMapper goalMapper;

    @InjectMocks
    private DreamMapper dreamMapper;

    private User user;
    private Dream dream;
    private DreamGoal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test").build();
        goal = DreamGoal.builder().id(100L).title("Goal").build();
        dream = Dream.builder()
                .id(10L)
                .user(user)
                .title("My Dream")
                .description("Desc")
                .dreamImageUrl("img1.jpg")
                .resultImageUrl("img2.jpg")
                .achievementComment("Great")
                .status(Dream.DreamStatus.ACHIEVED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .goals(List.of(goal))
                .build();
    }

    @Test
    void toResponseDTO_shouldMapAllFields() {
        DreamGoalResponseDTO goalResponse = new DreamGoalResponseDTO();
        goalResponse.setId(100L);
        when(goalMapper.toResponseDTOList(dream.getGoals())).thenReturn(List.of(goalResponse));

        DreamResponseDTO dto = dreamMapper.toResponseDTO(dream);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo("My Dream");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.getDreamImageUrl()).isEqualTo("img1.jpg");
        assertThat(dto.getResultImageUrl()).isEqualTo("img2.jpg");
        assertThat(dto.getAchievementComment()).isEqualTo("Great");
        assertThat(dto.getStatus()).isEqualTo(Dream.DreamStatus.ACHIEVED);
        assertThat(dto.getGoals()).hasSize(1);
        assertThat(dto.getGoals().get(0).getId()).isEqualTo(100L);
    }

    @Test
    void toEntity_shouldCreateDreamFromDtoAndUser() {
        DreamRequestDTO dto = new DreamRequestDTO();
        dto.setTitle("New Dream");
        dto.setDescription("New Desc");
        dto.setDreamImageUrl("img.jpg");

        Dream entity = dreamMapper.toEntity(dto, user);

        assertThat(entity.getUser()).isEqualTo(user);
        assertThat(entity.getTitle()).isEqualTo("New Dream");
        assertThat(entity.getDescription()).isEqualTo("New Desc");
        assertThat(entity.getDreamImageUrl()).isEqualTo("img.jpg");
        assertThat(entity.getStatus()).isEqualTo(Dream.DreamStatus.IN_PROGRESS);
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Dream existing = Dream.builder().title("Old").description("Old Desc").dreamImageUrl("old.jpg").build();
        DreamRequestDTO dto = new DreamRequestDTO();
        dto.setTitle("Updated");
        dto.setDescription("Updated Desc");
        dto.setDreamImageUrl("new.jpg");

        dreamMapper.updateEntity(existing, dto);

        assertThat(existing.getTitle()).isEqualTo("Updated");
        assertThat(existing.getDescription()).isEqualTo("Updated Desc");
        assertThat(existing.getDreamImageUrl()).isEqualTo("new.jpg");
    }

    @Test
    void updateEntity_withNullImageUrl_shouldNotOverride() {
        Dream existing = Dream.builder().dreamImageUrl("old.jpg").build();
        DreamRequestDTO dto = new DreamRequestDTO();
        dto.setTitle("Title");
        dto.setDreamImageUrl(null);

        dreamMapper.updateEntity(existing, dto);

        assertThat(existing.getDreamImageUrl()).isEqualTo("old.jpg"); // not changed
    }
}