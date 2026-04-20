package com.code.algonix.gamification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndType(Long userId, Badge.BadgeType type);
}
