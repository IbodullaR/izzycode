package com.code.algonix.problems;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.exception.InvalidInputException;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;
import com.code.algonix.user.UserStatistics;
import com.code.algonix.user.UserStatisticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HintService {

    private static final int HINT_COST = 5;

    private final HintUnlockRepository hintUnlockRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final UserStatisticsRepository statsRepository;

    /** Foydalanuvchi ochgan hintlar ro'yxati (matn faqat ochilganlarda) */
    @Transactional(readOnly = true)
    public List<HintResponse> getHints(Long problemId, String username) {
        Problem problem = getProblem(problemId);
        List<String> hints = problem.getHints();
        if (hints == null || hints.isEmpty()) return List.of();

        Long userId = getUser(username).getId();
        List<HintUnlock> unlocked = hintUnlockRepository.findByUserIdAndProblemId(userId, problemId);
        List<Integer> unlockedIndexes = unlocked.stream().map(HintUnlock::getHintIndex).toList();

        List<HintResponse> result = new ArrayList<>();
        for (int i = 0; i < hints.size(); i++) {
            boolean isUnlocked = unlockedIndexes.contains(i);
            result.add(HintResponse.builder()
                    .index(i)
                    .text(isUnlocked ? hints.get(i) : null)
                    .unlocked(isUnlocked)
                    .cost(HINT_COST)
                    .build());
        }
        return result;
    }

    /** 5 coin to'lab hint ochish */
    @Transactional
    public HintResponse unlockHint(Long problemId, int hintIndex, String username) {
        Problem problem = getProblem(problemId);
        List<String> hints = problem.getHints();

        if (hints == null || hintIndex < 0 || hintIndex >= hints.size()) {
            throw new InvalidInputException("Hint topilmadi");
        }

        UserEntity user = getUser(username);

        if (hintUnlockRepository.existsByUserIdAndProblemIdAndHintIndex(user.getId(), problemId, hintIndex)) {
            return HintResponse.builder()
                    .index(hintIndex)
                    .text(hints.get(hintIndex))
                    .unlocked(true)
                    .cost(HINT_COST)
                    .build();
        }

        // Coin tekshirish
        UserStatistics stats = user.getStatistics();
        if (stats == null || stats.getCoins() < HINT_COST) {
            int current = stats != null ? stats.getCoins() : 0;
            throw new InvalidInputException("Yetarli coin yo'q. Kerak: " + HINT_COST + ", Mavjud: " + current);
        }

        // Coin ayirish
        stats.setCoins(stats.getCoins() - HINT_COST);
        statsRepository.save(stats);

        // Unlock saqlash
        hintUnlockRepository.save(HintUnlock.builder()
                .user(user)
                .problem(problem)
                .hintIndex(hintIndex)
                .coinsSpent(HINT_COST)
                .build());

        return HintResponse.builder()
                .index(hintIndex)
                .text(hints.get(hintIndex))
                .unlocked(true)
                .cost(HINT_COST)
                .build();
    }

    private Problem getProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ---- Response DTO ----
    @lombok.Builder
    @lombok.Data
    public static class HintResponse {
        private int index;
        private String text;    // null = yashirin
        private boolean unlocked;
        private int cost;
    }
}
