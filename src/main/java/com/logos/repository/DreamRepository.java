package com.logos.repository;

import com.logos.entity.Dream;
import com.logos.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DreamRepository extends JpaRepository<Dream, Long> {
    List<Dream> findByUser(User user, Sort sort);

    // Загружаем мечту вместе с целями (для одного запроса)
    @Query("SELECT d FROM Dream d LEFT JOIN FETCH d.goals WHERE d.id = :id")
    Optional<Dream> findByIdWithGoals(@Param("id") Long id);

    // Загружаем все мечты пользователя вместе с их целями
    @Query("SELECT d FROM Dream d LEFT JOIN FETCH d.goals WHERE d.user = :user")
    List<Dream> findByUserWithGoals(@Param("user") User user, Sort sort);
}