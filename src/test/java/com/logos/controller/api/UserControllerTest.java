package com.logos.controller.api;

import com.logos.dto.ChangePasswordRequest;
import com.logos.dto.UserProfileUpdateDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.exception.InvalidPasswordException;
import com.logos.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final UserResponseDTO testUser = createTestUser(1L, "testuser", User.Role.USER);
    private final UserResponseDTO anotherUser = createTestUser(2L, "other", User.Role.USER);
    private final UserResponseDTO adminUser = createTestUser(3L, "admin", User.Role.ADMIN);

    private UserResponseDTO createTestUser(Long id, String username, User.Role role) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(username + "@example.com");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setDeathDate(LocalDate.of(2090, 1, 1));
        dto.setRole(role);
        dto.setActive(true);
        return dto;
    }

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);

        mockMvc.perform(get("/api/users/me")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserById_asSameUser_shouldReturnUser() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(userService.getUserById(1L)).willReturn(testUser);

        mockMvc.perform(get("/api/users/1")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserById_asAdmin_shouldReturnOtherUser() throws Exception {
        given(userService.getUserByUsername("admin")).willReturn(adminUser);
        given(userService.getUserById(2L)).willReturn(anotherUser);

        mockMvc.perform(get("/api/users/2")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void getUserById_asDifferentUser_shouldReturnForbidden() throws Exception {
        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(userService.getUserById(2L)).willReturn(anotherUser);

        mockMvc.perform(get("/api/users/2")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_shouldReturnUpdatedUser() throws Exception {
        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("new@example.com");
        updateDTO.setBirthDate(LocalDate.of(1991, 2, 2));
        updateDTO.setDeathDate(LocalDate.of(2091, 2, 2));

        UserResponseDTO updated = createTestUser(1L, "testuser", User.Role.USER);
        updated.setEmail("new@example.com");
        updated.setBirthDate(LocalDate.of(1991, 2, 2));
        updated.setDeathDate(LocalDate.of(2091, 2, 2));

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        given(userService.updateUserProfile(eq(1L), any(UserProfileUpdateDTO.class)))
                .willReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1991-02-02"))
                .andExpect(jsonPath("$.deathDate").value("2091-02-02"));
    }

    @Test
    void checkUsername_whenAvailable_shouldReturnTrue() throws Exception {
        given(userService.isUsernameAvailable("newuser")).willReturn(true);

        mockMvc.perform(get("/api/users/username-availability")
                        .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void checkUsername_whenTaken_shouldReturnFalse() throws Exception {
        given(userService.isUsernameAvailable("testuser")).willReturn(false);

        mockMvc.perform(get("/api/users/username-availability")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void checkEmail_whenAvailable_shouldReturnTrue() throws Exception {
        given(userService.isEmailAvailable("new@example.com")).willReturn(true);

        mockMvc.perform(get("/api/users/email-availability")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");
        request.setConfirmPassword("newPass123");

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        doNothing().when(userService).changePassword(eq(1L), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/users/me/password")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Пароль успешно изменён"));
    }

    @Test
    void changePassword_withInvalidOldPassword_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass123");
        request.setConfirmPassword("newPass123");

        given(userService.getUserByUsername("testuser")).willReturn(testUser);
        doThrow(new InvalidPasswordException())
                .when(userService).changePassword(eq(1L), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/users/me/password")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}