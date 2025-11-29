package com.example.sca_be.domain.notification.repository;

import com.example.sca_be.domain.notification.entity.ActionLog;
import com.example.sca_be.domain.notification.entity.ActionLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    @Query("SELECT al FROM ActionLog al " +
           "JOIN FETCH al.student s " +
           "WHERE s.classes.classId = :classId " +
           "AND al.createdAt >= :startDate " +
           "AND al.createdAt <= :endDate " +
           "AND al.changeCoral > 0")
    List<ActionLog> findByClassAndDateRangeWithCoralReward(
            @Param("classId") Integer classId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all activity logs for a specific student, ordered by creation date (newest first)
     * @param studentId The student's member ID
     * @return List of activity logs
     */
    List<ActionLog> findByStudent_MemberIdOrderByCreatedAtDesc(Integer studentId);

    /**
     * Find activity logs for a specific student filtered by type, ordered by creation date (newest first)
     * @param studentId The student's member ID
     * @param type The action log type to filter by
     * @return List of activity logs
     */
    List<ActionLog> findByStudent_MemberIdAndActionLogTypeOrderByCreatedAtDesc(
            Integer studentId,
            ActionLogType type
    );

    /**
     * Find top N activity logs for a specific student, ordered by creation date (newest first)
     * @param studentId The student's member ID
     * @param limit Maximum number of logs to return
     * @return List of activity logs
     */
    @Query("SELECT al FROM ActionLog al WHERE al.student.memberId = :studentId ORDER BY al.createdAt DESC LIMIT :limit")
    List<ActionLog> findTopNByStudentId(@Param("studentId") Integer studentId, @Param("limit") int limit);
}
