package com.logos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dreams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "dream_image_url")
    private String dreamImageUrl;   // фото-ожидание

    @Column(name = "result_image_url")
    private String resultImageUrl;  // фото-результат

    @Column(name = "achievement_comment", columnDefinition = "TEXT")
    private String achievementComment; // итоговый комментарий при достижении

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DreamStatus status = DreamStatus.IN_PROGRESS;

    @OneToMany(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DreamGoal> goals = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DreamStatus {
        IN_PROGRESS, ACHIEVED
    }
}