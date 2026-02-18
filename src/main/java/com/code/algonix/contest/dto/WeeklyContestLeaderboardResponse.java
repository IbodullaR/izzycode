package com.code.algonix.contest.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyContestLeaderboardResponse {
    private List<ParticipantRanking> topParticipants;
    private Long totalParticipants;
    private String period; // "HAFTALIK PESHQADAMLAR" yoki contest nomi
    private Integer page;
    private Integer pageSize;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantRanking {
        private Long userId;
        private String username;
        private Integer ranking;
        private Integer totalScore; // Ball
        private Integer contestsParticipated; // Qatnashgan contestlar soni (faqat haftalik uchun)
        private Integer problemsSolved; // Yechilgan masalalar soni
    }
}
