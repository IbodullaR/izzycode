package com.code.algonix.solution;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {
    List<Solution> findByProblemIdAndPublishedTrueOrderByLikesDesc(Long problemId);
    boolean existsByAuthorIdAndProblemId(Long authorId, Long problemId);
}
