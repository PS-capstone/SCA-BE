package com.example.sca_be.domain.groupquest.repository;

import com.example.sca_be.domain.groupquest.entity.GroupQuestProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupQuestProgressRepository extends JpaRepository<GroupQuestProgress, Integer> {

    @Query("SELECT gqp FROM GroupQuestProgress gqp WHERE gqp.groupQuest.groupQuestId = :questId")
    List<GroupQuestProgress> findByGroupQuestId(@Param("questId") Integer questId);

    @Query("SELECT gqp FROM GroupQuestProgress gqp WHERE gqp.groupQuest.groupQuestId = :questId AND gqp.student.memberId = :studentId")
    Optional<GroupQuestProgress> findByGroupQuestIdAndStudentId(@Param("questId") Integer questId, @Param("studentId") Integer studentId);

    @Query("SELECT COUNT(gqp) FROM GroupQuestProgress gqp WHERE gqp.groupQuest.groupQuestId = :questId AND gqp.isCompleted = true")
    Integer countCompletedByGroupQuestId(@Param("questId") Integer questId);
}
