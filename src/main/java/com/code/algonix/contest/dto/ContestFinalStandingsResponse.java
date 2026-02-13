package com.code.algonix.contest.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ContestFinalStandingsResponse {
    
    private ContestInfo contest;
    private Map<String, ProblemStats> overallStats;
    private List<StandingEntry> standings;
    private PaginationInfo pagination;
    
    @Data
    public static class ContestInfo {
        private String id;
        private String title;
        private String status;
        private Boolean isFrozen;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> problemList;
    }
    
    @Data
    public static class ProblemStats {
        private Integer solved;
        private Integer failed;
        private Integer attempting;
        private String firstSolveTime; // Format: "HH:mm"
    }
    
    @Data
    public static class StandingEntry {
        private Integer rank;
        private Long userId;
        private String username;
        private String avatarUrl;
        private Integer solvedCount;
        private Integer totalScore;
        private Long totalPenalty;
        private List<ProblemResult> problems;
    }
    
    @Data
    public static class ProblemResult {
        private String id; // Problem symbol (A, B, C, etc.)
        private String status; // "solved", "failed", "attempting", "unattempted"
        private Integer attempts;
        private String time; // Format: "HH:mm" or null
        private Boolean isFirstBlood;
    }
    
    @Data
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Integer totalParticipants;
    }
}
