package com.logos.controller.api;

import com.logos.dto.ChangePasswordRequest;
import com.logos.dto.UserProfileUpdateDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.exception.InvalidPasswordException;
import com.logos.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получить текущего пользователя
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDTO user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    /**
     * Получить пользователя по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        // Проверяем, что текущий пользователь имеет доступ
        UserResponseDTO currentUser = userService.getUserByUsername(userDetails.getUsername());
        UserResponseDTO requestedUser = userService.getUserById(id);

        // Только админ или сам пользователь может получить данные
        if (!currentUser.getId().equals(id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(requestedUser);
    }

    /**
     * Обновить профиль пользователя
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateDTO updates,
            HttpServletRequest request) {

        UserResponseDTO currentUser = userService.getUserByUsername(userDetails.getUsername());

        UserResponseDTO updatedUser = userService.updateUserProfile(currentUser.getId(), updates);

        // Если username изменился – принудительный выход
        if (!currentUser.getUsername().equals(updatedUser.getUsername())) {
            // Очищаем контекст безопасности
            SecurityContextHolder.clearContext();
            // Инвалидируем сессию
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Имя пользователя изменено. Выполните вход заново."));
        }

        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        UserResponseDTO currentUser = userService.getUserByUsername(userDetails.getUsername());

        try {
            userService.changePassword(currentUser.getId(), request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Пароль успешно изменён"));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Проверить доступность username
     */
    @GetMapping("/username-availability")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * Проверить доступность email
     */
    @GetMapping("/email-availability")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }
}