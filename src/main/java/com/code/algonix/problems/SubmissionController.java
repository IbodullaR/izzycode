package com.code.algonix.problems;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.code.algonix.problems.dto.SubmissionRequest;
import com.code.algonix.problems.dto.SubmissionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Kod yuborish va natijalarni ko'rish")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Kod yuborish", description = "Masala uchun yechim yuborish")
    public ResponseEntity<SubmissionResponse> submitCode(
            @RequestBody SubmissionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(submissionService.submitCode(request, username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Submission natijasini olish")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @PathVariable Long id,
            Authentication authentication) {
        Long viewerUserId = authentication != null
                ? submissionService.getUserIdByUsername(authentication.getName()) : null;
        return ResponseEntity.ok(submissionService.getSubmission(id, viewerUserId));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Submission kodini 25 coin evaziga ochish")
    public ResponseEntity<SubmissionResponse> unlockSubmission(
            @PathVariable Long id,
            Authentication authentication) {
        Long viewerUserId = submissionService.getUserIdByUsername(authentication.getName());
        return ResponseEntity.ok(submissionService.unlockSubmission(id, viewerUserId));
    }

    @GetMapping("/my")
    @Operation(summary = "O'z submissionlarimni ko'rish")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(submissionService.getUserSubmissions(username));
    }

    @GetMapping
    @Operation(summary = "Submissionlar ro'yxati (filter bilan)", description = "type=ME|ALL, problemId bo'yicha filter")
    public ResponseEntity<com.code.algonix.problems.dto.SubmissionsListResponse> getSubmissions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long problemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = authentication != null
                ? submissionService.getUserIdByUsername(authentication.getName()) : null;
        // ME bo'lsa faqat o'zini, ALL bo'lsa barcha (lekin codeVisible uchun userId kerak)
        Long filterUserId = "ME".equalsIgnoreCase(type) ? userId : null;
        return ResponseEntity.ok(submissionService.getSubmissionsList(type, problemId, filterUserId, userId, page, size));
    }
}
