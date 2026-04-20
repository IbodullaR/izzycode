package com.code.algonix.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "AI Hint va Code Review (Groq API)")
public class AiController {

    private final AiService aiService;

    @PostMapping("/hint")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AI Hint — 10 coin evaziga maslahat olish")
    public ResponseEntity<AiService.AiResponse> getAiHint(
            @RequestBody AiService.AiHintRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(aiService.getAiHint(
                request.getProblemId(),
                request.getCode(),
                request.getLanguage(),
                authentication.getName()));
    }

    @PostMapping("/review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Code Review — 5 coin evaziga kod tahlili")
    public ResponseEntity<AiService.AiResponse> getCodeReview(
            @RequestBody AiService.AiHintRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(aiService.getCodeReview(
                request.getProblemId(),
                request.getCode(),
                request.getLanguage(),
                authentication.getName()));
    }
}
