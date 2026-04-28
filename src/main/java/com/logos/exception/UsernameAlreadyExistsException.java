package com.logos.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends BusinessException {

    public UsernameAlreadyExistsException(String username) {
        super(
                String.format("Имя пользователя '%s' уже занято", username),
                HttpStatus.CONFLICT,
                "USERNAME_ALREADY_EXISTS"
        );
    }
}