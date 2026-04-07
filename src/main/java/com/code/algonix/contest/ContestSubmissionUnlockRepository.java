package com.code.algonix.contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestSubmissionUnlockRepository extends JpaRepository<ContestSubmissionUnlock, Long> {
    boolean existsByUserIdAndContestSubmissionId(Long userId, Long contestSubmissionId);
}
