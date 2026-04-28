package com.logos.controller.api;

import com.logos.dto.AdminCreateUserDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.service.AdminUserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserService adminUserService;

    private final UserResponseDTO testUser = createTestUser(1L, "user1", User.Role.USER);
    private final UserResponseDTO testAdmin = createTestUser(2L, "admin", User.Role.ADMIN);

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
    void createUser_shouldReturnCreatedUser() throws Exception {
        AdminCreateUserDTO createDTO = new AdminCreateUserDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("new@example.com");
        createDTO.setBirthDate(LocalDate.of(1995, 5, 5));
        createDTO.setDeathDate(LocalDate.of(2095, 5, 5));
        createDTO.setRole(User.Role.USER);

        given(adminUserService.createUser(any(AdminCreateUserDTO.class))).willReturn(testUser);

        mockMvc.perform(post("/api/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/users/1"))
                .andExpect(jsonPath("$.id").value(testUser.getId()));
    }

    @Test
    void getAllUsers_shouldReturnPageOfUsers() throws Exception {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(testUser, testAdmin));
        given(adminUserService.getAllUsers(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(testUser.getId()));
    }

    @Test
    void getUsersByRole_shouldReturnUsersList() throws Exception {
        given(adminUserService.getUsersByRole(User.Role.USER)).willReturn(List.of(testUser));

        mockMvc.perform(get("/api/admin/users?role=USER")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    void updateUserRole_shouldReturnUpdatedUser() throws Exception {
        UserResponseDTO updated = createTestUser(1L, "user1", User.Role.ADMIN);
        given(adminUserService.updateUser(eq(1L), argThat(dto -> dto.getRole() == User.Role.ADMIN)))
                .willReturn(updated);

        mockMvc.perform(patch("/api/admin/users/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deactivateUser_shouldReturnNoContent() throws Exception {
        UserResponseDTO deactivated = createTestUser(1L, "user1", User.Role.USER);
        deactivated.setActive(false);
        given(adminUserService.updateUser(eq(1L), argThat(dto -> dto.getActive() != null && !dto.getActive())))
                .willReturn(deactivated);

        mockMvc.perform(patch("/api/admin/users/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserStats_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/admin/users/stats")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void endpoints_shouldReturnForbiddenWhenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }
}