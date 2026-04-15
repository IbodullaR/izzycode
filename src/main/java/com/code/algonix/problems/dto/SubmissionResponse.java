package com.code.algonix.problems.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.code.algonix.problems.Submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long submissionId;
    private Long userId;
    private String username;
    private Long problemId;
    private String code;           // null unless codeVisible=true
    private String language;
    private Submission.SubmissionStatus status;
    private List<TestResultDto> testResults;
    private OverallStats overallStats;
    private RewardInfo rewardInfo;
    private LocalDateTime submittedAt;
    private LocalDateTime judgedAt;
    // Code visibility
    private Boolean codeVisible;   // true = ko'rsa bo'ladi (o'z submissioni)
    private Boolean unlocked;      // true = 25 coin to'lab unlock qilingan
    private Integer unlockCost;    // 25

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultDto {
        private Long testCaseId;
        private String status;
        private Integer runtime;
        private Double memory;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        private Integer totalTestCases;
        private Integer passedTestCases;
        private Integer runtime;
        private Double runtimePercentile;
        private Double memory;
        private Double memoryPercentile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardInfo {
        private int coinsEarned;
        private int xpEarned;
        private boolean leveledUp;
        private int oldLevel;
        private int newLevel;
        private int totalCoins;
        private int totalXp;
        private int currentLevelXp;
        private int xpToNextLevel;
        private String message;
    }
}
