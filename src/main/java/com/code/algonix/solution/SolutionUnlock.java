package com.code.algonix.solution;

import com.code.algonix.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "solution_unlocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "solution_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    private Solution solution;

    @Builder.Default
    private Integer coinsSpent = 25;

    @CreationTimestamp
    private LocalDateTime unlockedAt;
}
