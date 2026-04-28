package com.logos.controller.api;

import com.logos.dto.RegisterDTO;
import com.logos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Регистрация (AJAX)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO,
                                      BindingResult bindingResult) {
        log.info("Попытка регистрации: {}", registerDTO.getUsername());

        // Проверка валидации
        if (bindingResult.hasErrors()) {

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Ошибка валидации",
                            (existing, replacement) -> existing + ", " + replacement // если несколько сообщений на одно поле
                    ));
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Ошибки валидации",
                            "errors", errors
                    ));
        }



        try {
            authService.register(registerDTO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Регистрация успешна! Теперь вы можете войти."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }
}