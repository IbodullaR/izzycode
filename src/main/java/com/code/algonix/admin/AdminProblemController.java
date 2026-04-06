package com.code.algonix.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.code.algonix.problems.Problem;
import com.code.algonix.problems.ProblemRepository;
import com.code.algonix.problems.ProblemService;
import com.code.algonix.problems.dto.CreateProblemRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin-Dashboard-Problems", description = "Admin masalalar boshqaruvi")
public class AdminProblemController {
    
    private final ProblemService problemService;
    private final ProblemRepository problemRepository;
    
    /**
     * Yangi masala yaratish
     */
    @PostMapping
    @Operation(summary = "Yangi masala yaratish", tags = {"Admin-Dashboard-Problems"})
    public ResponseEntity<Problem> createProblem(@RequestBody CreateProblemRequest request) {
        Problem problem = problemService.createProblem(request);
        return ResponseEntity.ok(problem);
    }
    
    /**
     * Masalani yangilash
     */
    @PutMapping("/{problemId}")
    @Operation(summary = "Masalani yangilash (full)", tags = {"Admin-Dashboard-Problems"})
    public ResponseEntity<Problem> updateProblem(
            @PathVariable Long problemId,
            @RequestBody CreateProblemRequest request) {
        
        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        existingProblem.setTitle(request.getTitle());
        existingProblem.setDescription(request.getDescription());
        existingProblem.setDifficulty(request.getDifficulty());
        existingProblem.setCategories(request.getCategories());
        existingProblem.setTags(request.getTags());
        existingProblem.setHints(request.getHints());
        existingProblem.setTimeLimitMs(2000);
        existingProblem.setMemoryLimitMb(256);
        
        Problem updatedProblem = problemRepository.save(existingProblem);
        return ResponseEntity.ok(updatedProblem);
    }

    @PatchMapping("/{problemId}")
    @Operation(summary = "Masalani qisman yangilash", tags = {"Admin-Dashboard-Problems"})
    public ResponseEntity<Problem> patchProblem(
            @PathVariable Long problemId,
            @RequestBody CreateProblemRequest request) {

        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        if (request.getTitle() != null) existingProblem.setTitle(request.getTitle());
        if (request.getDescription() != null) existingProblem.setDescription(request.getDescription());
        if (request.getDifficulty() != null) existingProblem.setDifficulty(request.getDifficulty());
        if (request.getCategories() != null) existingProblem.setCategories(request.getCategories());
        if (request.getTags() != null) existingProblem.setTags(request.getTags());
        if (request.getHints() != null) existingProblem.setHints(request.getHints());

        return ResponseEntity.ok(problemRepository.save(existingProblem));
    }
    
    /**
     * Masala statistikalarini olish
     */
    @GetMapping("/{problemId}/stats")
    @Operation(summary = "Masala statistikasini olish", tags = {"Admin-Dashboard-Problems"})
    public ResponseEntity<Map<String, Object>> getProblemStats(@PathVariable Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        Map<String, Object> stats = Map.of(
                "totalSubmissions", problem.getTotalSubmissions(),
                "totalAccepted", problem.getTotalAccepted(),
                "acceptanceRate", problem.getAcceptanceRate(),
                "likes", problem.getLikes(),
                "dislikes", problem.getDislikes(),
                "difficulty", problem.getDifficulty(),
                "categories", problem.getCategories()
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Masalani publish/unpublish qilish
     */
    @PutMapping("/{problemId}/toggle-publish")
    @Operation(summary = "Masalani publish/unpublish qilish", tags = {"Admin-Dashboard-Problems"})
    public ResponseEntity<Map<String, String>> togglePublish(@PathVariable Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        // Bu yerda publish/unpublish logic qo'shish kerak
        // Hozircha message qaytaramiz
        
        return ResponseEntity.ok(Map.of(
                "message", "Problem publish status toggled",
                "problemId", problemId.toString()
        ));
    }
}