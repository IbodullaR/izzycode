package com.code.algonix.problems;

import com.code.algonix.problems.dto.SubmissionRequest;
import com.code.algonix.problems.dto.SubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmission(id));
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
        Long userId = null;
        if (authentication != null && "ME".equalsIgnoreCase(type)) {
            userId = submissionService.getUserIdByUsername(authentication.getName());
        }
        return ResponseEntity.ok(submissionService.getSubmissionsList(type, problemId, userId, page, size));
    }
}
