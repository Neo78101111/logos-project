package com.logos.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super(
                String.format("Email '%s' уже используется", email),
                HttpStatus.CONFLICT,
                "EMAIL_ALREADY_EXISTS"
        );
    }
}
