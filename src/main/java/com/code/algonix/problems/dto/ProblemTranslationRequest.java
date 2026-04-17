package com.code.algonix.problems.dto;

import lombok.Data;

@Data
public class ProblemTranslationRequest {
    private String language;       // "uz", "en", "ru"
    private String title;
    private String description;
    private String descriptionHtml;
}
