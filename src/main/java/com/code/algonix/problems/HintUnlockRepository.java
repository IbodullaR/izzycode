package com.code.algonix.problems;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HintUnlockRepository extends JpaRepository<HintUnlock, Long> {
    boolean existsByUserIdAndProblemIdAndHintIndex(Long userId, Long problemId, Integer hintIndex);
    List<HintUnlock> findByUserIdAndProblemId(Long userId, Long problemId);
}
