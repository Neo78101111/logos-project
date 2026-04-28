package com.logos.controller.api;

import com.logos.dto.*;
import com.logos.service.DreamService;
import com.logos.service.FileUploadService;
import com.logos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dreams")
@RequiredArgsConstructor
public class DreamController {

    private final DreamService dreamService;
    private final UserService userService;
    private final FileUploadService fileUploadService;

    // Получить все мечты пользователя
    @GetMapping
    public ResponseEntity<List<DreamResponseDTO>> getUserDreams(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(dreamService.getUserDreams(userId));
    }

    // Получить одну мечту
    @GetMapping("/{dreamId}")
    public ResponseEntity<DreamResponseDTO> getDream(@PathVariable Long dreamId,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(dreamService.getDream(dreamId, userId));
    }

    // Создать мечту
    @PostMapping
    public ResponseEntity<DreamResponseDTO> createDream(@Valid @RequestBody DreamRequestDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        DreamResponseDTO dream = dreamService.createDream(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dream);
    }

    // Обновить мечту
    @PutMapping("/{dreamId}")
    public ResponseEntity<DreamResponseDTO> updateDream(@PathVariable Long dreamId,
                                                        @Valid @RequestBody DreamRequestDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(dreamService.updateDream(dreamId, userId, dto));
    }

    // Удалить мечту
    @DeleteMapping("/{dreamId}")
    public ResponseEntity<Void> deleteDream(@PathVariable Long dreamId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        dreamService.deleteDream(dreamId, userId);
        return ResponseEntity.noContent().build();
    }

    // Добавить цель
    @PostMapping("/{dreamId}/goals")
    public ResponseEntity<DreamGoalResponseDTO> addGoal(@PathVariable Long dreamId,
                                                        @Valid @RequestBody DreamGoalRequestDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(dreamService.addGoal(dreamId, userId, dto));
    }

    // Обновить цель
    @PutMapping("/{dreamId}/goals/{goalId}")
    public ResponseEntity<DreamGoalResponseDTO> updateGoal(@PathVariable Long dreamId,
                                                           @PathVariable Long goalId,
                                                           @Valid @RequestBody DreamGoalRequestDTO dto,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(dreamService.updateGoal(dreamId, goalId, userId, dto));
    }

    // Переключить выполнение цели
    @PatchMapping("/{dreamId}/goals/{goalId}/toggle")
    public ResponseEntity<DreamGoalResponseDTO> toggleGoal(@PathVariable Long dreamId,
                                                           @PathVariable Long goalId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(dreamService.toggleGoalComplete(dreamId, goalId, userId));
    }

    // Удалить цель
    @DeleteMapping("/{dreamId}/goals/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long dreamId,
                                           @PathVariable Long goalId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        dreamService.deleteGoal(dreamId, goalId, userId);
        return ResponseEntity.noContent().build();
    }

    // Завершить мечту (с фото-результатом и комментарием)
    @PostMapping("/{dreamId}/achieve")
    public ResponseEntity<DreamResponseDTO> achieveDream(@PathVariable Long dreamId,
                                                         @RequestParam(required = false) MultipartFile resultImage,
                                                         @RequestParam(required = false) String achievementComment,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        String resultImageUrl = null;
        if (resultImage != null && !resultImage.isEmpty()) {
            resultImageUrl = fileUploadService.uploadImage(resultImage);
        }
        DreamResponseDTO dream = dreamService.achieveDream(dreamId, userId, resultImageUrl, achievementComment);
        return ResponseEntity.ok(dream);
    }

    // Загрузить фото-ожидание для мечты (отдельный эндпоинт)
    @PostMapping("/{dreamId}/upload-dream-image")
    public ResponseEntity<Map<String, String>> uploadDreamImage(@PathVariable Long dreamId,
                                                                @RequestParam("image") MultipartFile image,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        DreamResponseDTO dream = dreamService.getDream(dreamId, userId);
        String imageUrl = fileUploadService.uploadImage(image);
        // обновляем мечту, сохраняя URL
        DreamRequestDTO updateDto = new DreamRequestDTO();
        updateDto.setTitle(dream.getTitle());
        updateDto.setDescription(dream.getDescription());
        updateDto.setDreamImageUrl(imageUrl);
        dreamService.updateDream(dreamId, userId, updateDto);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}