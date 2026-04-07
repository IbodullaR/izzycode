package com.code.algonix.solution;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.exception.InvalidInputException;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.problems.Problem;
import com.code.algonix.problems.ProblemRepository;
import com.code.algonix.problems.Submission;
import com.code.algonix.problems.SubmissionRepository;
import com.code.algonix.solution.dto.CreateSolutionRequest;
import com.code.algonix.solution.dto.SolutionResponse;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;
import com.code.algonix.user.UserStatistics;
import com.code.algonix.user.UserStatisticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolutionService {

    private static final int UNLOCK_COST = 25;

    private final SolutionRepository solutionRepository;
    private final SolutionUnlockRepository unlockRepository;
    private final SolutionLikeRepository likeRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final UserStatisticsRepository statsRepository;

    /** List all published solutions for a problem (code hidden unless unlocked) */
    @Transactional(readOnly = true)
    public List<SolutionResponse> getSolutionsForProblem(Long problemId, String username) {
        Long userId = username != null ? getUserId(username) : null;
        return solutionRepository
                .findByProblemIdAndPublishedTrueOrderByLikesDesc(problemId)
                .stream()
                .map(s -> toResponse(s, userId))
                .toList();
    }

    /** Publish a new solution (must have an ACCEPTED submission for the problem) */
    @Transactional
    public SolutionResponse createSolution(CreateSolutionRequest req, String username) {
        UserEntity author = getUser(username);
        Problem problem = problemRepository.findById(req.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        // Must have at least one accepted submission
        boolean hasAccepted = submissionRepository
                .existsByUserAndProblemAndStatus(author, problem, Submission.SubmissionStatus.ACCEPTED);
        if (!hasAccepted) {
            throw new InvalidInputException("Yechim publish qilish uchun avval masalani hal qiling");
        }

        // One solution per user per problem
        if (solutionRepository.existsByAuthorIdAndProblemId(author.getId(), problem.getId())) {
            throw new InvalidInputException("Siz bu masala uchun allaqachon yechim publish qilgansiz");
        }

        Submission submission = null;
        if (req.getSubmissionId() != null) {
            submission = submissionRepository.findById(req.getSubmissionId()).orElse(null);
        }

        Solution solution = Solution.builder()
                .author(author)
                .problem(problem)
                .submission(submission)
                .title(req.getTitle())
                .description(req.getDescription())
                .language(req.getLanguage())
                .code(req.getCode())
                .build();

        solution = solutionRepository.save(solution);
        return toResponse(solution, author.getId());
    }

    /** Unlock a solution for 25 coins — returns solution with code */
    @Transactional
    public SolutionResponse unlockSolution(Long solutionId, String username) {
        UserEntity user = getUser(username);
        Solution solution = getSolution(solutionId);

        // Author can always see their own code
        if (solution.getAuthor().getId().equals(user.getId())) {
            return toResponse(solution, user.getId());
        }

        // Already unlocked?
        if (unlockRepository.existsByUserIdAndSolutionId(user.getId(), solutionId)) {
            return toResponse(solution, user.getId());
        }

        // Check coins
        UserStatistics stats = getOrCreateStats(user);
        if (stats.getCoins() < UNLOCK_COST) {
            throw new InvalidInputException("Yetarli coin yo'q. Kerak: " + UNLOCK_COST + ", Mavjud: " + stats.getCoins());
        }

        // Deduct coins
        stats.setCoins(stats.getCoins() - UNLOCK_COST);
        statsRepository.save(stats);

        // Record unlock
        SolutionUnlock unlock = SolutionUnlock.builder()
                .user(user)
                .solution(solution)
                .coinsSpent(UNLOCK_COST)
                .build();
        unlockRepository.save(unlock);

        // Increment counters
        solution.setUnlockCount(solution.getUnlockCount() + 1);
        solution.setViews(solution.getViews() + 1);
        solutionRepository.save(solution);

        return toResponse(solution, user.getId());
    }

    /** Toggle like on a solution */
    @Transactional
    public SolutionResponse toggleLike(Long solutionId, String username) {
        UserEntity user = getUser(username);
        Solution solution = getSolution(solutionId);

        if (likeRepository.existsByUserIdAndSolutionId(user.getId(), solutionId)) {
            likeRepository.deleteByUserIdAndSolutionId(user.getId(), solutionId);
            solution.setLikes(Math.max(0, solution.getLikes() - 1));
        } else {
            SolutionLike like = SolutionLike.builder().user(user).solution(solution).build();
            likeRepository.save(like);
            solution.setLikes(solution.getLikes() + 1);
        }

        solution = solutionRepository.save(solution);
        return toResponse(solution, user.getId());
    }

    // ---- helpers ----

    private SolutionResponse toResponse(Solution s, Long viewerUserId) {
        boolean isAuthor = viewerUserId != null && s.getAuthor().getId().equals(viewerUserId);
        boolean unlocked = isAuthor ||
                (viewerUserId != null && unlockRepository.existsByUserIdAndSolutionId(viewerUserId, s.getId()));
        boolean liked = viewerUserId != null &&
                likeRepository.existsByUserIdAndSolutionId(viewerUserId, s.getId());

        return SolutionResponse.builder()
                .id(s.getId())
                .authorId(s.getAuthor().getId())
                .authorUsername(s.getAuthor().getUsername())
                .authorAvatar(s.getAuthor().getAvatarUrl())
                .problemId(s.getProblem().getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .language(s.getLanguage())
                .code(unlocked ? s.getCode() : null)
                .unlocked(unlocked)
                .liked(liked)
                .likes(s.getLikes())
                .views(s.getViews())
                .unlockCount(s.getUnlockCount())
                .createdAt(s.getCreatedAt())
                .unlockCost(UNLOCK_COST)
                .build();
    }

    private Solution getSolution(Long id) {
        return solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found"));
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long getUserId(String username) {
        return getUser(username).getId();
    }

    private UserStatistics getOrCreateStats(UserEntity user) {
        UserStatistics stats = user.getStatistics();
        if (stats == null) {
            stats = UserStatistics.builder().user(user).coins(0).build();
            user.setStatistics(stats);
        }
        return stats;
    }
}
