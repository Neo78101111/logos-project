package com.logos.dto;

import com.logos.entity.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private User.Role role;
    private boolean active;
}
