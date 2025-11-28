package com.example.sca_be.domain.ai.repository;

import com.example.sca_be.domain.ai.entity.StudentsFactors;
import com.example.sca_be.domain.ai.entity.StudentsQuestFactors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentsQuestFactorsRepository extends JpaRepository<StudentsQuestFactors, Long> {

    /**
     * 학생 계수 ID로 모든 난이도별 계수 조회
     */
    List<StudentsQuestFactors> findByStudentFactor(StudentsFactors studentFactor);

    /**
     * 학생 계수 ID와 난이도로 계수 조회
     */
    Optional<StudentsQuestFactors> findByStudentFactorAndDifficulty(
            StudentsFactors studentFactor,
            Integer difficulty
    );

    /**
     * 학생 계수 ID와 난이도로 존재 여부 확인
     */
    boolean existsByStudentFactorAndDifficulty(
            StudentsFactors studentFactor,
            Integer difficulty
    );
}
