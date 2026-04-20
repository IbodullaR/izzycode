package com.code.algonix.social;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/social")
@RequiredArgsConstructor
@Tag(name = "Social", description = "Follow/Unfollow, do'stlar va leaderboard")
public class SocialController {

    private final SocialService socialService;

    @PostMapping("/follow/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Follow / Unfollow")
    public ResponseEntity<SocialService.FollowResponse> toggleFollow(
            @PathVariable String username,
            Authentication authentication) {
        return ResponseEntity.ok(socialService.toggleFollow(authentication.getName(), username));
    }

    @GetMapping("/follow-status/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Follow holati")
    public ResponseEntity<SocialService.FollowStatus> getFollowStatus(
            @PathVariable String username,
            Authentication authentication) {
        return ResponseEntity.ok(socialService.getFollowStatus(authentication.getName(), username));
    }

    @GetMapping("/following")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Men kuzatayotganlar")
    public ResponseEntity<List<SocialService.UserSummary>> getFollowing(Authentication authentication) {
        return ResponseEntity.ok(socialService.getFollowing(authentication.getName()));
    }

    @GetMapping("/followers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Meni kuzatayotganlar")
    public ResponseEntity<List<SocialService.UserSummary>> getFollowers(Authentication authentication) {
        return ResponseEntity.ok(socialService.getFollowers(authentication.getName()));
    }

    @GetMapping("/friends")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Do'stlar (ikki tomonlama follow)")
    public ResponseEntity<List<SocialService.UserSummary>> getFriends(Authentication authentication) {
        return ResponseEntity.ok(socialService.getFriends(authentication.getName()));
    }

    @GetMapping("/friends/leaderboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Do'stlar leaderboard — do'stlar orasida reyting")
    public ResponseEntity<List<SocialService.FriendRanking>> getFriendsLeaderboard(Authentication authentication) {
        return ResponseEntity.ok(socialService.getFriendsLeaderboard(authentication.getName()));
    }

    @GetMapping("/user/{username}/following")
    @Operation(summary = "Boshqa foydalanuvchi kuzatayotganlar")
    public ResponseEntity<List<SocialService.UserSummary>> getUserFollowing(
            @PathVariable String username,
            Authentication authentication) {
        return ResponseEntity.ok(socialService.getFollowing(username));
    }

    @GetMapping("/user/{username}/followers")
    @Operation(summary = "Boshqa foydalanuvchi kuzatuvchilari")
    public ResponseEntity<List<SocialService.UserSummary>> getUserFollowers(@PathVariable String username) {
        return ResponseEntity.ok(socialService.getFollowers(username));
    }
}
