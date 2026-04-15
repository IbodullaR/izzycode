package com.code.algonix.problems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionUnlockRepository extends JpaRepository<SubmissionUnlock, Long> {
    boolean existsByUserIdAndSubmissionId(Long userId, Long submissionId);
}
