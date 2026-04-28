package com.logos.repository;

import com.logos.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по имени
    Optional<User> findByUsername(String username);

    // Найти пользователя по email
    Optional<User> findByEmail(String email);

    // Проверить, существует ли пользователь с таким именем
    boolean existsByUsername(String username);

    // Проверить, существует ли пользователь с таким email
    boolean existsByEmail(String email);

    // Методы для админки
    List<User> findByRole(User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();
}