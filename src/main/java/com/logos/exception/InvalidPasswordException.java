package com.logos.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException() {
        super(
                "Неверный пароль",
                HttpStatus.BAD_REQUEST,
                "INVALID_PASSWORD"
        );
    }

}
