package com.code.algonix.daily;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/daily-challenge")
@RequiredArgsConstructor
@Tag(name = "Daily Challenge", description = "Kunlik vazifa — har kuni bonus coin/XP")
public class DailyChallengeController {

    private final DailyChallengeService dailyChallengeService;

    @GetMapping
    @Operation(summary = "Bugungi daily challenge ni olish")
    public ResponseEntity<DailyChallengeService.DailyChallengeResponse> getTodayChallenge(
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(dailyChallengeService.getTodayChallenge(username));
    }
}
