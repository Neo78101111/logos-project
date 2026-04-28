package com.logos.repository;

import com.logos.entity.Day;
import com.logos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayRepository extends JpaRepository<Day, Long> {

    Optional<Day> findByUserAndDate(User user, LocalDate date);

    List<Day> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<Day> findByUser(User user);

    Optional<Day> findByUserIdAndDate(Long userId, LocalDate date);

    List<Day> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT d FROM Day d WHERE d.user.id = :userId AND d.evaluation IS NOT NULL")
    List<Day> findEvaluatedDaysByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM Day d WHERE d.user.id = :userId AND SIZE(d.tasks) > 0")
    List<Day> findDaysWithTasksByUserId(@Param("userId") Long userId);   // новое имя

    @Query("SELECT d FROM Day d WHERE d.user.id = :userId AND YEAR(d.date) = :year AND MONTH(d.date) = :month")
    List<Day> findByUserIdAndYearAndMonth(@Param("userId") Long userId,
                                          @Param("year") int year,
                                          @Param("month") int month);

    @Query("SELECT COUNT(d) FROM Day d WHERE d.user.id = :userId AND d.evaluation IS NOT NULL")
    Long countEvaluatedDaysByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(d) FROM Day d WHERE d.user.id = :userId")
    Long countDaysByUserId(@Param("userId") Long userId);
}