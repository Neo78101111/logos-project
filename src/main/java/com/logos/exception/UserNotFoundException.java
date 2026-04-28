package com.logos.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long id) {
        super(
                String.format("Пользователь с ID %d не найден", id),
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND"
        );
    }

    public UserNotFoundException(String username) {
        super(
                String.format("Пользователь '%s' не найден", username),
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND"
        );
    }
}
