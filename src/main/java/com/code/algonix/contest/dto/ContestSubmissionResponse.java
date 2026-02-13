package com.code.algonix.contest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestSubmissionResponse {
    private Long submissionId;
    private Long contestSubmissionId;
    private String status;
    private Boolean isAccepted;
    private Integer score;
    private Long timeTaken;
    private String message;
}
