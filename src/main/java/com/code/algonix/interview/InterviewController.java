package com.code.algonix.interview;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Tag(name = "Mock Interview", description = "Vaqt cheklangan intervyu simulyatsiyasi")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Interview boshlash — tasodifiy masala va timer")
    public ResponseEntity<InterviewService.SessionResponse> startSession(
            @RequestBody InterviewService.StartRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(interviewService.startSession(
                authentication.getName(),
                request.getDifficulty(),
                request.getDurationMinutes()));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Faol sessiyani olish (qolgan vaqt bilan)")
    public ResponseEntity<InterviewService.SessionResponse> getActiveSession(Authentication authentication) {
        return ResponseEntity.ok(interviewService.getActiveSession(authentication.getName()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kod yuborish va AI feedback olish")
    public ResponseEntity<InterviewService.SessionResponse> submitCode(
            @PathVariable Long id,
            @RequestBody InterviewService.SubmitRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(interviewService.submitCode(
                id, request.getCode(), request.getLanguage(), authentication.getName()));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sessiyani yakunlash (vaqt tugadi yoki tark etildi)")
    public ResponseEntity<InterviewService.SessionResponse> endSession(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean abandoned,
            Authentication authentication) {
        return ResponseEntity.ok(interviewService.endSession(id, authentication.getName(), abandoned));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "O'tgan interviewlar tarixi")
    public ResponseEntity<List<InterviewService.SessionResponse>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(interviewService.getHistory(authentication.getName()));
    }
}
