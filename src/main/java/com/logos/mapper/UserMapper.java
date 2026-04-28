package com.logos.mapper;

import com.logos.dto.AdminCreateUserDTO;
import com.logos.dto.RegisterDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {


    public UserResponseDTO toResponseDto(User user) {
        if (user == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBirthDate(user.getBirthDate());
        dto.setDeathDate(user.getDeathDate());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        return dto;
    }

    public User toEntity(RegisterDTO dto) {
        if (dto == null) return null;
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .deathDate(dto.getDeathDate())
                .build();
    }

    // Для административного создания пользователя (AdminCreateUserDTO -> User)
    public User toEntity(AdminCreateUserDTO dto) {
        if (dto == null) return null;
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .deathDate(dto.getDeathDate())
                .role(dto.getRole())
                .build();
    }
}
