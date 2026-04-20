package com.code.algonix.gamification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.problems.Problem;
import com.code.algonix.problems.Submission;
import com.code.algonix.problems.SubmissionRepository;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserStatistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final SubmissionRepository submissionRepository;

    /** Submission qabul qilingandan keyin badge larni tekshirish */
    @Transactional
    public List<Badge> checkAndAwardBadges(UserEntity user, Problem problem, Submission submission) {
        List<Badge> newBadges = new ArrayList<>();
        UserStatistics stats = user.getStatistics();
        if (stats == null) return newBadges;

        long totalSolved = submissionRepository.countSolvedProblemsByUser(user);

        newBadges.addAll(checkSolveBadges(user, totalSolved));
        newBadges.addAll(checkDifficultyBadges(user, problem));
        newBadges.addAll(checkSpeedBadge(user, submission));
        newBadges.addAll(checkStreakBadges(user, stats));
        newBadges.addAll(checkCoinBadge(user, stats));
        newBadges.addAll(checkLevelBadge(user, stats));

        if (!newBadges.isEmpty()) {
            log.info("User {} earned {} new badge(s)", user.getUsername(), newBadges.size());
        }
        return newBadges;
    }

    /** Foydalanuvchi badge larini olish */
    public List<BadgeResponse> getUserBadges(Long userId) {
        return badgeRepository.findByUserIdOrderByEarnedAtDesc(userId)
                .stream()
                .map(b -> BadgeResponse.builder()
                        .type(b.getType().name())
                        .name(b.getType().name)
                        .description(b.getType().description)
                        .emoji(b.getType().emoji)
                        .earnedAt(b.getEarnedAt())
                        .build())
                .toList();
    }

    // ---- Private check methods ----

    private List<Badge> checkSolveBadges(UserEntity user, long totalSolved) {
        List<Badge> badges = new ArrayList<>();
        if (totalSolved == 1) badges.add(award(user, Badge.BadgeType.FIRST_SOLVE));
        if (totalSolved >= 10) badges.add(award(user, Badge.BadgeType.SOLVE_10));
        if (totalSolved >= 50) badges.add(award(user, Badge.BadgeType.SOLVE_50));
        if (totalSolved >= 100) badges.add(award(user, Badge.BadgeType.SOLVE_100));
        return badges.stream().filter(b -> b != null).toList();
    }

    private List<Badge> checkDifficultyBadges(UserEntity user, Problem problem) {
        List<Badge> badges = new ArrayList<>();
        if (problem.getDifficulty() == Problem.Difficulty.HARD) {
            badges.add(award(user, Badge.BadgeType.FIRST_HARD));
        }
        if (problem.getDifficulty() == Problem.Difficulty.MEDIUM) {
            badges.add(award(user, Badge.BadgeType.FIRST_MEDIUM));
        }
        return badges.stream().filter(b -> b != null).toList();
    }

    private List<Badge> checkSpeedBadge(UserEntity user, Submission submission) {
        if (submission.getRuntime() != null && submission.getRuntime() < 100) {
            Badge b = award(user, Badge.BadgeType.SPEED_DEMON);
            return b != null ? List.of(b) : List.of();
        }
        return List.of();
    }

    private List<Badge> checkStreakBadges(UserEntity user, UserStatistics stats) {
        List<Badge> badges = new ArrayList<>();
        if (stats.getCurrentStreak() >= 7) badges.add(award(user, Badge.BadgeType.STREAK_7));
        if (stats.getCurrentStreak() >= 30) badges.add(award(user, Badge.BadgeType.STREAK_30));
        return badges.stream().filter(b -> b != null).toList();
    }

    private List<Badge> checkCoinBadge(UserEntity user, UserStatistics stats) {
        if (stats.getCoins() >= 100) {
            Badge b = award(user, Badge.BadgeType.COIN_100);
            return b != null ? List.of(b) : List.of();
        }
        return List.of();
    }

    private List<Badge> checkLevelBadge(UserEntity user, UserStatistics stats) {
        if (stats.getLevel() >= 10) {
            Badge b = award(user, Badge.BadgeType.LEVEL_10);
            return b != null ? List.of(b) : List.of();
        }
        return List.of();
    }

    /** Badge berish — allaqachon bor bo'lsa null qaytaradi */
    private Badge award(UserEntity user, Badge.BadgeType type) {
        if (badgeRepository.existsByUserIdAndType(user.getId(), type)) return null;
        Badge badge = Badge.builder().user(user).type(type).build();
        return badgeRepository.save(badge);
    }

    // ---- Response DTO ----
    @lombok.Builder
    @lombok.Data
    public static class BadgeResponse {
        private String type;
        private String name;
        private String description;
        private String emoji;
        private java.time.LocalDateTime earnedAt;
    }
}
