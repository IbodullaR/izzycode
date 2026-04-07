package com.code.algonix.solution.dto;

import lombok.Data;

@Data
public class CreateSolutionRequest {
    private Long problemId;
    private Long submissionId; // optional — link to accepted submission
    private String title;
    private String description;
    private String language;
    private String code;
}
