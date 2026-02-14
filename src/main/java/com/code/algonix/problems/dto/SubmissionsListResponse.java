package com.code.algonix.problems.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionsListResponse {
    private Boolean success;
    private String type; // "ALL" or "ME"
    private Long problemId;
    private String problemTitle;
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
        private String problemTitle;
        private String language;
        private String status;
        private String runtime;
        private String memory;
        private LocalDateTime submittedAt;
    }
}
