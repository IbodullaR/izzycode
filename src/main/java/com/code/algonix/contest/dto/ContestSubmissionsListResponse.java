package com.code.algonix.contest.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestSubmissionsListResponse {
    private Boolean success;
    private String type; // "ALL" or "ME"
    private Long contestId;
    private String contestName;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private List<SubmissionEntry> data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionEntry {
        private Long id;
        private Long userId;
        private String username;
        private Long problemId;
        private String problemCode;
        private String problemTitle;
        private String language;
        private String status;
        private String runtime;
        private String memory;
        private Integer attempt;
        private Integer penalty;
        private Integer score;
        private LocalDateTime submittedAt;
    }
}
