package com.code.algonix.daily;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.problems.Problem;
import com.code.algonix.problems.ProblemRepository;
import com.code.algonix.problems.Submission;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;
import com.code.algonix.user.UserStatistics;
import com.code.algonix.user.UserStatisticsRepository;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyChallengeService {

    private static final int BONUS_COINS = 10;
    private static final int BONUS_XP = 20;

    private final DailyChallengeRepository dailyChallengeRepository;
    private final DailyChallengeCompletionRepository completionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final UserStatisticsRepository statsRepository;

    /** Bugungi daily challenge ni olish (yo'q bo'lsa yaratish) */
    @Transactional
    public DailyChallengeResponse getTodayChallenge(String username) {
        LocalDate today = LocalDate.now();
        DailyChallenge challenge = dailyChallengeRepository.findByChallengeDate(today)
                .orElseGet(() -> createDailyChallenge(today));

        boolean completed = false;
        if (username != null) {
            UserEntity user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                completed = completionRepository.existsByUserIdAndDailyChallengeId(user.getId(), challenge.getId());
            }
        }

        return toResponse(challenge, completed);
    }

    /** Submission ACCEPTED bo'lganda daily challenge ni tekshirish */
    @Transactional
    public DailyChallengeBonus checkDailyChallenge(UserEntity user, Problem problem, Submission submission) {
        LocalDate today = LocalDate.now();
        DailyChallenge challenge = dailyChallengeRepository.findByChallengeDate(today).orElse(null);

        if (challenge == null || !challenge.getProblem().getId().equals(problem.getId())) {
            return null; // Bu daily challenge emas
        }

        if (completionRepository.existsByUserIdAndDailyChallengeId(user.getId(), challenge.getId())) {
            return null; // Allaqachon bajarilgan
        }

        // Bonus berish
        UserStatistics stats = user.getStatistics();
        if (stats == null) return null;

        stats.setCoins(stats.getCoins() + BONUS_COINS);
        stats.setExperience(stats.getExperience() + BONUS_XP);
        statsRepository.save(stats);

        // Completion saqlash
        completionRepository.save(DailyChallengeCompletion.builder()
                .user(user)
                .dailyChallenge(challenge)
                .bonusCoinsEarned(BONUS_COINS)
                .bonusXpEarned(BONUS_XP)
                .build());

        log.info("User {} completed daily challenge! +{} coins, +{} XP", user.getUsername(), BONUS_COINS, BONUS_XP);

        return DailyChallengeBonus.builder()
                .bonusCoins(BONUS_COINS)
                .bonusXp(BONUS_XP)
                .message("🎉 Kunlik vazifa bajarildi! +" + BONUS_COINS + " coin, +" + BONUS_XP + " XP")
                .build();
    }

    /** Har kuni soat 00:00 da yangi masala tanlash */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void scheduleDailyChallenge() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (!dailyChallengeRepository.existsByChallengeDate(tomorrow)) {
            createDailyChallenge(tomorrow);
            log.info("Daily challenge created for {}", tomorrow);
        }
    }

    private DailyChallenge createDailyChallenge(LocalDate date) {
        List<Problem> problems = problemRepository.findAll();
        if (problems.isEmpty()) throw new ResourceNotFoundException("No problems available");

        // Deterministik tanlash — sana asosida (har kuni bir xil masala)
        int index = (int) (date.toEpochDay() % problems.size());
        Problem selected = problems.get(index);

        DailyChallenge challenge = DailyChallenge.builder()
                .challengeDate(date)
                .problem(selected)
                .build();

        return dailyChallengeRepository.save(challenge);
    }

    private DailyChallengeResponse toResponse(DailyChallenge c, boolean completed) {
        Problem p = c.getProblem();
        return DailyChallengeResponse.builder()
                .id(c.getId())
                .challengeDate(c.getChallengeDate())
                .problemId(p.getId())
                .problemSlug(p.getSlug())
                .problemTitle(p.getTitle())
                .difficulty(p.getDifficulty().name())
                .bonusCoins(BONUS_COINS)
                .bonusXp(BONUS_XP)
                .completed(completed)
                .build();
    }

    // ---- DTOs ----

    @Data @Builder
    public static class DailyChallengeResponse {
        private Long id;
        private LocalDate challengeDate;
        private Long problemId;
        private String problemSlug;
        private String problemTitle;
        private String difficulty;
        private int bonusCoins;
        private int bonusXp;
        private boolean completed;
    }

    @Data @Builder
    public static class DailyChallengeBonus {
        private int bonusCoins;
        private int bonusXp;
        private String message;
    }
}
