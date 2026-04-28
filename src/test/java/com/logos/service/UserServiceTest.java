package com.logos.service;

import com.logos.dto.ChangePasswordRequest;
import com.logos.dto.RegisterDTO;
import com.logos.dto.UserProfileUpdateDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.exception.EmailAlreadyExistsException;
import com.logos.exception.InvalidPasswordException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private RegisterDTO registerDTO;
    private User userEntity;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("rawPassword");
        registerDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        registerDTO.setDeathDate(LocalDate.of(2080, 1, 1));

        userEntity = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(LocalDate.of(2080, 1, 1))
                .role(User.Role.USER)
                .active(true)
                .build();

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setUsername("testuser");
        userResponseDTO.setEmail("test@example.com");
    }

    @Test
    void createUserFromRegistration_shouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        when(userMapper.toEntity(registerDTO)).thenReturn(userEntity);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponseDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUserFromRegistration(registerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(userEntity);
    }

    @Test
    void createUserFromRegistration_shouldThrowWhenUsernameExists() {
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserFromRegistration(registerDTO))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("testuser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserFromRegistration_shouldThrowWhenEmailExists() {
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserFromRegistration(registerDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponseDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserByUsername_shouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponseDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserByUsername_shouldThrowWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUserProfile_shouldUpdateEmail() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("old@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(LocalDate.of(2080, 1, 1))
                .build();

        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("new@example.com");
        updateDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        updateDTO.setDeathDate(LocalDate.of(2080, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toResponseDto(existingUser)).thenReturn(userResponseDTO);

        userService.updateUserProfile(1L, updateDTO);

        assertThat(existingUser.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserProfile_shouldThrowWhenEmailAlreadyExists() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("old@example.com")
                .build();

        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("new@example.com");
        updateDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        updateDTO.setDeathDate(LocalDate.of(2080, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUserProfile(1L, updateDTO))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateUserProfile_shouldUpdateUsername() {
        User existingUser = User.builder()
                .id(1L)
                .username("olduser")
                .email("test@example.com")
                .build();

        UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
        updateDTO.setUsername("newuser");
        updateDTO.setEmail("test@example.com");
        updateDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        updateDTO.setDeathDate(LocalDate.of(2080, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toResponseDto(existingUser)).thenReturn(userResponseDTO);

        userService.updateUserProfile(1L, updateDTO);

        assertThat(existingUser.getUsername()).isEqualTo("newuser");
    }

    @Test
    void isUsernameAvailable_shouldReturnTrueWhenNotExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        assertThat(userService.isUsernameAvailable("newuser")).isTrue();
    }

    @Test
    void isEmailAvailable_shouldReturnTrueWhenNotExists() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        assertThat(userService.isEmailAvailable("new@example.com")).isTrue();
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        User existingUser = User.builder()
                .id(1L)
                .password("encodedOldPass")
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("rawOldPass");
        request.setNewPassword("newRawPass");
        request.setConfirmPassword("newRawPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("rawOldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newRawPass")).thenReturn("encodedNewPass");

        userService.changePassword(1L, request);

        verify(userRepository).save(existingUser);
        assertThat(existingUser.getPassword()).isEqualTo("encodedNewPass");
    }

    @Test
    void changePassword_shouldThrowWhenOldPasswordIncorrect() {
        User existingUser = User.builder().id(1L).password("encodedOldPass").build();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(InvalidPasswordException.class);
    }
}