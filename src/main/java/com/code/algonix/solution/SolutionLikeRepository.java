package com.code.algonix.solution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionLikeRepository extends JpaRepository<SolutionLike, Long> {
    boolean existsByUserIdAndSolutionId(Long userId, Long solutionId);
    void deleteByUserIdAndSolutionId(Long userId, Long solutionId);
}
