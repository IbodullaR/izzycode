package com.code.algonix.contest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    
    Optional<Contest> findByNumber(String number);
    
    List<Contest> findByStatusOrderByStartTimeDesc(Contest.ContestStatus status);
    
    @Query("SELECT c FROM Contest c WHERE c.startTime > :now ORDER BY c.startTime ASC")
    List<Contest> findUpcomingContests(LocalDateTime now);
    
    @Query("SELECT c FROM Contest c WHERE c.startTime <= :now AND " +
           "FUNCTION('TIMESTAMPADD', SECOND, c.durationSeconds, c.startTime) > :now")
    List<Contest> findActiveContests(LocalDateTime now);
    
    @Query("SELECT c FROM Contest c WHERE " +
           "FUNCTION('TIMESTAMPADD', SECOND, c.durationSeconds, c.startTime) <= :now " +
           "ORDER BY c.startTime DESC")
    List<Contest> findFinishedContests(LocalDateTime now);
    
    @Query("SELECT c FROM Contest c WHERE c.startTime >= :weekAgo ORDER BY c.startTime DESC")
    List<Contest> findWeeklyContests(LocalDateTime weekAgo);
}
