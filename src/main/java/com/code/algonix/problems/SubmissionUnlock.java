package com.code.algonix.problems;

import com.code.algonix.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submission_unlocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "submission_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Builder.Default
    private Integer coinsSpent = 25;

    @CreationTimestamp
    private LocalDateTime unlockedAt;
}
