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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Создание пользователя при регистрации
     */
    @Transactional
    public UserResponseDTO createUserFromRegistration(RegisterDTO registerDTO) {

        log.info("Создание пользователя: {}", registerDTO.getUsername());

        // Проверка уникальности username
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new UsernameAlreadyExistsException(registerDTO.getUsername());
        }

        // Проверка уникальности email
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new EmailAlreadyExistsException(registerDTO.getEmail());
        }

        // Маппер создаёт сущность без пароля и роли
        User user = userMapper.toEntity(registerDTO);
        // 2. Устанавливаем поля, которых нет в DTO
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        log.info("Пользователь создан: {} (ID: {})",
                savedUser.getUsername(), savedUser.getId());

        return userMapper.toResponseDto(savedUser);
    }

    /**
     * Получить пользователя по ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return  userMapper.toResponseDto(user);
    }

    /**
     * Получить пользователя по username
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return userMapper.toResponseDto(user);
    }

    /**
     * Получить прокси пользователя по ID
     */
    @Transactional
    public User getUserReferenceById(Long id) {
        return userRepository.getReferenceById(id);
    }

    /**
     * Обновить профиль пользователя
     */
    @Transactional
    public UserResponseDTO updateUserProfile(Long userId, UserProfileUpdateDTO dto) {
        log.info("Обновление профиля пользователя ID={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Обновление username
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new UsernameAlreadyExistsException(dto.getUsername());
            }
            user.setUsername(dto.getUsername());
            log.debug("Username изменён на {}", dto.getUsername());
        }

        // Обновление email
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new EmailAlreadyExistsException(dto.getEmail());
            }
            user.setEmail(dto.getEmail());
            log.debug("Email изменён на {}", dto.getEmail());
        }

        // Обновление дат
        if (dto.getBirthDate() != null) {
            user.setBirthDate(dto.getBirthDate());
            log.debug("Дата рождения изменена на {}", dto.getBirthDate());
        }
        if (dto.getDeathDate() != null) {
            user.setDeathDate(dto.getDeathDate());
            log.debug("Дата смерти изменена на {}", dto.getDeathDate());
        }

        User updatedUser = userRepository.save(user);
        log.info("Профиль пользователя ID={} обновлён", userId);
        return userMapper.toResponseDto(updatedUser);
    }

    /**
     * Смена пароля
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Смена пароля для пользователя ID={}", userId);
        User user = userRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException(userId));

        // Проверка старого пароля
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Неудачная попытка смены пароля для ID={}: неверный старый пароль", userId);
            throw new InvalidPasswordException();
        }


        // Шифрование и сохранение
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Пароль успешно изменён для пользователя ID={}", userId);
    }




    /**
     * Проверить доступность username
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Проверить доступность email
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

}