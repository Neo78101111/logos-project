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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponseDTO createUser(AdminCreateUserDTO dto) {
        log.info("Администратор создает пользователя: {}", dto.getUsername());

        // Проверка уникальности
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException(dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }


        User user = userMapper.toEntity(dto);
        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setActive(true);
        User savedUser = userRepository.save(user);

        log.info("Создан пользователь администратором: {} (ID: {}, Role: {})",
                savedUser.getUsername(), savedUser.getId(), savedUser.getRole());

        // TODO: отправить временный пароль
        return userMapper.toResponseDto(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(User.Role role) {

        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponseDTO updateUser(Long userId, AdminUserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (updateDTO.getRole() != null) {
            user.setRole(updateDTO.getRole());
            log.info("Роль пользователя {} изменена на {}", user.getUsername(), updateDTO.getRole());
        }

        if (updateDTO.getActive() != null) {
            user.setActive(updateDTO.getActive());
            log.info("Статус активности пользователя {} изменён на {}", user.getUsername(), updateDTO.getActive());
        }

        return userMapper.toResponseDto(userRepository.save(user));
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
