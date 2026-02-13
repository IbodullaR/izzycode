package com.code.algonix.contest.dto;

import com.code.algonix.problems.dto.SubmissionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestSubmissionResponse {
    private Long submissionId;
    private Long contestSubmissionId;
    private Long userId;
    private Long problemId;
    private String code;
    private String language;
    private String status;
    private Boolean isAccepted;
    private Integer score;
    private Long timeTaken;
    private List<TestResult> testResults;
    private OverallStats overallStats;
    private LocalDateTime submittedAt;
    private LocalDateTime judgedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResult {
        private Long testCaseId;
        private String status;
        private Long runtime;
        private Double memory;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String errorMessage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        private Integer totalTestCases;
        private Integer passedTestCases;
        private Long runtime;
        private Double runtimePercentile;
        private Double memory;
        private Double memoryPercentile;
    }
}
