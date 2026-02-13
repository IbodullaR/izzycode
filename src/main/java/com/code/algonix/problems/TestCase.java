package com.code.algonix.problems;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(columnDefinition = "TEXT")
    private String input; // JSON format

    @Column(columnDefinition = "TEXT")
    private String expectedOutput; // JSON format

    private Boolean isHidden = false; // yashirin test case
    private Integer timeLimitMs = 2000; // default 2 seconds
    private Integer orderIndex;
}
