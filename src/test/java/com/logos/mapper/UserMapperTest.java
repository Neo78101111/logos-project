package com.logos.mapper;

import com.logos.dto.AdminCreateUserDTO;
import com.logos.dto.RegisterDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toResponseDto_shouldMapAllFields() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(LocalDate.of(2080, 1, 1))
                .role(User.Role.USER)
                .active(true)
                .build();

        UserResponseDTO dto = userMapper.toResponseDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getBirthDate()).isEqualTo("1990-01-01");
        assertThat(dto.getDeathDate()).isEqualTo("2080-01-01");
        assertThat(dto.getRole()).isEqualTo(User.Role.USER);
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void toEntity_fromRegisterDTO_shouldMap() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setDeathDate(LocalDate.of(2080, 1, 1));

        User user = userMapper.toEntity(dto);

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(user.getDeathDate()).isEqualTo(LocalDate.of(2080, 1, 1));
        // Поля, не заданные в DTO, получают значения по умолчанию из билдера сущности
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isEqualTo(User.Role.USER); // дефолт USER
        assertThat(user.getActive()).isTrue(); // дефолт true
    }

    @Test
    void toEntity_fromAdminCreateUserDTO_shouldMap() {
        AdminCreateUserDTO dto = new AdminCreateUserDTO();
        dto.setUsername("adminuser");
        dto.setEmail("admin@example.com");
        dto.setBirthDate(LocalDate.of(1980, 1, 1));
        dto.setDeathDate(LocalDate.of(2050, 1, 1));
        dto.setRole(User.Role.ADMIN);

        User user = userMapper.toEntity(dto);

        assertThat(user.getUsername()).isEqualTo("adminuser");
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1980, 1, 1));
        assertThat(user.getDeathDate()).isEqualTo(LocalDate.of(2050, 1, 1));
        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
        // password не задан, active по умолчанию true
        assertThat(user.getPassword()).isNull();
        assertThat(user.getActive()).isTrue();
    }

    @Test
    void toResponseDto_shouldReturnNullForNullUser() {
        assertThat(userMapper.toResponseDto(null)).isNull();
    }

    @Test
    void toEntity_fromRegisterDTO_shouldReturnNullForNullDTO() {
        assertThat(userMapper.toEntity((RegisterDTO) null)).isNull();
    }

    @Test
    void toEntity_fromAdminCreateUserDTO_shouldReturnNullForNullDTO() {
        assertThat(userMapper.toEntity((AdminCreateUserDTO) null)).isNull();
    }
}