package com.code.algonix.solution;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.code.algonix.solution.dto.CreateSolutionRequest;
import com.code.algonix.solution.dto.SolutionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solutions")
@RequiredArgsConstructor
@Tag(name = "Solutions", description = "Masala yechimlari (25 coin evaziga ko'rish)")
public class SolutionController {

    private final SolutionService solutionService;

    @GetMapping("/problem/{problemId}")
    @Operation(summary = "Masala uchun barcha yechimlar (kod yashirin)")
    public ResponseEntity<List<SolutionResponse>> getSolutions(
            @PathVariable Long problemId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(solutionService.getSolutionsForProblem(problemId, username));
    }

    @PostMapping
    @Operation(summary = "Yangi yechim publish qilish (ACCEPTED submission kerak)")
    public ResponseEntity<SolutionResponse> createSolution(
            @RequestBody CreateSolutionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(solutionService.createSolution(request, authentication.getName()));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Yechim kodini 25 coin evaziga ochish")
    public ResponseEntity<SolutionResponse> unlockSolution(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(solutionService.unlockSolution(id, authentication.getName()));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Yechimga like bosish / olib tashlash")
    public ResponseEntity<SolutionResponse> toggleLike(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(solutionService.toggleLike(id, authentication.getName()));
    }
}
