package com.code.algonix.problems;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.daily.DailyChallengeService;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.gamification.RewardResult;
import com.code.algonix.gamification.RewardService;
import com.code.algonix.problems.dto.SubmissionRequest;
import com.code.algonix.problems.dto.SubmissionResponse;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final DailyChallengeService dailyChallengeService;
    private final LeetCodeExecutionService leetCodeExecutionService;
    private final SubmissionUnlockRepository submissionUnlockRepository;
    private final com.code.algonix.user.UserStatisticsRepository userStatisticsRepository;

    @Transactional
    public SubmissionResponse submitCode(SubmissionRequest request, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        // Create submission
        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(request.getLanguage())
                .status(Submission.SubmissionStatus.PENDING)
                .testResults(new ArrayList<>())
                .build();

        submission = submissionRepository.save(submission);

        // Execute code synchronously (can be made async later)
        RewardResult rewardResult = executeCode(submission, problem);

        SubmissionResponse response = mapToSubmissionResponse(submission);
        
        // Add reward info if available
        if (rewardResult != null) {
            response.setRewardInfo(SubmissionResponse.RewardInfo.builder()
                    .coinsEarned(rewardResult.getCoinsEarned())
                    .xpEarned(rewardResult.getXpEarned())
                    .leveledUp(rewardResult.isLeveledUp())
                    .oldLevel(rewardResult.getOldLevel())
                    .newLevel(rewardResult.getNewLevel())
                    .totalCoins(rewardResult.getTotalCoins())
                    .totalXp(rewardResult.getTotalXp())
                    .currentLevelXp(rewardResult.getCurrentLevelXp())
                    .xpToNextLevel(rewardResult.getXpToNextLevel())
                    .message(rewardResult.getMessage())
                    .build());
        }

        return response;
    }

    private RewardResult executeCode(Submission submission, Problem problem) {
        RewardResult rewardResult = null;
        try {
            // LeetCode style kod bajarish
            CodeExecutionService.ExecutionResult executionResult = leetCodeExecutionService.executeCode(
                    submission.getCode(),
                    submission.getLanguage(),
                    problem.getTestCases()
            );

            // Map execution results to test results
            List<TestResult> testResults = new ArrayList<>();
            for (CodeExecutionService.TestCaseResult tcResult : executionResult.getTestResults()) {
                TestCase testCase = problem.getTestCases().stream()
                        .filter(tc -> tc.getId().equals(tcResult.getTestCaseId()))
                        .findFirst()
                        .orElse(null);

                TestResult.TestStatus status = switch (tcResult.getStatus()) {
                    case ACCEPTED -> TestResult.TestStatus.PASSED;
                    case WRONG_ANSWER -> TestResult.TestStatus.FAILED;
                    case TIME_LIMIT_EXCEEDED -> TestResult.TestStatus.TIME_LIMIT_EXCEEDED;
                    case RUNTIME_ERROR -> TestResult.TestStatus.RUNTIME_ERROR;
                    default -> TestResult.TestStatus.FAILED;
                };

                TestResult result = TestResult.builder()
                        .submission(submission)
                        .testCase(testCase)
                        .status(status)
                        .runtime(tcResult.getRuntime())
                        .memory(tcResult.getMemory())
                        .input(tcResult.getInput())
                        .expectedOutput(tcResult.getExpectedOutput())
                        .actualOutput(tcResult.getActualOutput())
                        .errorMessage(tcResult.getErrorMessage())
                        .build();
                testResults.add(result);
            }

            // Set submission results
            submission.setTestResults(testResults);
            submission.setTotalTestCases(executionResult.getTotalTestCases());
            submission.setPassedTestCases(executionResult.getPassedTestCases());
            submission.setRuntime(executionResult.getAverageRuntime());
            submission.setMemory(executionResult.getAverageMemory());
            submission.setJudgedAt(LocalDateTime.now());

            // Determine submission status
            Submission.SubmissionStatus submissionStatus = switch (executionResult.getStatus()) {
                case ACCEPTED -> Submission.SubmissionStatus.ACCEPTED;
                case WRONG_ANSWER -> Submission.SubmissionStatus.WRONG_ANSWER;
                case TIME_LIMIT_EXCEEDED -> Submission.SubmissionStatus.TIME_LIMIT_EXCEEDED;
                case MEMORY_LIMIT_EXCEEDED -> Submission.SubmissionStatus.MEMORY_LIMIT_EXCEEDED;
                case RUNTIME_ERROR -> Submission.SubmissionStatus.RUNTIME_ERROR;
                case COMPILE_ERROR -> Submission.SubmissionStatus.COMPILE_ERROR;
            };
            submission.setStatus(submissionStatus);

            if (executionResult.getErrorMessage() != null) {
                submission.setErrorMessage(executionResult.getErrorMessage());
            }

            // Calculate percentiles (mock for now)
            submission.setRuntimePercentile(85.2);
            submission.setMemoryPercentile(72.5);

            // Save submission first to get ID
            submissionRepository.save(submission);

            // Process rewards if submission is accepted (AFTER saving submission)
            if (submissionStatus == Submission.SubmissionStatus.ACCEPTED) {
                rewardResult = rewardService.processSuccessfulSubmission(
                    submission.getUser(), 
                    problem, 
                    submission
                );
                // Daily challenge bonus
                dailyChallengeService.checkDailyChallenge(submission.getUser(), problem, submission);
            }

        } catch (Exception e) {
            submission.setStatus(Submission.SubmissionStatus.RUNTIME_ERROR);
            submission.setErrorMessage("Kod bajarishda xato: " + e.getMessage());
            submission.setJudgedAt(LocalDateTime.now());
            submissionRepository.save(submission);
        }

        return rewardResult;
    }

    public SubmissionResponse getSubmission(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        return mapToSubmissionResponse(submission);
    }

    public SubmissionResponse getSubmission(Long id, Long viewerUserId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        return mapToSubmissionResponse(submission, viewerUserId);
    }

    @org.springframework.transaction.annotation.Transactional
    public SubmissionResponse unlockSubmission(Long submissionId, Long viewerUserId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        // Owner — bepul
        if (submission.getUser().getId().equals(viewerUserId)) {
            return mapToSubmissionResponse(submission, viewerUserId);
        }

        // Allaqachon unlock qilingan
        if (submissionUnlockRepository.existsByUserIdAndSubmissionId(viewerUserId, submissionId)) {
            return mapToSubmissionResponse(submission, viewerUserId);
        }

        UserEntity viewer = userRepository.findById(viewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        com.code.algonix.user.UserStatistics stats = viewer.getStatistics();
        if (stats == null || stats.getCoins() < 25) {
            int current = stats != null ? stats.getCoins() : 0;
            throw new com.code.algonix.exception.InvalidInputException(
                    "Yetarli coin yo'q. Kerak: 25, Mavjud: " + current);
        }

        stats.setCoins(stats.getCoins() - 25);
        userStatisticsRepository.save(stats);

        SubmissionUnlock unlock = SubmissionUnlock.builder()
                .user(viewer)
                .submission(submission)
                .coinsSpent(25)
                .build();
        submissionUnlockRepository.save(unlock);

        return mapToSubmissionResponse(submission, viewerUserId);
    }

    public List<SubmissionResponse> getUserSubmissions(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId())
                .stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return mapToSubmissionResponse(submission, null);
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission, Long viewerUserId) {
        boolean isOwner = viewerUserId != null && submission.getUser().getId().equals(viewerUserId);
        boolean unlocked = isOwner || (viewerUserId != null &&
                submissionUnlockRepository.existsByUserIdAndSubmissionId(viewerUserId, submission.getId()));
        boolean codeVisible = isOwner || unlocked;

        List<SubmissionResponse.TestResultDto> testResults = submission.getTestResults().stream()
                .map(tr -> SubmissionResponse.TestResultDto.builder()
                        .testCaseId(tr.getTestCase() != null ? tr.getTestCase().getId() : null)
                        .status(tr.getStatus().name())
                        .runtime(tr.getRuntime())
                        .memory(tr.getMemory())
                        .input(tr.getInput())
                        .expectedOutput(tr.getExpectedOutput())
                        .actualOutput(tr.getActualOutput())
                        .errorMessage(tr.getErrorMessage())
                        .build())
                .collect(Collectors.toList());

        SubmissionResponse.OverallStats stats = SubmissionResponse.OverallStats.builder()
                .totalTestCases(submission.getTotalTestCases())
                .passedTestCases(submission.getPassedTestCases())
                .runtime(submission.getRuntime())
                .runtimePercentile(submission.getRuntimePercentile())
                .memory(submission.getMemory())
                .memoryPercentile(submission.getMemoryPercentile())
                .build();

        return SubmissionResponse.builder()
                .submissionId(submission.getId())
                .userId(submission.getUser().getId())
                .username(submission.getUser().getUsername())
                .problemId(submission.getProblem().getId())
                .code(codeVisible ? submission.getCode() : null)
                .language(submission.getLanguage())
                .status(submission.getStatus())
                .testResults(testResults)
                .overallStats(stats)
                .submittedAt(submission.getSubmittedAt())
                .judgedAt(submission.getJudgedAt())
                .codeVisible(codeVisible)
                .unlocked(unlocked)
                .unlockCost(25)
                .build();
    }
    
    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    public com.code.algonix.problems.dto.SubmissionsListResponse getSubmissionsList(
            String type, Long problemId, Long filterUserId, Long viewerUserId, int page, int size) {
        
        List<Submission> allSubmissions;
        
        if ("ME".equalsIgnoreCase(type) && filterUserId != null) {
            if (problemId != null) {
                allSubmissions = submissionRepository
                        .findByUserIdAndProblemIdOrderBySubmittedAtDesc(filterUserId, problemId);
            } else {
                allSubmissions = submissionRepository
                        .findByUserIdOrderBySubmittedAtDesc(filterUserId);
            }
        } else {
            if (problemId != null) {
                allSubmissions = submissionRepository
                        .findByProblemIdOrderBySubmittedAtDesc(problemId);
            } else {
                allSubmissions = submissionRepository
                        .findAllByOrderBySubmittedAtDesc();
            }
        }
        
        // Pagination
        long totalElements = allSubmissions.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, allSubmissions.size());
        
        List<Submission> pageSubmissions = start < allSubmissions.size()
                ? allSubmissions.subList(start, end)
                : new ArrayList<>();
        
        // Map to response
        List<com.code.algonix.problems.dto.SubmissionsListResponse.SubmissionEntry> entries = 
                pageSubmissions.stream()
                .map(s -> {
                    com.code.algonix.problems.dto.SubmissionsListResponse.SubmissionEntry entry = 
                            new com.code.algonix.problems.dto.SubmissionsListResponse.SubmissionEntry();
                    
                    entry.setId(s.getId());
                    entry.setUserId(s.getUser().getId());
                    entry.setUsername(s.getUser().getUsername());
                    entry.setProblemId(s.getProblem().getId());
                    entry.setProblemTitle(s.getProblem().getTitle());
                    entry.setLanguage(s.getLanguage());
                    entry.setStatus(s.getStatus().name());
                    
                    // Runtime and memory
                    if (s.getRuntime() != null && s.getRuntime() > 0) {
                        entry.setRuntime(s.getRuntime() + "ms");
                    } else {
                        entry.setRuntime("—");
                    }
                    
                    if (s.getMemory() != null && s.getMemory() > 0) {
                        entry.setMemory(String.format("%.2fMB", s.getMemory()));
                    } else {
                        entry.setMemory("—");
                    }
                    
                    entry.setSubmittedAt(s.getSubmittedAt());

                    // Code visibility — ME bo'lsa o'z kodi ko'rinadi, ALL bo'lsa yashirin
                    boolean isOwner = viewerUserId != null && s.getUser().getId().equals(viewerUserId);
                    boolean unlocked = isOwner || (viewerUserId != null &&
                            submissionUnlockRepository.existsByUserIdAndSubmissionId(viewerUserId, s.getId()));
                    entry.setCodeVisible(isOwner || unlocked);
                    entry.setUnlocked(unlocked);
                    entry.setUnlockCost(25);

                    return entry;
                })
                .collect(Collectors.toList());
        
        com.code.algonix.problems.dto.SubmissionsListResponse response = 
                new com.code.algonix.problems.dto.SubmissionsListResponse();
        response.setSuccess(true);
        response.setType(type != null ? type.toUpperCase() : "ALL");
        response.setProblemId(problemId);
        
        if (problemId != null) {
            Problem problem = problemRepository.findById(problemId).orElse(null);
            response.setProblemTitle(problem != null ? problem.getTitle() : null);
        }
        
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setCurrentPage(page);
        response.setData(entries);
        
        return response;
    }
}
