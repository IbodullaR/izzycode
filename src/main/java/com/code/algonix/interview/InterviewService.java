package com.code.algonix.interview;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.algonix.ai.GroqService;
import com.code.algonix.exception.InvalidInputException;
import com.code.algonix.exception.ResourceNotFoundException;
import com.code.algonix.problems.Problem;
import com.code.algonix.problems.ProblemRepository;
import com.code.algonix.problems.Submission;
import com.code.algonix.problems.SubmissionService;
import com.code.algonix.problems.dto.SubmissionRequest;
import com.code.algonix.user.UserEntity;
import com.code.algonix.user.UserRepository;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionService submissionService;
    private final GroqService groqService;

    /** Interview sessiyasini boshlash */
    @Transactional
    public SessionResponse startSession(String username, Problem.Difficulty difficulty, Integer durationMinutes) {
        UserEntity user = getUser(username);

        // Faol sessiya bormi?
        if (sessionRepository.existsByUserIdAndStatus(user.getId(), InterviewSession.SessionStatus.ACTIVE)) {
            throw new InvalidInputException("Sizda allaqachon faol interview sessiyasi bor");
        }

        // Masala tanlash
        Problem problem = selectProblem(difficulty);

        int duration = durationMinutes != null ? durationMinutes : 45;

        InterviewSession session = InterviewSession.builder()
                .user(user)
                .problem(problem)
                .startTime(LocalDateTime.now())
                .durationMinutes(duration)
                .status(InterviewSession.SessionStatus.ACTIVE)
                .build();

        session = sessionRepository.save(session);
        log.info("Interview started: user={}, problem={}, duration={}min", username, problem.getTitle(), duration);

        return toResponse(session);
    }

    /** Kod yuborish va natijani olish */
    @Transactional
    public SessionResponse submitCode(Long sessionId, String code, String language, String username) {
        InterviewSession session = getActiveSession(sessionId, username);

        // Vaqt tekshirish
        LocalDateTime deadline = session.getStartTime().plusMinutes(session.getDurationMinutes());
        if (LocalDateTime.now().isAfter(deadline)) {
            session.setStatus(InterviewSession.SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new InvalidInputException("Vaqt tugadi! Interview yakunlandi.");
        }

        // Kodni bajarish
        SubmissionRequest req = new SubmissionRequest();
        req.setProblemId(session.getProblem().getId());
        req.setCode(code);
        req.setLanguage(language);

        var submissionResponse = submissionService.submitCode(req, username);
        boolean passed = submissionResponse.getStatus() == Submission.SubmissionStatus.ACCEPTED;

        long timeTaken = ChronoUnit.SECONDS.between(session.getStartTime(), LocalDateTime.now());

        // AI feedback olish
        String aiFeedback = getAiFeedback(session.getProblem(), code, language, passed);
        int aiScore = calculateScore(passed, timeTaken, session.getDurationMinutes());

        session.setSubmittedCode(code);
        session.setSubmittedLanguage(language);
        session.setPassed(passed);
        session.setTimeTakenSeconds(timeTaken);
        session.setAiFeedback(aiFeedback);
        session.setAiScore(aiScore);
        session.setEndTime(LocalDateTime.now());
        session.setStatus(InterviewSession.SessionStatus.COMPLETED);

        session = sessionRepository.save(session);
        return toResponse(session);
    }

    /** Vaqt tugadi yoki tark etildi */
    @Transactional
    public SessionResponse endSession(Long sessionId, String username, boolean abandoned) {
        InterviewSession session = getActiveSession(sessionId, username);
        session.setEndTime(LocalDateTime.now());
        session.setStatus(abandoned ? InterviewSession.SessionStatus.ABANDONED : InterviewSession.SessionStatus.EXPIRED);
        session = sessionRepository.save(session);
        return toResponse(session);
    }

    /** Faol sessiyani olish */
    @Transactional(readOnly = true)
    public SessionResponse getActiveSession(String username) {
        UserEntity user = getUser(username);
        InterviewSession session = sessionRepository
                .findByUserIdAndStatus(user.getId(), InterviewSession.SessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Faol interview sessiyasi topilmadi"));

        // Vaqt tugaganini tekshirish
        LocalDateTime deadline = session.getStartTime().plusMinutes(session.getDurationMinutes());
        if (LocalDateTime.now().isAfter(deadline)) {
            session.setStatus(InterviewSession.SessionStatus.EXPIRED);
            sessionRepository.save(session);
        }

        return toResponse(session);
    }

    /** Tarix */
    @Transactional(readOnly = true)
    public List<SessionResponse> getHistory(String username) {
        UserEntity user = getUser(username);
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    // ---- Private ----

    private Problem selectProblem(Problem.Difficulty difficulty) {
        List<Problem> problems = difficulty != null
                ? problemRepository.findByDifficulty(difficulty, org.springframework.data.domain.Pageable.unpaged()).getContent()
                : problemRepository.findAll();

        if (problems.isEmpty()) throw new ResourceNotFoundException("Masala topilmadi");

        return problems.get(new Random().nextInt(problems.size()));
    }

    private String getAiFeedback(Problem problem, String code, String language, boolean passed) {
        String systemPrompt = """
                Siz tajribali dasturchi va intervyu o'tkazuvchisiz.
                Foydalanuvchi intervyu masalasini yechdi. Qisqa va aniq feedback bering.
                O'zbek tilida javob bering. 3-5 jumlada.
                """;

        String userMessage = String.format("""
                Masala: %s
                Natija: %s
                Kod (%s):
                ```
                %s
                ```
                Iltimos, qisqa feedback bering: vaqt murakkabligi, kod sifati, yaxshilash tavsiyasi.
                """,
                problem.getTitle(),
                passed ? "ACCEPTED ✅" : "FAILED ❌",
                language,
                code != null ? code.substring(0, Math.min(1500, code.length())) : "");

        return groqService.chat(systemPrompt, userMessage);
    }

    private int calculateScore(boolean passed, long timeTakenSeconds, int durationMinutes) {
        if (!passed) return 3;
        long totalSeconds = (long) durationMinutes * 60;
        double ratio = (double) timeTakenSeconds / totalSeconds;
        if (ratio < 0.3) return 10;
        if (ratio < 0.5) return 9;
        if (ratio < 0.7) return 8;
        if (ratio < 0.85) return 7;
        return 6;
    }

    private InterviewSession getActiveSession(Long sessionId, String username) {
        UserEntity user = getUser(username);
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new InvalidInputException("Ruxsat yo'q");
        }
        if (session.getStatus() != InterviewSession.SessionStatus.ACTIVE) {
            throw new InvalidInputException("Sessiya faol emas");
        }
        return session;
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SessionResponse toResponse(InterviewSession s) {
        LocalDateTime deadline = s.getStartTime() != null
                ? s.getStartTime().plusMinutes(s.getDurationMinutes()) : null;
        long remainingSeconds = 0;
        if (deadline != null && s.getStatus() == InterviewSession.SessionStatus.ACTIVE) {
            remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(), deadline));
        }

        return SessionResponse.builder()
                .id(s.getId())
                .problemId(s.getProblem().getId())
                .problemSlug(s.getProblem().getSlug())
                .problemTitle(s.getProblem().getTitle())
                .difficulty(s.getProblem().getDifficulty().name())
                .status(s.getStatus().name())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .durationMinutes(s.getDurationMinutes())
                .remainingSeconds(remainingSeconds)
                .passed(s.getPassed())
                .timeTakenSeconds(s.getTimeTakenSeconds())
                .aiFeedback(s.getAiFeedback())
                .aiScore(s.getAiScore())
                .build();
    }

    // ---- DTOs ----

    @Data @Builder
    public static class SessionResponse {
        private Long id;
        private Long problemId;
        private String problemSlug;
        private String problemTitle;
        private String difficulty;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
        private long remainingSeconds;
        private Boolean passed;
        private Long timeTakenSeconds;
        private String aiFeedback;
        private Integer aiScore;
    }

    @Data
    public static class StartRequest {
        private Problem.Difficulty difficulty;
        private Integer durationMinutes;
    }

    @Data
    public static class SubmitRequest {
        private String code;
        private String language;
    }
}
