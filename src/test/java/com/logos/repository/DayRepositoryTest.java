package com.logos.repository;

import com.logos.entity.Day;
import com.logos.entity.DayEvaluation;
import com.logos.entity.DayTask;
import com.logos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DayRepositoryTest {

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .birthDate(LocalDate.of(2000,1,1))
                .deathDate(LocalDate.of(2100,1,1))
                .role(User.Role.USER)
                .active(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    void findByUserIdAndDate_shouldReturnDayIfExists() {
        // given
        LocalDate date = LocalDate.now();
        Day day = Day.builder().user(user).date(date).tasks(new ArrayList<>()).build(); // goals → tasks
        entityManager.persist(day);

        // when
        Optional<Day> found = dayRepository.findByUserIdAndDate(user.getId(), date);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(day.getId());
    }

    @Test
    void findByUserIdAndDate_shouldReturnEmptyIfNotExists() {
        // when
        Optional<Day> found = dayRepository.findByUserIdAndDate(user.getId(), LocalDate.now());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdAndDateBetween_shouldReturnDaysInRange() {
        // given
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 10);
        Day day1 = Day.builder().user(user).date(LocalDate.of(2025, 3, 5)).tasks(new ArrayList<>()).build();
        Day day2 = Day.builder().user(user).date(LocalDate.of(2025, 3, 8)).tasks(new ArrayList<>()).build();
        Day day3 = Day.builder().user(user).date(LocalDate.of(2025, 3, 15)).tasks(new ArrayList<>()).build(); // вне диапазона
        entityManager.persist(day1);
        entityManager.persist(day2);
        entityManager.persist(day3);

        // when
        List<Day> days = dayRepository.findByUserIdAndDateBetween(user.getId(), start, end);

        // then
        assertThat(days).hasSize(2).extracting(Day::getId)
                .containsExactlyInAnyOrder(day1.getId(), day2.getId());
    }

    @Test
    void findByUserIdAndYearAndMonth_shouldReturnDaysOfMonth() {
        // given
        Day day1 = Day.builder().user(user).date(LocalDate.of(2025, 3, 10)).tasks(new ArrayList<>()).build();
        Day day2 = Day.builder().user(user).date(LocalDate.of(2025, 3, 20)).tasks(new ArrayList<>()).build();
        Day day3 = Day.builder().user(user).date(LocalDate.of(2025, 4, 1)).tasks(new ArrayList<>()).build();
        entityManager.persist(day1);
        entityManager.persist(day2);
        entityManager.persist(day3);

        // when
        List<Day> days = dayRepository.findByUserIdAndYearAndMonth(user.getId(), 2025, 3);

        // then
        assertThat(days).hasSize(2).extracting(Day::getId)
                .containsExactlyInAnyOrder(day1.getId(), day2.getId());
    }

    @Test
    void findEvaluatedDaysByUserId_shouldReturnOnlyDaysWithEvaluation() {
        // given
        DayEvaluation eval = DayEvaluation.builder()
                .daySatisfactionScore(5)
                .wisdomScore(5)
                .courageScore(5)
                .temperanceScore(5)
                .justiceScore(5)
                .build();
        Day dayWithEval = Day.builder().user(user).date(LocalDate.now())
                .evaluation(eval).tasks(new ArrayList<>()).build();
        Day dayWithoutEval = Day.builder().user(user).date(LocalDate.now().plusDays(1))
                .tasks(new ArrayList<>()).build();
        entityManager.persist(dayWithEval);
        entityManager.persist(dayWithoutEval);
        entityManager.flush();

        // when
        List<Day> evaluated = dayRepository.findEvaluatedDaysByUserId(user.getId());

        // then
        assertThat(evaluated).hasSize(1).extracting(Day::getId).containsExactly(dayWithEval.getId());
    }

    @Test
    void findDaysWithTasksByUserId_shouldReturnDaysHavingTasks() { // переименовано с findDaysWithGoalsByUserId
        // given
        DayTask task = DayTask.builder().title("Task").build(); // Goal → Task
        Day dayWithTask = Day.builder().user(user).date(LocalDate.now())
                .tasks(List.of(task)).build(); // goals → tasks
        Day dayWithoutTask = Day.builder().user(user).date(LocalDate.now().plusDays(1))
                .tasks(new ArrayList<>()).build();
        entityManager.persist(dayWithTask);
        entityManager.persist(dayWithoutTask);

        // when
        List<Day> withTasks = dayRepository.findDaysWithTasksByUserId(user.getId()); // переименовано

        // then
        assertThat(withTasks).hasSize(1).extracting(Day::getId).containsExactly(dayWithTask.getId());
    }

    @Test
    void countEvaluatedDaysByUserId_shouldReturnCorrectCount() {
        // given
        DayEvaluation eval1 = DayEvaluation.builder()
                .daySatisfactionScore(5)
                .wisdomScore(5)
                .courageScore(5)
                .temperanceScore(5)
                .justiceScore(5)
                .build();
        DayEvaluation eval2 = DayEvaluation.builder()
                .daySatisfactionScore(6)
                .wisdomScore(6)
                .courageScore(6)
                .temperanceScore(6)
                .justiceScore(6)
                .build();
        Day day1 = Day.builder().user(user).date(LocalDate.now()).evaluation(eval1)
                .tasks(new ArrayList<>()).build();
        Day day2 = Day.builder().user(user).date(LocalDate.now().plusDays(1)).evaluation(eval2)
                .tasks(new ArrayList<>()).build();
        Day day3 = Day.builder().user(user).date(LocalDate.now().plusDays(2))
                .tasks(new ArrayList<>()).build();
        entityManager.persist(day1);
        entityManager.persist(day2);
        entityManager.persist(day3);

        // when
        Long count = dayRepository.countEvaluatedDaysByUserId(user.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countDaysByUserId_shouldReturnTotalDays() {
        // given
        Day day1 = Day.builder().user(user).date(LocalDate.now()).tasks(new ArrayList<>()).build();
        Day day2 = Day.builder().user(user).date(LocalDate.now().plusDays(1)).tasks(new ArrayList<>()).build();
        entityManager.persist(day1);
        entityManager.persist(day2);

        // when
        Long count = dayRepository.countDaysByUserId(user.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }
}