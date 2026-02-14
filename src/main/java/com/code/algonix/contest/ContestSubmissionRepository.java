package com.code.algonix.contest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, Long> {
    
    List<ContestSubmission> findByContestIdAndUserIdOrderBySubmittedAtDesc(Long contestId, Long userId);
    
    List<ContestSubmission> findByContestIdOrderBySubmittedAtDesc(Long contestId);
    
    List<ContestSubmission> findByContestIdAndContestProblemId(Long contestId, Long contestProblemId);
    
    @Query("SELECT cs FROM ContestSubmission cs WHERE cs.contest.id = :contestId " +
           "AND cs.user.id = :userId AND cs.contestProblem.id = :contestProblemId " +
           "ORDER BY cs.submittedAt DESC")
    List<ContestSubmission> findUserProblemSubmissions(Long contestId, Long userId, Long contestProblemId);
    
    boolean existsByContestIdAndUserIdAndContestProblemIdAndIsAcceptedTrue(
            Long contestId, Long userId, Long contestProblemId);
    
    long countByContestIdAndUserIdAndContestProblemIdAndSubmittedAtLessThanEqual(
            Long contestId, Long userId, Long contestProblemId, LocalDateTime submittedAt);
    
    long countByContestIdAndUserIdAndContestProblemIdAndIsAcceptedFalseAndSubmittedAtBefore(
            Long contestId, Long userId, Long contestProblemId, LocalDateTime submittedAt);
}
