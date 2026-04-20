package com.code.algonix.gamification;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false)
    private BadgeType type;

    @CreationTimestamp
    private LocalDateTime earnedAt;

    public enum BadgeType {
        FIRST_SOLVE("Birinchi qadam", "Birinchi masalani hal qilding!", "🎯"),
        SOLVE_10("10 masala", "10 ta masala hal qilding!", "⭐"),
        SOLVE_50("50 masala", "50 ta masala hal qilding!", "🌟"),
        SOLVE_100("100 masala", "100 ta masala hal qilding!", "💯"),
        FIRST_HARD("Qiyin masala", "Birinchi HARD masalani hal qilding!", "🔥"),
        FIRST_MEDIUM("O'rta masala", "Birinchi MEDIUM masalani hal qilding!", "💪"),
        STREAK_7("Haftalik streak", "7 kun ketma-ket masala yechdingiz!", "📅"),
        STREAK_30("Oylik streak", "30 kun ketma-ket masala yechdingiz!", "🏆"),
        CONTEST_WIN("Musobaqa g'olibi", "Contestda 1-o'rin oldingiz!", "🥇"),
        SPEED_DEMON("Tezkor yechuvchi", "Masalani 100ms dan tez hal qilding!", "⚡"),
        COIN_100("Boylik", "100 ta coin to'pladingiz!", "💰"),
        LEVEL_10("10-daraja", "10-darajaga yetdingiz!", "🎖️");

        public final String name;
        public final String description;
        public final String emoji;

        BadgeType(String name, String description, String emoji) {
            this.name = name;
            this.description = description;
            this.emoji = emoji;
        }
    }
}
