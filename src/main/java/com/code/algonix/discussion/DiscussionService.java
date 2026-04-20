package com.code.algonix.discussion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.exception.InvalidInputException;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.problems.Problem;
import com.code.algonix.problems.ProblemRepository;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository replyRepository;
    private final DiscussionLikeRepository likeRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    // ---- Discussion CRUD ----

    @Transactional(readOnly = true)
    public DiscussionListResponse getDiscussions(Long problemId, int page, int size, String username) {
        Page<Discussion> pageResult = discussionRepository
                .findByProblemIdOrderByPinnedDescLikesDescCreatedAtDesc(
                        problemId, PageRequest.of(page, size));

        Long userId = username != null ? getUser(username).getId() : null;

        List<DiscussionResponse> items = pageResult.getContent().stream()
                .map(d -> toResponse(d, userId, false))
                .toList();

        return DiscussionListResponse.builder()
                .discussions(items)
                .total(pageResult.getTotalElements())
                .page(page)
                .pageSize(size)
                .build();
    }

    @Transactional(readOnly = true)
    public DiscussionResponse getDiscussion(Long id, String username) {
        Discussion d = getDiscussionById(id);
        d.setViews(d.getViews() + 1);
        discussionRepository.save(d);
        Long userId = username != null ? getUser(username).getId() : null;
        return toResponse(d, userId, true);
    }

    @Transactional
    public DiscussionResponse createDiscussion(Long problemId, String title, String content, String username) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        UserEntity author = getUser(username);

        Discussion d = Discussion.builder()
                .problem(problem)
                .author(author)
                .title(title)
                .content(content)
                .build();

        return toResponse(discussionRepository.save(d), author.getId(), false);
    }

    @Transactional
    public void deleteDiscussion(Long id, String username) {
        Discussion d = getDiscussionById(id);
        UserEntity user = getUser(username);
        if (!d.getAuthor().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new InvalidInputException("Ruxsat yo'q");
        }
        discussionRepository.delete(d);
    }

    // ---- Replies ----

    @Transactional
    public ReplyResponse addReply(Long discussionId, String content, Long parentReplyId, String username) {
        Discussion discussion = getDiscussionById(discussionId);
        UserEntity author = getUser(username);

        DiscussionReply parent = null;
        if (parentReplyId != null) {
            parent = replyRepository.findById(parentReplyId).orElse(null);
        }

        DiscussionReply reply = DiscussionReply.builder()
                .discussion(discussion)
                .author(author)
                .content(content)
                .parentReply(parent)
                .build();

        return toReplyResponse(replyRepository.save(reply));
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> getReplies(Long discussionId) {
        return replyRepository
                .findByDiscussionIdAndParentReplyIsNullOrderByCreatedAtAsc(discussionId)
                .stream()
                .map(r -> {
                    ReplyResponse resp = toReplyResponse(r);
                    // Nested replies
                    resp.setReplies(replyRepository
                            .findByParentReplyIdOrderByCreatedAtAsc(r.getId())
                            .stream().map(this::toReplyResponse).toList());
                    return resp;
                })
                .toList();
    }

    @Transactional
    public void deleteReply(Long replyId, String username) {
        DiscussionReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Reply not found"));
        UserEntity user = getUser(username);
        if (!reply.getAuthor().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new InvalidInputException("Ruxsat yo'q");
        }
        replyRepository.delete(reply);
    }

    // ---- Like ----

    @Transactional
    public DiscussionResponse toggleLike(Long discussionId, String username) {
        Discussion d = getDiscussionById(discussionId);
        UserEntity user = getUser(username);

        if (likeRepository.existsByUserIdAndDiscussionId(user.getId(), discussionId)) {
            likeRepository.deleteByUserIdAndDiscussionId(user.getId(), discussionId);
            d.setLikes(Math.max(0, d.getLikes() - 1));
        } else {
            likeRepository.save(DiscussionLike.builder().user(user).discussion(d).build());
            d.setLikes(d.getLikes() + 1);
        }

        return toResponse(discussionRepository.save(d), user.getId(), false);
    }

    // ---- Helpers ----

    private DiscussionResponse toResponse(Discussion d, Long viewerUserId, boolean includeReplies) {
        boolean liked = viewerUserId != null &&
                likeRepository.existsByUserIdAndDiscussionId(viewerUserId, d.getId());

        List<ReplyResponse> replies = includeReplies ? getReplies(d.getId()) : null;

        return DiscussionResponse.builder()
                .id(d.getId())
                .problemId(d.getProblem().getId())
                .authorId(d.getAuthor().getId())
                .authorUsername(d.getAuthor().getUsername())
                .authorAvatar(d.getAuthor().getAvatarUrl())
                .title(d.getTitle())
                .content(d.getContent())
                .likes(d.getLikes())
                .views(d.getViews())
                .pinned(d.isPinned())
                .liked(liked)
                .replyCount(d.getReplies().size())
                .replies(replies)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private ReplyResponse toReplyResponse(DiscussionReply r) {
        return ReplyResponse.builder()
                .id(r.getId())
                .discussionId(r.getDiscussion().getId())
                .authorId(r.getAuthor().getId())
                .authorUsername(r.getAuthor().getUsername())
                .authorAvatar(r.getAuthor().getAvatarUrl())
                .content(r.getContent())
                .likes(r.getLikes())
                .parentReplyId(r.getParentReply() != null ? r.getParentReply().getId() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private Discussion getDiscussionById(Long id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isAdmin(UserEntity user) {
        return user.getRole() != null && user.getRole().name().equals("ADMIN");
    }

    // ---- DTOs ----

    @Data @Builder
    public static class DiscussionListResponse {
        private List<DiscussionResponse> discussions;
        private long total;
        private int page;
        private int pageSize;
    }

    @Data @Builder
    public static class DiscussionResponse {
        private Long id;
        private Long problemId;
        private Long authorId;
        private String authorUsername;
        private String authorAvatar;
        private String title;
        private String content;
        private Integer likes;
        private Integer views;
        private boolean pinned;
        private boolean liked;
        private int replyCount;
        private List<ReplyResponse> replies;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @Builder
    public static class ReplyResponse {
        private Long id;
        private Long discussionId;
        private Long authorId;
        private String authorUsername;
        private String authorAvatar;
        private String content;
        private Integer likes;
        private Long parentReplyId;
        private List<ReplyResponse> replies;
        private LocalDateTime createdAt;
    }
}
