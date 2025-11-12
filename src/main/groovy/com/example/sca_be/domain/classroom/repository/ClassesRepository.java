package com.example.sca_be.domain.classroom.repository;

import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.classroom.entity.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassesRepository extends JpaRepository<Classes, Integer> {
    Optional<Classes> findByInviteCode(String inviteCode);

    List<Classes> findByTeacherOrderByCreatedAtDesc(Teacher teacher);

    boolean existsByInviteCode(String inviteCode);
}