package com.logos.service;

import com.logos.dto.AdminCreateUserDTO;
import com.logos.dto.AdminUserUpdateDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.exception.EmailAlreadyExistsException;
import com.logos.exception.UserNotFoundException;
import com.logos.exception.UsernameAlreadyExistsException;
import com.logos.mapper.UserMapper;
import com.logos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserService adminUserService;

    private AdminCreateUserDTO adminCreateUserDTO;
    private User userEntity;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        adminCreateUserDTO = new AdminCreateUserDTO();
        adminCreateUserDTO.setUsername("adminuser");
        adminCreateUserDTO.setEmail("admin@example.com");
        adminCreateUserDTO.setBirthDate(LocalDate.of(1980, 1, 1));
        adminCreateUserDTO.setDeathDate(LocalDate.of(2050, 1, 1));
        adminCreateUserDTO.setRole(User.Role.ADMIN);

        userEntity = User.builder()
                .id(1L)
                .username("adminuser")
                .email("admin@example.com")
                .password("encodedTemp")
                .birthDate(LocalDate.of(1980, 1, 1))
                .deathDate(LocalDate.of(2050, 1, 1))
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setUsername("adminuser");
        userResponseDTO.setEmail("admin@example.com");
        userResponseDTO.setRole(User.Role.ADMIN);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername(adminCreateUserDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(adminCreateUserDTO.getEmail())).thenReturn(false);
        when(userMapper.toEntity(adminCreateUserDTO)).thenReturn(userEntity);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTemp");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = adminUserService.createUser(adminCreateUserDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("adminuser");
        verify(userRepository).save(userEntity);
    }

    @Test
    void createUser_shouldThrowWhenUsernameExists() {
        when(userRepository.existsByUsername(adminCreateUserDTO.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(adminCreateUserDTO))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void createUser_shouldThrowWhenEmailExists() {
        when(userRepository.existsByUsername(adminCreateUserDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(adminCreateUserDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(adminCreateUserDTO))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateUser_shouldUpdateRoleOnly() {
        Long userId = 1L;
        User existingUser = User.builder().id(userId).role(User.Role.USER).active(true).build();
        AdminUserUpdateDTO updateDTO = new AdminUserUpdateDTO();
        updateDTO.setRole(User.Role.ADMIN);
        updateDTO.setActive(null); // не меняем

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponseDto(existingUser)).thenReturn(userResponseDTO);

        UserResponseDTO result = adminUserService.updateUser(userId, updateDTO);

        assertThat(existingUser.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(existingUser.getActive()).isTrue(); // не изменилось
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldDeactivateUserOnly() {
        Long userId = 1L;
        User existingUser = User.builder().id(userId).role(User.Role.USER).active(true).build();
        AdminUserUpdateDTO updateDTO = new AdminUserUpdateDTO();
        updateDTO.setRole(null);
        updateDTO.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponseDto(existingUser)).thenReturn(userResponseDTO);

        UserResponseDTO result = adminUserService.updateUser(userId, updateDTO);

        assertThat(existingUser.getActive()).isFalse();
        assertThat(existingUser.getRole()).isEqualTo(User.Role.USER); // не изменилось
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldUpdateBothRoleAndActive() {
        Long userId = 1L;
        User existingUser = User.builder().id(userId).role(User.Role.USER).active(true).build();
        AdminUserUpdateDTO updateDTO = new AdminUserUpdateDTO();
        updateDTO.setRole(User.Role.ADMIN);
        updateDTO.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponseDto(existingUser)).thenReturn(userResponseDTO);

        UserResponseDTO result = adminUserService.updateUser(userId, updateDTO);

        assertThat(existingUser.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(existingUser.getActive()).isFalse();
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldDoNothingWhenUpdateDtoHasNullFields() {
        Long userId = 1L;
        User existingUser = User.builder().id(userId).role(User.Role.USER).active(true).build();
        AdminUserUpdateDTO updateDTO = new AdminUserUpdateDTO(); // все поля null

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponseDto(existingUser)).thenReturn(userResponseDTO);

        UserResponseDTO result = adminUserService.updateUser(userId, updateDTO);

        assertThat(existingUser.getRole()).isEqualTo(User.Role.USER);
        assertThat(existingUser.getActive()).isTrue();
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        Long userId = 99L;
        AdminUserUpdateDTO updateDTO = new AdminUserUpdateDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateUser(userId, updateDTO))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(userEntity));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponseDto(userEntity)).thenReturn(userResponseDTO);

        Page<UserResponseDTO> result = adminUserService.getAllUsers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("adminuser");
    }

    @Test
    void getUsersByRole_shouldReturnList() {
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(List.of(userEntity));
        when(userMapper.toResponseDto(userEntity)).thenReturn(userResponseDTO);

        List<UserResponseDTO> result = adminUserService.getUsersByRole(User.Role.ADMIN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(User.Role.ADMIN);
    }
}