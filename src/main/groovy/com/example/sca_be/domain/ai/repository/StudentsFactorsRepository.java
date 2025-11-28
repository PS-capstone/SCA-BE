package com.example.sca_be.domain.ai.repository;

import com.example.sca_be.domain.ai.entity.StudentsFactors;
import com.example.sca_be.domain.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentsFactorsRepository extends JpaRepository<StudentsFactors, Long> {

    /**
     * 학생 ID로 계수 조회
     */
    Optional<StudentsFactors> findByStudent(Student student);

    /**
     * 학생 ID로 계수 존재 여부 확인
     */
    boolean existsByStudent(Student student);
}
