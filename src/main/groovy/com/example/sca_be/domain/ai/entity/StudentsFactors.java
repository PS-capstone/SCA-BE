package com.example.sca_be.domain.ai.entity;

import com.example.sca_be.domain.auth.entity.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "students_factors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class StudentsFactors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @ColumnDefault("1.0")
    @Column(name = "global_factor", nullable = false)
    private Double globalFactor;

    @ColumnDefault("false")
    @Column(name = "initialized", nullable = false)
    private Boolean initialized;

    @Column(name = "initial_score")
    private Integer initialScore;

    @Column(name = "initialized_at")
    private LocalDateTime initializedAt;

    @ColumnDefault("0")
    @Column(name = "total_learning_count", nullable = false)
    private Integer totalLearningCount;

    @Column(name = "last_learning_at")
    private LocalDateTime lastLearningAt;

    @Column(name = "avg_modification_rate")
    private Double avgModificationRate;

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
    public StudentsFactors(Student student, Double globalFactor, Boolean initialized, Integer initialScore) {
        this.student = student;
        this.globalFactor = globalFactor;
        this.initialized = initialized;
        this.initialScore = initialScore;
    }

    // 계수 초기화 메서드
    public void initialize(Integer initialScore) {
        this.initialScore = initialScore;
        this.globalFactor = 1.0 + (75.0 - initialScore) / 100.0;
        this.initialized = true;
        this.initializedAt = LocalDateTime.now();
    }

    // 학습 횟수 증가 메서드
    public void incrementLearningCount() {
        this.totalLearningCount = (this.totalLearningCount != null ? this.totalLearningCount : 0) + 1;
        this.lastLearningAt = LocalDateTime.now();
    }

    // 계수 업데이트 메서드
    public void updateGlobalFactor(Double newFactor) {
        if (newFactor < 0.5 || newFactor > 1.5) {
            throw new IllegalArgumentException("Global factor must be between 0.5 and 1.5");
        }
        this.globalFactor = newFactor;
    }

    // 평균 수정률 업데이트 메서드 (지수 이동 평균)
    public void updateAvgModificationRate(Double modificationRate) {
        if (this.avgModificationRate == null) {
            this.avgModificationRate = modificationRate;
        } else {
            // EMA: alpha = 0.3
            this.avgModificationRate = 0.3 * modificationRate + 0.7 * this.avgModificationRate;
        }
    }
}
