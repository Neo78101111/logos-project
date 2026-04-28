package com.logos.service;

import com.logos.dto.RegisterDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public UserResponseDTO register(RegisterDTO registerDTO) {
        log.debug("Начало регистрации пользователя: {}", registerDTO.getUsername());

        // Создаем пользователя через UserService
        UserResponseDTO userDto = userService.createUserFromRegistration(registerDTO);

        // Дополнительные действия при регистрации:
        // - Отправка welcome email
        // - Создание дефолтных настроек
        //...................................
        log.debug("Регистрация пользователя {} завершена", registerDTO.getUsername());
        return userDto;
    }
}