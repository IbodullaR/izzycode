package com.code.algonix.daily;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.code.algonix.user.UserEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_challenge_completions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "daily_challenge_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChallengeCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_challenge_id", nullable = false)
    private DailyChallenge dailyChallenge;

    private Integer bonusCoinsEarned;
    private Integer bonusXpEarned;

    @CreationTimestamp
    private LocalDateTime completedAt;
}
