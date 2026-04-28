package com.logos.controller.api;

import com.logos.dto.*;
import com.logos.entity.Dream;
import com.logos.service.DreamService;
import com.logos.service.FileUploadService;
import com.logos.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DreamService dreamService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FileUploadService fileUploadService;

    private final UserResponseDTO testUser = createTestUser();

    private UserResponseDTO createTestUser() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setUsername("testuser");
        return dto;
    }

    private DreamResponseDTO createDreamResponse(Long id, String title) {
        DreamResponseDTO dto = new DreamResponseDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setStatus(Dream.DreamStatus.IN_PROGRESS);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    // GET /api/dreams
    @Test
    void getUserDreams_shouldReturnList() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        List<DreamResponseDTO> dreams = List.of(createDreamResponse(1L, "Dream1"));
        given(dreamService.getUserDreams(1L)).willReturn(dreams);

        mockMvc.perform(get("/api/dreams")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // GET /api/dreams/{dreamId}
    @Test
    void getDream_shouldReturnDream() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamResponseDTO dream = createDreamResponse(10L, "My dream");
        given(dreamService.getDream(10L, 1L)).willReturn(dream);

        mockMvc.perform(get("/api/dreams/10")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("My dream"));
    }

    // POST /api/dreams
    @Test
    void createDream_shouldReturnCreated() throws Exception {
        DreamRequestDTO request = new DreamRequestDTO();
        request.setTitle("New dream");
        request.setDescription("Desc");

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamResponseDTO response = createDreamResponse(5L, "New dream");
        given(dreamService.createDream(eq(1L), any(DreamRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/dreams")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    // PUT /api/dreams/{dreamId}
    @Test
    void updateDream_shouldReturnUpdated() throws Exception {
        DreamRequestDTO request = new DreamRequestDTO();
        request.setTitle("Updated");
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamResponseDTO response = createDreamResponse(10L, "Updated");
        given(dreamService.updateDream(eq(10L), eq(1L), any(DreamRequestDTO.class))).willReturn(response);

        mockMvc.perform(put("/api/dreams/10")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    // DELETE /api/dreams/{dreamId}
    @Test
    void deleteDream_shouldReturnNoContent() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        doNothing().when(dreamService).deleteDream(10L, 1L);

        mockMvc.perform(delete("/api/dreams/10")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // POST /api/dreams/{dreamId}/goals
    @Test
    void addGoal_shouldReturnCreated() throws Exception {
        DreamGoalRequestDTO goalRequest = new DreamGoalRequestDTO();
        goalRequest.setTitle("Learn Java");
        goalRequest.setDeadline(java.time.LocalDate.now().plusMonths(1));

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamGoalResponseDTO goalResponse = new DreamGoalResponseDTO();
        goalResponse.setId(100L);
        given(dreamService.addGoal(eq(10L), eq(1L), any(DreamGoalRequestDTO.class))).willReturn(goalResponse);

        mockMvc.perform(post("/api/dreams/10/goals")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    // PUT /api/dreams/{dreamId}/goals/{goalId}
    @Test
    void updateGoal_shouldReturnOk() throws Exception {
        DreamGoalRequestDTO goalRequest = new DreamGoalRequestDTO();
        goalRequest.setTitle("Updated goal");
        goalRequest.setDeadline(java.time.LocalDate.now());

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamGoalResponseDTO goalResponse = new DreamGoalResponseDTO();
        goalResponse.setId(200L);
        given(dreamService.updateGoal(eq(10L), eq(200L), eq(1L), any(DreamGoalRequestDTO.class)))
                .willReturn(goalResponse);

        mockMvc.perform(put("/api/dreams/10/goals/200")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200));
    }

    // PATCH /api/dreams/{dreamId}/goals/{goalId}/toggle
    @Test
    void toggleGoal_shouldReturnOk() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamGoalResponseDTO goalResponse = new DreamGoalResponseDTO();
        goalResponse.setId(200L);
        goalResponse.setCompleted(true);
        given(dreamService.toggleGoalComplete(eq(10L), eq(200L), eq(1L))).willReturn(goalResponse);

        mockMvc.perform(patch("/api/dreams/10/goals/200/toggle")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    // DELETE /api/dreams/{dreamId}/goals/{goalId}
    @Test
    void deleteGoal_shouldReturnNoContent() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        doNothing().when(dreamService).deleteGoal(10L, 300L, 1L);

        mockMvc.perform(delete("/api/dreams/10/goals/300")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // POST /api/dreams/{dreamId}/achieve
    @Test
    void achieveDream_withFileAndComment_shouldReturnOk() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        MockMultipartFile image = new MockMultipartFile("resultImage", "photo.jpg", "image/jpeg", "fake".getBytes());
        given(fileUploadService.uploadImage(any())).willReturn("/uploads/photo.jpg");
        DreamResponseDTO response = createDreamResponse(10L, "Done");
        given(dreamService.achieveDream(eq(10L), eq(1L), eq("/uploads/photo.jpg"), eq("Well done!")))
                .willReturn(response);

        mockMvc.perform(multipart("/api/dreams/10/achieve")
                        .file(image)
                        .param("achievementComment", "Well done!")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // POST /api/dreams/{dreamId}/upload-dream-image
    @Test
    void uploadDreamImage_shouldReturnImageUrl() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        DreamResponseDTO existingDream = createDreamResponse(10L, "Dream");
        given(dreamService.getDream(10L, 1L)).willReturn(existingDream);
        given(fileUploadService.uploadImage(any())).willReturn("/uploads/photo.jpg");
        given(dreamService.updateDream(eq(10L), eq(1L), any(DreamRequestDTO.class))).willReturn(existingDream);

        MockMultipartFile image = new MockMultipartFile("image", "dream.jpg", "image/jpeg", "data".getBytes());
        mockMvc.perform(multipart("/api/dreams/10/upload-dream-image")
                        .file(image)
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/uploads/photo.jpg"));
    }
}