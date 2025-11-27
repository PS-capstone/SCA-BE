package com.example.sca_be.domain.ai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "students_quest_factors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class StudentsQuestFactors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_factor_id", nullable = false)
    private StudentsFactors studentFactor;

    @Min(1)
    @Max(5)
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @Column(name = "factor_value", nullable = false)
    private Double factorValue;

    @ColumnDefault("0")
    @Column(name = "learning_count", nullable = false)
    private Integer learningCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public StudentsQuestFactors(StudentsFactors studentFactor, Integer difficulty, Double factorValue) {
        this.studentFactor = studentFactor;
        if (difficulty != null && (difficulty < 1 || difficulty > 5)) {
            throw new IllegalArgumentException("Difficulty must be between 1 and 5");
        }
        this.difficulty = difficulty;
        this.factorValue = factorValue;
    }

    // 학습 횟수 증가 메서드
    public void incrementLearningCount() {
        this.learningCount = (this.learningCount != null ? this.learningCount : 0) + 1;
    }

    // 계수 업데이트 메서드
    public void updateFactorValue(Double newFactorValue) {
        if (newFactorValue < 0.5 || newFactorValue > 1.5) {
            throw new IllegalArgumentException("Factor value must be between 0.5 and 1.5");
        }
        this.factorValue = newFactorValue;
    }
}
