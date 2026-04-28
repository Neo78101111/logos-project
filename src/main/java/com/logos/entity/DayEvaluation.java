package com.logos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "day_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // критерии оценки

    @Column(nullable = false)
    private Integer daySatisfactionScore; // общая удовлетворенность днем
    @Column(nullable = false)
    private Integer wisdomScore; // оценка Мудрости
    @Column(nullable = false)
    private Integer courageScore; // оценка Мужества
    @Column(nullable = false)
    private Integer temperanceScore; // оценка Умеренности
    @Column(nullable = false)
    private Integer justiceScore; // оценка Справедливости
    @Column(columnDefinition = "TEXT")
    private String comment;


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
}
