package com.code.algonix.interview;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<InterviewSession> findByUserIdAndStatus(Long userId, InterviewSession.SessionStatus status);
    boolean existsByUserIdAndStatus(Long userId, InterviewSession.SessionStatus status);
}
