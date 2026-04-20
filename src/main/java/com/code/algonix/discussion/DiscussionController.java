package com.code.algonix.discussion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@Tag(name = "Discussion", description = "Masala muhokamasi — savol-javob, likes, replies")
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping("/problem/{problemId}")
    @Operation(summary = "Masala muhokamalarini olish")
    public ResponseEntity<DiscussionService.DiscussionListResponse> getDiscussions(
            @PathVariable Long problemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(discussionService.getDiscussions(problemId, page, size, username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Muhokamani to'liq ko'rish (replies bilan)")
    public ResponseEntity<DiscussionService.DiscussionResponse> getDiscussion(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(discussionService.getDiscussion(id, username));
    }

    @PostMapping("/problem/{problemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Yangi muhokama yaratish")
    public ResponseEntity<DiscussionService.DiscussionResponse> createDiscussion(
            @PathVariable Long problemId,
            @RequestBody CreateDiscussionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.createDiscussion(
                problemId, request.getTitle(), request.getContent(), authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Muhokamani o'chirish (o'z yoki admin)")
    public ResponseEntity<Void> deleteDiscussion(
            @PathVariable Long id,
            Authentication authentication) {
        discussionService.deleteDiscussion(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Like bosish / olib tashlash")
    public ResponseEntity<DiscussionService.DiscussionResponse> toggleLike(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.toggleLike(id, authentication.getName()));
    }

    // ---- Replies ----

    @GetMapping("/{id}/replies")
    @Operation(summary = "Muhokama javoblarini olish")
    public ResponseEntity<List<DiscussionService.ReplyResponse>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getReplies(id));
    }

    @PostMapping("/{id}/replies")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Javob yozish")
    public ResponseEntity<DiscussionService.ReplyResponse> addReply(
            @PathVariable Long id,
            @RequestBody AddReplyRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(discussionService.addReply(
                id, request.getContent(), request.getParentReplyId(), authentication.getName()));
    }

    @DeleteMapping("/replies/{replyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Javobni o'chirish")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long replyId,
            Authentication authentication) {
        discussionService.deleteReply(replyId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ---- Request DTOs ----

    @Data
    static class CreateDiscussionRequest {
        private String title;
        private String content;
    }

    @Data
    static class AddReplyRequest {
        private String content;
        private Long parentReplyId; // null = top-level reply
    }
}
