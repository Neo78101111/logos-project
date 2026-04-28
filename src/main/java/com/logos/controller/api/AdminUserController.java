package com.logos.controller.api;

import com.logos.dto.AdminCreateUserDTO;
import com.logos.dto.AdminUserUpdateDTO;
import com.logos.dto.UserResponseDTO;
import com.logos.entity.User;
import com.logos.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        UserResponseDTO user = adminUserService.createUser(dto);
        return ResponseEntity
                .created(URI.create("/api/admin/users/" + user.getId()))
                .body(user);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<UserResponseDTO> users = adminUserService.getAllUsers(pageable);

        return ResponseEntity.ok(users);
    }

    @GetMapping(params = "role")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@RequestParam User.Role role) {
        List<UserResponseDTO> users = adminUserService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // Единый PATCH для обновления роли и статуса
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long userId,
                                                      @Valid @RequestBody AdminUserUpdateDTO updateDTO) {
        UserResponseDTO user = adminUserService.updateUser(userId, updateDTO);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        // Пока заглушка
        return ResponseEntity.ok().build();
    }
}
