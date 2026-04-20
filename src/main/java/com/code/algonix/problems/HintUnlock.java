package com.code.algonix.problems;

import com.code.algonix.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hint_unlocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "problem_id", "hint_index"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HintUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer hintIndex; // 0, 1, 2

    @Builder.Default
    private Integer coinsSpent = 5;

    @CreationTimestamp
    private LocalDateTime unlockedAt;
}
