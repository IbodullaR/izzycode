package com.code.algonix.contest;

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
@Table(name = "contest_submission_unlocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "contest_submission_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestSubmissionUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_submission_id", nullable = false)
    private ContestSubmission contestSubmission;

    @Builder.Default
    private Integer coinsSpent = 25;

    @CreationTimestamp
    private LocalDateTime unlockedAt;
}
