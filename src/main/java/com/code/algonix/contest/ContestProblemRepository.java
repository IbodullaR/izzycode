package com.code.algonix.contest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {
    
    List<ContestProblem> findByContestIdOrderByOrderIndexAsc(Long contestId);
    
    List<ContestProblem> findByContestId(Long contestId);
    
    Optional<ContestProblem> findByContestIdAndProblemId(Long contestId, Long problemId);
    
    Optional<ContestProblem> findByContestIdAndSymbol(Long contestId, String symbol);
}
