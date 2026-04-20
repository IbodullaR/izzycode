package com.code.algonix.daily;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyChallengeCompletionRepository extends JpaRepository<DailyChallengeCompletion, Long> {
    boolean existsByUserIdAndDailyChallengeId(Long userId, Long dailyChallengeId);
    List<DailyChallengeCompletion> findByUserIdOrderByCompletedAtDesc(Long userId);
}
