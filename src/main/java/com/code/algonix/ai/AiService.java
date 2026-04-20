package com.code.algonix.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.exception.InvalidInputException;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.problems.Problem;
import com.code.algonix.problems.ProblemRepository;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;
import com.code.algonix.user.UserStatistics;
import com.code.algonix.user.UserStatisticsRepository;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${ai.hint.cost:10}")
    private int hintCost;

    @Value("${ai.review.cost:5}")
    private int reviewCost;

    private final GroqService groqService;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final UserStatisticsRepository statsRepository;

    /** AI Hint — 10 coin evaziga masala bo'yicha maslahat */
    @Transactional
    public AiResponse getAiHint(Long problemId, String userCode, String language, String username) {
        UserEntity user = getUser(username);
        deductCoins(user, hintCost);

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        String systemPrompt = """
                Siz algoritmik masalalar bo'yicha mutaxassis o'qituvchisiz.
                Foydalanuvchiga masalani hal qilishda yordam bering, lekin to'liq javobni bermang.
                Faqat yo'nalish va maslahat bering. O'zbek tilida javob bering.
                """;

        String userMessage = String.format("""
                Masala: %s
                
                Masala tavsifi: %s
                
                Foydalanuvchi kodi (%s):
                ```
                %s
                ```
                
                Iltimos, bu kodni tahlil qilib, foydalanuvchiga qayerda xato qilayotganini yoki qanday yondashuv ishlatishi kerakligini tushuntiring.
                To'liq yechimni bermang, faqat yo'nalish bering.
                """,
                problem.getTitle(),
                problem.getDescription() != null ? problem.getDescription().substring(0, Math.min(500, problem.getDescription().length())) : "",
                language,
                userCode != null ? userCode.substring(0, Math.min(1000, userCode.length())) : "");

        String aiResponse = groqService.chat(systemPrompt, userMessage);

        return AiResponse.builder()
                .response(aiResponse)
                .coinsSpent(hintCost)
                .type("HINT")
                .build();
    }

    /** Code Review — 5 coin evaziga kod sifatini baholash */
    @Transactional
    public AiResponse getCodeReview(Long problemId, String code, String language, String username) {
        UserEntity user = getUser(username);
        deductCoins(user, reviewCost);

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        String systemPrompt = """
                Siz tajribali dasturchi va kod reviewer siz.
                Kodni tahlil qilib, quyidagilarni baholang:
                1. Vaqt murakkabligi (Time Complexity)
                2. Xotira murakkabligi (Space Complexity)
                3. Kod sifati va o'qilishi
                4. Yaxshilash tavsiyalari
                O'zbek tilida javob bering.
                """;

        String userMessage = String.format("""
                Masala: %s
                
                Kod (%s):
                ```
                %s
                ```
                
                Iltimos, bu kodni to'liq tahlil qiling.
                """,
                problem.getTitle(),
                language,
                code != null ? code.substring(0, Math.min(2000, code.length())) : "");

        String aiResponse = groqService.chat(systemPrompt, userMessage);

        return AiResponse.builder()
                .response(aiResponse)
                .coinsSpent(reviewCost)
                .type("REVIEW")
                .build();
    }

    private void deductCoins(UserEntity user, int amount) {
        UserStatistics stats = user.getStatistics();
        if (stats == null || stats.getCoins() < amount) {
            int current = stats != null ? stats.getCoins() : 0;
            throw new InvalidInputException("Yetarli coin yo'q. Kerak: " + amount + ", Mavjud: " + current);
        }
        stats.setCoins(stats.getCoins() - amount);
        statsRepository.save(stats);
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Data @Builder
    public static class AiResponse {
        private String response;
        private int coinsSpent;
        private String type;
    }

    @Data
    public static class AiHintRequest {
        private Long problemId;
        private String code;
        private String language;
    }
}
