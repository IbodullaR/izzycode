package com.code.algonix.problems.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemTranslationResponse {
    private Long id;
    private Long problemId;
    private String language;
    private String title;
    private String description;
    private String descriptionHtml;
}
