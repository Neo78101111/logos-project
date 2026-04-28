package com.logos.repository;

import com.logos.entity.DreamGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DreamGoalRepository extends JpaRepository<DreamGoal, Long> {
}