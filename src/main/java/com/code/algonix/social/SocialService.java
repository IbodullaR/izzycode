package com.code.algonix.social;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.exception.InvalidInputException;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;
import com.code.algonix.user.UserStatistics;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /** Follow / Unfollow */
    @Transactional
    public FollowResponse toggleFollow(String followerUsername, String targetUsername) {
        if (followerUsername.equals(targetUsername)) {
            throw new InvalidInputException("O'zingizni kuzata olmaysiz");
        }

        UserEntity follower = getUser(followerUsername);
        UserEntity target = getUser(targetUsername);

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(follower.getId(), target.getId());

        if (isFollowing) {
            followRepository.deleteByFollowerIdAndFollowingId(follower.getId(), target.getId());
        } else {
            followRepository.save(Follow.builder().follower(follower).following(target).build());
        }

        return FollowResponse.builder()
                .targetUsername(targetUsername)
                .following(!isFollowing)
                .followersCount(followRepository.countByFollowingId(target.getId()))
                .followingCount(followRepository.countByFollowerId(target.getId()))
                .build();
    }

    /** Kimlarni kuzatyapman */
    @Transactional(readOnly = true)
    public List<UserSummary> getFollowing(String username) {
        UserEntity user = getUser(username);
        return followRepository.findByFollowerIdOrderByFollowedAtDesc(user.getId())
                .stream().map(f -> toSummary(f.getFollowing(), user.getId())).toList();
    }

    /** Meni kuzatayotganlar */
    @Transactional(readOnly = true)
    public List<UserSummary> getFollowers(String username) {
        UserEntity user = getUser(username);
        return followRepository.findByFollowingIdOrderByFollowedAtDesc(user.getId())
                .stream().map(f -> toSummary(f.getFollower(), user.getId())).toList();
    }

    /** Do'stlar (ikki tomonlama follow) */
    @Transactional(readOnly = true)
    public List<UserSummary> getFriends(String username) {
        UserEntity user = getUser(username);
        return followRepository.findMutualFriends(user.getId())
                .stream().map(u -> toSummary(u, user.getId())).toList();
    }

    /** Do'stlar leaderboard — do'stlar orasida reyting */
    @Transactional(readOnly = true)
    public List<FriendRanking> getFriendsLeaderboard(String username) {
        UserEntity user = getUser(username);
        List<UserEntity> friends = followRepository.findMutualFriends(user.getId());
        friends.add(user); // o'zini ham qo'shish

        return friends.stream()
                .map(u -> {
                    UserStatistics stats = u.getStatistics();
                    return FriendRanking.builder()
                            .userId(u.getId())
                            .username(u.getUsername())
                            .avatarUrl(u.getAvatarUrl())
                            .totalSolved(stats != null ? stats.getTotalSolved() : 0)
                            .level(stats != null ? stats.getLevel() : 1)
                            .coins(stats != null ? stats.getCoins() : 0)
                            .experience(stats != null ? stats.getExperience() : 0)
                            .isMe(u.getId().equals(user.getId()))
                            .build();
                })
                .sorted((a, b) -> b.getTotalSolved() - a.getTotalSolved())
                .toList();
    }

    /** Follow holati */
    @Transactional(readOnly = true)
    public FollowStatus getFollowStatus(String viewerUsername, String targetUsername) {
        UserEntity viewer = getUser(viewerUsername);
        UserEntity target = getUser(targetUsername);

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), target.getId());
        boolean isFollower = followRepository.existsByFollowerIdAndFollowingId(target.getId(), viewer.getId());

        return FollowStatus.builder()
                .isFollowing(isFollowing)
                .isFollower(isFollower)
                .isFriend(isFollowing && isFollower)
                .followersCount(followRepository.countByFollowingId(target.getId()))
                .followingCount(followRepository.countByFollowerId(target.getId()))
                .build();
    }

    // ---- Helpers ----

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private UserSummary toSummary(UserEntity u, Long viewerUserId) {
        UserStatistics stats = u.getStatistics();
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(viewerUserId, u.getId());
        return UserSummary.builder()
                .userId(u.getId())
                .username(u.getUsername())
                .avatarUrl(u.getAvatarUrl())
                .level(stats != null ? stats.getLevel() : 1)
                .totalSolved(stats != null ? stats.getTotalSolved() : 0)
                .isFollowing(isFollowing)
                .build();
    }

    // ---- DTOs ----

    @Data @Builder
    public static class FollowResponse {
        private String targetUsername;
        private boolean following;
        private long followersCount;
        private long followingCount;
    }

    @Data @Builder
    public static class FollowStatus {
        private boolean isFollowing;
        private boolean isFollower;
        private boolean isFriend;
        private long followersCount;
        private long followingCount;
    }

    @Data @Builder
    public static class UserSummary {
        private Long userId;
        private String username;
        private String avatarUrl;
        private int level;
        private int totalSolved;
        private boolean isFollowing;
    }

    @Data @Builder
    public static class FriendRanking {
        private Long userId;
        private String username;
        private String avatarUrl;
        private int totalSolved;
        private int level;
        private int coins;
        private int experience;
        private boolean isMe;
    }
}
