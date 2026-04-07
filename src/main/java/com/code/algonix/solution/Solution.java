package com.code.algonix.solution;

import com.code.algonix.problems.Problem;
import com.code.algonix.problems.Submission;
import com.code.algonix.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solutions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // The accepted submission this solution is based on (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String language;

    // Code is hidden until unlocked — stored here
    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Builder.Default
    private Integer likes = 0;

    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    private Integer unlockCount = 0;

    @Builder.Default
    private boolean published = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "solution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SolutionUnlock> unlocks = new ArrayList<>();

    @OneToMany(mappedBy = "solution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SolutionLike> solutionLikes = new ArrayList<>();
}
