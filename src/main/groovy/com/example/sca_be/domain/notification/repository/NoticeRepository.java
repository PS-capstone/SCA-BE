package com.example.sca_be.domain.notification.repository;

import com.example.sca_be.domain.notification.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * Find all notices for a specific student, ordered by creation date (newest first)
     * @param studentId The student's member ID
     * @return List of notices
     */
    List<Notice> findByStudent_MemberIdOrderByCreatedAtDesc(Integer studentId);

    /**
     * Find top N notices for a specific student, ordered by creation date (newest first)
     * @param studentId The student's member ID
     * @param limit Maximum number of notices to return
     * @return List of notices
     */
    @Query("SELECT n FROM Notice n WHERE n.student.memberId = :studentId ORDER BY n.createdAt DESC LIMIT :limit")
    List<Notice> findTopNByStudentId(@Param("studentId") Integer studentId, @Param("limit") int limit);

    /**
     * Count unread notices for a specific student
     * (Note: is_read field doesn't exist in entity, this is for future use if needed)
     */
    // Long countByStudent_MemberIdAndIsReadFalse(Integer studentId);
}
