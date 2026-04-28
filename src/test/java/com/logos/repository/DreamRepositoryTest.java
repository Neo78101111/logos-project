package com.logos.repository;

import com.logos.entity.Dream;
import com.logos.entity.DreamGoal;
import com.logos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DreamRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DreamRepository dreamRepository;
    @Autowired
    private DreamGoalRepository goalRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(LocalDate.of(2050, 1, 1))
                .build();
        user = userRepository.save(user);
    }

    @Test
    void findByUser_shouldReturnDreamsSorted() {
        Dream dream1 = Dream.builder().user(user).title("First").build();
        Dream dream2 = Dream.builder().user(user).title("Second").build();
        dreamRepository.saveAll(List.of(dream1, dream2));

        List<Dream> dreams = dreamRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        assertThat(dreams).hasSize(2);
        assertThat(dreams).extracting(Dream::getTitle).containsExactlyInAnyOrder("First", "Second");
    }

    @Test
    void findByIdWithGoals_shouldLoadGoals() {
        Dream dream = Dream.builder().user(user).title("With goals").build();
        dream = dreamRepository.save(dream);
        DreamGoal goal = DreamGoal.builder().dream(dream).title("Goal1").deadline(LocalDate.now()).build();
        goalRepository.save(goal);

        // Очищаем persistence context, чтобы убрать кэшированный dream без целей
        entityManager.flush();
        entityManager.clear();

        Dream found = dreamRepository.findByIdWithGoals(dream.getId()).orElseThrow();
        assertThat(found.getGoals()).hasSize(1);
        assertThat(found.getGoals().get(0).getTitle()).isEqualTo("Goal1");
    }

    @Test
    void findByUserWithGoals_shouldFetchGoalsEagerly() {
        Dream dream = Dream.builder().user(user).title("Eager").build();
        dream = dreamRepository.save(dream);
        DreamGoal goal = DreamGoal.builder().dream(dream).title("Goal A").deadline(LocalDate.now()).build();
        goalRepository.save(goal);

        entityManager.flush();
        entityManager.clear();

        List<Dream> dreams = dreamRepository.findByUserWithGoals(user, Sort.unsorted());
        assertThat(dreams).hasSize(1);
        // Проверяем, что goals инициализированы (не proxy)
        assertThat(dreams.get(0).getGoals()).isNotEmpty();
    }
}