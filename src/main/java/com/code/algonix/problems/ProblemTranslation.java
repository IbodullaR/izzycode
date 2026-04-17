package com.code.algonix.problems;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problem_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id", "language"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 10)
    private String language; // "uz", "en", "ru"

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String descriptionHtml;
}
