package com.code.algonix.solution;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionUnlockRepository extends JpaRepository<SolutionUnlock, Long> {
    boolean existsByUserIdAndSolutionId(Long userId, Long solutionId);
    Optional<SolutionUnlock> findByUserIdAndSolutionId(Long userId, Long solutionId);
}
