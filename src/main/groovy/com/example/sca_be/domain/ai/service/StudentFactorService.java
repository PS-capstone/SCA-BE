package com.example.sca_be.domain.ai.service;

import com.example.sca_be.domain.ai.config.AIConstants;
import com.example.sca_be.domain.ai.dto.BaseReward;
import com.example.sca_be.domain.ai.dto.PersonalizedReward;
import com.example.sca_be.domain.ai.entity.QuestDifficulty;
import com.example.sca_be.domain.ai.entity.StudentsFactors;
import com.example.sca_be.domain.ai.entity.StudentsQuestFactors;
import com.example.sca_be.domain.ai.repository.StudentsFactorsRepository;
import com.example.sca_be.domain.ai.repository.StudentsQuestFactorsRepository;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학생 보정계수 관리 서비스
 * 파이썬 student_factor_manage.py를 Spring으로 이식
 *
 * 역할:
 * 1. 계수 초기화 (선생님의 초기 성적 기반)
 * 2. 계수 조회 및 최종 계수 계산
 * 3. 기본 보상 계산 (모든 학생 공통)
 * 4. 개인화 보상 계산 (학생별 계수 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentFactorService {

    private final StudentsFactorsRepository studentsFactorsRepository;
    private final StudentsQuestFactorsRepository studentsQuestFactorsRepository;
    private final StudentRepository studentRepository;

    /**
     * 1. 계수 초기화
     * PDF 2.3 참고
     *
     * 공식: initialFactor = 1.0 + (75 - initialScore) / 100
     * 범위: 0.75 ~ 1.25
     *
     * @param studentId 학생 ID
     * @param initialScore 초기 성적 (0-100)
     */
    @Transactional
    public void initializeFactor(Integer studentId, Integer initialScore) {
        log.info("Initializing factor for student {} with score {}", studentId, initialScore);

        // 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentId));

        // 계수 계산
        double initialFactor = 1.0 + (AIConstants.BASELINE_SCORE - initialScore) / 100.0;
        double limitedFactor = Math.max(
                AIConstants.INITIAL_FACTOR_MIN,
                Math.min(AIConstants.INITIAL_FACTOR_MAX, initialFactor)
        );

        // 기존 계수 조회 또는 생성
        StudentsFactors studentFactor = studentsFactorsRepository.findByStudent(student)
                .orElseGet(() -> StudentsFactors.builder()
                        .student(student)
                        .build());

        // 초기화 메서드 호출 (새로 생성되든 기존 것이든 모두)
        studentFactor.initialize(initialScore);

        studentsFactorsRepository.save(studentFactor);

        log.info("Factor initialized - Student: {}, Score: {}, Global Factor: {}",
                studentId, initialScore, String.format("%.3f", limitedFactor));
    }

    /**
     * 2. 최종 계수 조회
     * PDF Step 4-1 참고
     *
     * 난이도별 계수가 있는 경우: 0.6 × questFactor + 0.4 × globalFactor
     * 없는 경우: globalFactor
     *
     * @param studentId 학생 ID
     * @param difficulty 퀘스트 난이도
     * @return 최종 적용 계수
     */
    @Transactional(readOnly = true)
    public Double getEffectiveFactor(Integer studentId, QuestDifficulty difficulty) {
        log.debug("Getting effective factor for student {} with difficulty {}", studentId, difficulty);

        // 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentId));

        // 학생 계수 조회 (없으면 자동 생성)
        StudentsFactors studentFactor = getOrCreateStudentFactor(student);

        // 난이도가 없으면 global_factor 반환
        if (difficulty == null) {
            return studentFactor.getGlobalFactor();
        }

        // 난이도별 계수 조회
        Integer difficultyValue = mapDifficultyToInt(difficulty);
        StudentsQuestFactors questFactor = studentsQuestFactorsRepository
                .findByStudentFactorAndDifficulty(studentFactor, difficultyValue)
                .orElse(null);

        // 난이도별 계수가 없으면 global_factor 반환
        if (questFactor == null) {
            log.debug("No quest factor found, using global factor: {}", studentFactor.getGlobalFactor());
            return studentFactor.getGlobalFactor();
        }

        // 최종 계수 = 0.6 × questFactor + 0.4 × globalFactor
        double effectiveFactor = AIConstants.DIFFICULTY_WEIGHT * questFactor.getFactorValue()
                + AIConstants.GLOBAL_WEIGHT * studentFactor.getGlobalFactor();

        log.debug("Effective factor calculated: {} (quest: {}, global: {})",
                String.format("%.3f", effectiveFactor),
                String.format("%.3f", questFactor.getFactorValue()),
                String.format("%.3f", studentFactor.getGlobalFactor()));

        return effectiveFactor;
    }

    /**
     * 3. 기본 보상 계산 (정적 메서드)
     * PDF Step 3 참고
     *
     * explorationData = (cognitive² × 5) + (effort × 2)
     * coral = (effort × 5) + (cognitive × 2)
     *
     * @param cognitive 인지 과정 점수 (1-6)
     * @param effort 노력 점수 (1-10)
     * @return 기본 보상
     */
    public static BaseReward calculateBaseReward(Integer cognitive, Integer effort) {
        log.debug("Calculating base reward - Cognitive: {}, Effort: {}", cognitive, effort);

        // explorationData = (cognitive² × 5) + (effort × 2)
        int explorationData = (cognitive * cognitive) * AIConstants.EXPLORATION_COGNITIVE_WEIGHT
                + effort * AIConstants.EXPLORATION_EFFORT_WEIGHT;

        // coral = (effort × 5) + (cognitive × 2)
        int coral = effort * AIConstants.CORAL_EFFORT_WEIGHT
                + cognitive * AIConstants.CORAL_COGNITIVE_WEIGHT;

        log.debug("Base reward calculated - Exploration: {}, Coral: {}", explorationData, coral);

        return BaseReward.builder()
                .explorationData(explorationData)
                .coral(coral)
                .build();
    }

    /**
     * 4. 개인화 보상 계산
     * PDF Step 4-2 참고
     *
     * @param studentId 학생 ID (member_id)
     * @param cognitive 인지 과정 점수
     * @param effort 노력 점수
     * @param difficulty 퀘스트 난이도
     * @return 개인화된 보상
     */
    @Transactional(readOnly = true)
    public PersonalizedReward calculatePersonalizedReward(
            Integer studentId,
            Integer cognitive,
            Integer effort,
            QuestDifficulty difficulty) {

        log.info("Calculating personalized reward for student {}", studentId);

        // 1. 기본 보상 계산
        BaseReward baseReward = calculateBaseReward(cognitive, effort);

        // 2. 최종 계수 조회
        Double effectiveFactor = getEffectiveFactor(studentId, difficulty);

        // 3. 개인화 보상 = 기본 보상 × 최종 계수
        int personalizedExploration = (int) Math.round(baseReward.getExplorationData() * effectiveFactor);
        int personalizedCoral = (int) Math.round(baseReward.getCoral() * effectiveFactor);

        // 4. 학생 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentId));

        // 학생 이름은 student.getMember().getRealName()으로 접근
        String studentName = student.getMember().getRealName();

        log.info("Personalized reward calculated - Exploration: {}, Coral: {}, Factor: {}",
                personalizedExploration, personalizedCoral, String.format("%.3f", effectiveFactor));

        return PersonalizedReward.builder()
                .studentId(studentId)
                .studentName(studentName)
                .explorationData(personalizedExploration)
                .coral(personalizedCoral)
                .effectiveFactor(effectiveFactor)
                .build();
    }

    /**
     * 학생 계수 조회 또는 생성 (Lazy Initialization)
     * PDF 2.5 참고
     *
     * @param student 학생 엔티티
     * @return 학생 계수
     */
    @Transactional
    public StudentsFactors getOrCreateStudentFactor(Student student) {
        return studentsFactorsRepository.findByStudent(student)
                .orElseGet(() -> {
                    log.info("Auto-creating student factor with global_factor=1.0 for student {}", student.getMemberId());

                    StudentsFactors newFactor = StudentsFactors.builder()
                            .student(student)
                            .globalFactor(1.0)
                            .initialized(false)
                            .build();

                    return studentsFactorsRepository.save(newFactor);
                });
    }

    /**
     * QuestDifficulty Enum을 Integer로 변환
     *
     * @param difficulty QuestDifficulty
     * @return 1-5 사이의 정수
     */
    private Integer mapDifficultyToInt(QuestDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> AIConstants.DIFFICULTY_EASY;
            case BASIC -> AIConstants.DIFFICULTY_BASIC;
            case MEDIUM -> AIConstants.DIFFICULTY_MEDIUM;
            case HARD -> AIConstants.DIFFICULTY_HARD;
            case VERY_HARD -> AIConstants.DIFFICULTY_VERY_HARD;
        };
    }
}
