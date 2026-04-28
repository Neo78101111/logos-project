package com.logos.repository;

import com.logos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPass")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(LocalDate.of(2080, 1, 1))
                .role(User.Role.USER)
                .active(true)
                .build();

        user2 = User.builder()
                .username("jane_admin")
                .email("jane@example.com")
                .password("encodedPass")
                .birthDate(LocalDate.of(1985, 5, 10))
                .deathDate(LocalDate.of(2075, 5, 10))
                .role(User.Role.ADMIN)
                .active(false)
                .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
    }

    @Test
    void findByUsername_shouldReturnUser() {
        Optional<User> found = userRepository.findByUsername("john_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnUser() {
        Optional<User> found = userRepository.findByEmail("jane@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane_admin");
    }

    @Test
    void existsByUsername_shouldReturnTrueForExisting() {
        boolean exists = userRepository.existsByUsername("john_doe");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalseForNonExisting() {
        boolean exists = userRepository.existsByUsername("unknown");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_shouldReturnTrueForExisting() {
        boolean exists = userRepository.existsByEmail("jane@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalseForNonExisting() {
        boolean exists = userRepository.existsByEmail("unknown@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void findByRole_shouldReturnUsersWithGivenRole() {
        List<User> users = userRepository.findByRole(User.Role.USER);
        assertThat(users).hasSize(1).extracting(User::getUsername).containsExactly("john_doe");

        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        assertThat(admins).hasSize(1).extracting(User::getUsername).containsExactly("jane_admin");
    }

    @Test
    void countByRole_shouldReturnCorrectCount() {
        long userCount = userRepository.countByRole(User.Role.USER);
        assertThat(userCount).isEqualTo(1);

        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        assertThat(adminCount).isEqualTo(1);
    }

    @Test
    void findByActiveTrue_shouldReturnActiveUsers() {
        List<User> activeUsers = userRepository.findByActiveTrue();
        assertThat(activeUsers).hasSize(1).extracting(User::getUsername).containsExactly("john_doe");
    }

    @Test
    void findByActiveFalse_shouldReturnInactiveUsers() {
        List<User> inactiveUsers = userRepository.findByActiveFalse();
        assertThat(inactiveUsers).hasSize(1).extracting(User::getUsername).containsExactly("jane_admin");
    }
}