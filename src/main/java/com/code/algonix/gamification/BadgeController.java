package com.code.algonix.gamification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@Tag(name = "Badges", description = "Foydalanuvchi yutuqlari (badge)")
public class BadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "O'z badge larini ko'rish")
    public ResponseEntity<List<BadgeService.BadgeResponse>> getMyBadges(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(badgeService.getUserBadges(user.getId()));
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Boshqa foydalanuvchi badge larini ko'rish")
    public ResponseEntity<List<BadgeService.BadgeResponse>> getUserBadges(@PathVariable String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(badgeService.getUserBadges(user.getId()));
    }
}
