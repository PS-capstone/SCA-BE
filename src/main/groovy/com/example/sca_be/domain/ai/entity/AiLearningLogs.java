package com.example.sca_be.domain.ai.entity;

import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.personalquest.entity.QuestAssignment;
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
@Table(name = "ai_learning_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class AiLearningLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private QuestAssignment questAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Min(1)
    @Max(5)
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @Column(name = "cognitive_score", nullable = false)
    private Integer cognitiveScore;

    @Column(name = "effort_score", nullable = false)
    private Integer effortScore;

    @Column(name = "ai_coral", nullable = false)
    private Integer aiCoral;

    @Column(name = "ai_research_data", nullable = false)
    private Integer aiResearchData;

    @Column(name = "teacher_coral", nullable = false)
    private Integer teacherCoral;

    @Column(name = "teacher_research_data", nullable = false)
    private Integer teacherResearchData;

    @ColumnDefault("false")
    @Column(name = "learned", nullable = false)
    private Boolean learned;

    @Column(name = "learned_at")
    private LocalDateTime learnedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public AiLearningLogs(QuestAssignment questAssignment, Student student, Integer difficulty,
                          Integer cognitiveScore, Integer effortScore,
                          Integer aiCoral, Integer aiResearchData,
                          Integer teacherCoral, Integer teacherResearchData) {
        this.questAssignment = questAssignment;
        this.student = student;
        if (difficulty != null && (difficulty < 1 || difficulty > 5)) {
            throw new IllegalArgumentException("Difficulty must be between 1 and 5");
        }
        this.difficulty = difficulty;
        this.cognitiveScore = cognitiveScore;
        this.effortScore = effortScore;
        this.aiCoral = aiCoral;
        this.aiResearchData = aiResearchData;
        this.teacherCoral = teacherCoral;
        this.teacherResearchData = teacherResearchData;
    }

    // 학습 처리 완료 메서드
    public void markAsLearned() {
        this.learned = true;
        this.learnedAt = LocalDateTime.now();
    }
}
