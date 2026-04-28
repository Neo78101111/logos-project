package com.logos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationEventListener {

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        log.info("Пользователь вошел: {}", event.getAuthentication().getName());
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication() != null ?
                event.getAuthentication().getName() : "unknown";
        log.warn("Неудачная попытка входа: {}", username);
    }

    @EventListener
    public void handleLogoutSuccess(LogoutSuccessEvent event) {
        if (event.getAuthentication() != null) {
            log.info("Пользователь вышел: {}", event.getAuthentication().getName());
        }
    }
}