package com.example.sca_be.domain.groupquest.repository;

import com.example.sca_be.domain.groupquest.entity.GroupQuest;
import com.example.sca_be.domain.groupquest.entity.GroupQuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupQuestRepository extends JpaRepository<GroupQuest, Integer> {

    @Query("SELECT gq FROM GroupQuest gq WHERE gq.classes.classId = :classId")
    List<GroupQuest> findByClassId(@Param("classId") Integer classId);

    @Query("SELECT gq FROM GroupQuest gq WHERE gq.classes.classId = :classId AND gq.status = :status")
    List<GroupQuest> findByClassIdAndStatus(@Param("classId") Integer classId, @Param("status") GroupQuestStatus status);

    @Query("SELECT gq FROM GroupQuest gq LEFT JOIN FETCH gq.progressList WHERE gq.groupQuestId = :questId")
    Optional<GroupQuest> findByIdWithProgress(@Param("questId") Integer questId);
}
