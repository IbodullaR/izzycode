package com.code.algonix.interview;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.code.algonix.problems.Problem;
import com.code.algonix.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interview_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder.Default
    private Integer durationMinutes = 45; // default 45 daqiqa

    // Submission natijasi
    private String submittedCode;
    private String submittedLanguage;
    private Boolean passed;
    private Long timeTakenSeconds; // necha sekundda yechdi

    // AI feedback
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    @Builder.Default
    private Integer aiScore = 0; // 1-10

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum SessionStatus {
        ACTIVE,     // davom etmoqda
        COMPLETED,  // muvaffaqiyatli yakunlandi
        EXPIRED,    // vaqt tugadi
        ABANDONED   // foydalanuvchi tark etdi
    }
}
