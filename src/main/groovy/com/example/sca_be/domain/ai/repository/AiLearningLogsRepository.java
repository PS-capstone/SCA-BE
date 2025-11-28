package com.example.sca_be.domain.ai.repository;

import com.example.sca_be.domain.ai.entity.AiLearningLogs;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.personalquest.entity.QuestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiLearningLogsRepository extends JpaRepository<AiLearningLogs, Long> {

    /**
     * 학생 ID로 학습 로그 조회
     */
    List<AiLearningLogs> findByStudent(Student student);

    /**
     * 할당 ID로 학습 로그 조회
     */
    Optional<AiLearningLogs> findByQuestAssignment(QuestAssignment questAssignment);

    /**
     * 학습되지 않은 로그 조회 (learned = false)
     */
    List<AiLearningLogs> findByLearnedFalse();

    /**
     * 학생 ID와 난이도로 최근 학습 로그 조회
     */
    @Query("SELECT l FROM AiLearningLogs l " +
           "WHERE l.student = :student AND l.difficulty = :difficulty " +
           "ORDER BY l.createdAt DESC")
    List<AiLearningLogs> findRecentLogsByStudentAndDifficulty(
            @Param("student") Student student,
            @Param("difficulty") Integer difficulty
    );
}
