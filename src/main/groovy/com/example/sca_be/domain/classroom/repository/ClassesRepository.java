package com.example.sca_be.domain.classroom.repository;

import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.classroom.entity.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassesRepository extends JpaRepository<Classes, Integer> {
    Optional<Classes> findByInviteCode(String inviteCode);

    List<Classes> findByTeacherOrderByCreatedAtDesc(Teacher teacher);
<<<<<<< HEAD
=======
    
    // teacher_id로 직접 조회 (더 안정적)
    List<Classes> findByTeacher_MemberIdOrderByCreatedAtDesc(Integer teacherId);
>>>>>>> 31173bfecbab8de3fb1a27ec81d0030d72e3a49c

    boolean existsByInviteCode(String inviteCode);
}