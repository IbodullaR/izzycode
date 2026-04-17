package com.code.algonix.problems;

import com.code.algonix.problems.dto.ProblemTranslationRequest;
import com.code.algonix.problems.dto.ProblemTranslationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Tag(name = "Problems Translations", description = "Masala tarjimasini boshqarish (uz, en, ru)")
public class ProblemTranslationController {

    private final ProblemService problemService;

    @GetMapping("/{id}/translations")
    @Operation(summary = "Masalaning barcha tarjimalarini olish")
    public ResponseEntity<List<ProblemTranslationResponse>> getTranslations(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getTranslations(id));
    }

    @PostMapping("/{id}/translations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tarjima qo'shish yoki yangilash (ADMIN)")
    public ResponseEntity<ProblemTranslationResponse> saveTranslation(
            @PathVariable Long id,
            @RequestBody ProblemTranslationRequest request) {
        return ResponseEntity.ok(problemService.saveTranslation(id, request));
    }

    @DeleteMapping("/{id}/translations/{lang}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tarjimani o'chirish (ADMIN)")
    public ResponseEntity<Void> deleteTranslation(
            @PathVariable Long id,
            @PathVariable String lang) {
        problemService.deleteTranslation(id, lang);
        return ResponseEntity.noContent().build();
    }
}
