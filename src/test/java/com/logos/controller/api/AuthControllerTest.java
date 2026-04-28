package com.logos.controller.api;

import com.logos.dto.RegisterDTO;
import com.logos.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_withValidData_shouldReturnSuccess() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setDeathDate(LocalDate.of(2090, 1, 1));

        given(authService.register(any(RegisterDTO.class))).willReturn(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_withInvalidData_shouldReturnValidationErrors() throws Exception {
        RegisterDTO dto = new RegisterDTO();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void register_whenUserAlreadyExists_shouldReturnConflict() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existing");
        dto.setEmail("existing@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setDeathDate(LocalDate.of(2090, 1, 1));

        willThrow(new RuntimeException("Username already taken"))
                .given(authService).register(any(RegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }
}