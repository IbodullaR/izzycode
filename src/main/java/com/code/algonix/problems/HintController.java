package com.code.algonix.problems;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Tag(name = "Hints", description = "Masala hintlarini boshqarish (5 coin)")
public class HintController {

    private final HintService hintService;

    @GetMapping("/{id}/hints")
    @Operation(summary = "Masala hintlarini ko'rish (ochilganlar matni bilan)")
    public ResponseEntity<List<HintService.HintResponse>> getHints(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(hintService.getHints(id, authentication.getName()));
    }

    @PostMapping("/{id}/hints/{index}/unlock")
    @Operation(summary = "Hintni 5 coin evaziga ochish")
    public ResponseEntity<HintService.HintResponse> unlockHint(
            @PathVariable Long id,
            @PathVariable int index,
            Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(hintService.unlockHint(id, index, authentication.getName()));
    }
}
