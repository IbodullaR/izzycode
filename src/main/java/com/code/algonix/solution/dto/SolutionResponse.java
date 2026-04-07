package com.code.algonix.solution.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolutionResponse {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String authorAvatar;
    private Long problemId;
    private String title;
    private String description;
    private String language;
    // code is null unless unlocked
    private String code;
    private boolean unlocked;
    private boolean liked;
    private Integer likes;
    private Integer views;
    private Integer unlockCount;
    private LocalDateTime createdAt;
    private int unlockCost; // always 25
}
