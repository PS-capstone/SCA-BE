package com.example.sca_be.domain.ai.service;

import com.example.sca_be.domain.ai.config.AIConstants;
import com.example.sca_be.domain.ai.dto.LearningEvent;
import com.example.sca_be.domain.ai.dto.LearningResult;
import com.example.sca_be.domain.ai.entity.AiLearningLogs;
import com.example.sca_be.domain.ai.entity.StudentsFactors;
import com.example.sca_be.domain.ai.entity.StudentsQuestFactors;
import com.example.sca_be.domain.ai.repository.AiLearningLogsRepository;
import com.example.sca_be.domain.ai.repository.StudentsFactorsRepository;
import com.example.sca_be.domain.ai.repository.StudentsQuestFactorsRepository;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.personalquest.entity.QuestAssignment;
import com.example.sca_be.domain.personalquest.repository.QuestAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 학습 엔진 서비스
 * 파이썬 learning_engine.py를 Spring으로 이식
 *
 * 역할:
 * - 선생님의 피드백(AI 추천 수정)을 기반으로 학생별 보정계수 학습
 * - EMA(지수 이동 평균) 방식으로 계수 업데이트
 * - 학습 로그 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningEngineService {

    private final StudentsFactorsRepository studentsFactorsRepository;
    private final StudentsQuestFactorsRepository studentsQuestFactorsRepository;
    private final AiLearningLogsRepository aiLearningLogsRepository;
    private final StudentRepository studentRepository;
    private final QuestAssignmentRepository questAssignmentRepository;
    private final StudentFactorService studentFactorService;

    /**
     * 1. 학습 실행 (비동기)
     * PDF 3.3 전체 흐름 참고
     *
     * @param event 학습 이벤트 (선생님의 피드백)
     */
    @Async("aiLearningExecutor")
    @Retryable(maxAttempts = 3)
    @Transactional
    public void learnAsync(LearningEvent event) {
        log.info("Starting async learning for student {}, assignment {}",
                event.getStudentId(), event.getAssignmentId());

        try {
            // 1. 수정 여부 확인
            if (!isModified(event)) {
                log.info("No modification detected, skipping learning");
                return;
            }

            // 2. 수정률 계산
            Double modificationRate = calculateModificationRate(
                    event.getAiCoral(), event.getAiResearchData(),
                    event.getTeacherCoral(), event.getTeacherResearchData()
            );

            // 3. 학습률 결정
            Double learningRate = getLearningRate(modificationRate, event.getStudentId());

            // 4. 계수 업데이트 (EMA)
            LearningResult result = updateFactors(event, modificationRate, learningRate);

            // 5. 학습 로그 저장
            saveLearningLog(event, result);

            log.info("Learning completed successfully - Student: {}, Modification Rate: {}, Learning Rate: {}",
                    event.getStudentId(),
                    String.format("%.3f", modificationRate),
                    String.format("%.3f", learningRate));

        } catch (Exception e) {
            log.error("Learning failed for student {}, assignment {}",
                    event.getStudentId(), event.getAssignmentId(), e);
            throw new RuntimeException("학습 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 2. 수정률 계산
     * PDF 3.3 Step 2 참고
     *
     * coralRatio = teacherCoral / aiCoral
     * researchRatio = teacherResearch / aiResearch
     * actualRatio = coralRatio × 0.5 + researchRatio × 0.5
     * modificationRate = |actualRatio - 1.0|
     *
     * @param aiCoral AI 추천 코랄
     * @param aiResearch AI 추천 탐사 데이터
     * @param teacherCoral 선생님 최종 코랄
     * @param teacherResearch 선생님 최종 탐사 데이터
     * @return 수정률 (절댓값)
     */
    private Double calculateModificationRate(
            Integer aiCoral, Integer aiResearch,
            Integer teacherCoral, Integer teacherResearch) {

        // 0으로 나누기 방지
        double safeAiCoral = aiCoral == 0 ? 1.0 : aiCoral;
        double safeAiResearch = aiResearch == 0 ? 1.0 : aiResearch;

        // 비율 계산
        double coralRatio = teacherCoral / safeAiCoral;
        double researchRatio = teacherResearch / safeAiResearch;

        // 가중 평균 (50:50)
        double actualRatio = coralRatio * AIConstants.CORAL_REWARD_WEIGHT
                + researchRatio * AIConstants.EXPLORATION_REWARD_WEIGHT;

        // 수정률 = |실제 비율 - 1.0|
        double modificationRate = Math.abs(actualRatio - 1.0);

        log.debug("Modification rate calculated: {} (coral: {}, research: {})",
                String.format("%.3f", modificationRate),
                String.format("%.3f", coralRatio),
                String.format("%.3f", researchRatio));

        return modificationRate;
    }

    /**
     * 3. 학습률 결정
     * PDF 3.3 Step 4 참고
     *
     * - modificationRate > 0.20 → α = 0.3 (OVERRIDE)
     * - 0.05 < modificationRate ≤ 0.20 → α = 0.1 (FINE_TUNE)
     * - modificationRate ≤ 0.05 → α = 0.05 (MINOR)
     * - 학기 초 3주 이내: α × 0.5
     *
     * @param modificationRate 수정률
     * @param studentId 학생 ID
     * @return 학습률 (alpha)
     */
    private Double getLearningRate(Double modificationRate, Integer studentId) {
        // 기본 학습률 결정
        double learningRate;
        if (modificationRate > AIConstants.OVERRIDE_THRESHOLD) {
            learningRate = AIConstants.LEARNING_RATE_OVERRIDE;
        } else if (modificationRate > AIConstants.FINE_TUNE_THRESHOLD) {
            learningRate = AIConstants.LEARNING_RATE_FINE_TUNE;
        } else {
            learningRate = AIConstants.LEARNING_RATE_MINOR;
        }

        // 학기 초 보정 (3주 이내)
        if (isColdStartPeriod(studentId)) {
            learningRate *= 0.5;
            log.debug("Cold start period detected, learning rate halved: {}",
                    String.format("%.3f", learningRate));
        }

        return learningRate;
    }

    /**
     * 시그모이드 변환을 사용한 actualRatio 정규화
     * actualRatio를 FACTOR_MIN ~ FACTOR_MAX 범위로 부드럽게 매핑
     *
     * - actualRatio = 1.0 → 1.0 (중립)
     * - actualRatio > 1.0 → FACTOR_MAX (1.2)에 근접
     * - actualRatio < 1.0 → FACTOR_MIN (0.8)에 근접
     *
     * @param actualRatio 실제 비율 (teacherReward / aiReward)
     * @return 변환된 계수 값 (0.8 ~ 1.2 범위)
     */
    private double transformActualRatio(double actualRatio) {
        // 1.0으로부터의 차이 계산
        double delta = actualRatio - 1.0;

        // tanh를 사용한 부드러운 변환 (출력 범위: -1 ~ 1)
        // 스케일링 계수 2.0으로 민감도 조절
        double normalized = Math.tanh(delta * 2.0);

        // FACTOR_MIN ~ FACTOR_MAX 범위로 매핑
        double range = AIConstants.FACTOR_MAX - AIConstants.FACTOR_MIN;  // 0.4
        double midpoint = (AIConstants.FACTOR_MAX + AIConstants.FACTOR_MIN) / 2.0;  // 1.0

        // midpoint(1.0) + normalized * (range/2) = 1.0 + normalized * 0.2
        return midpoint + normalized * (range / 2.0);
    }

    /**
     * 4. 계수 업데이트 (EMA)
     * PDF 3.3 Step 5 참고
     *
     * transformedRatio = sigmoid(actualRatio)
     * newGlobal = α × transformedRatio + (1-α) × oldGlobal
     * newQuest = α × transformedRatio + (1-α) × oldQuest
     * 범위: 0.8 ~ 1.2 (sigmoid 변환으로 자동 보장)
     *
     * @param event 학습 이벤트
     * @param modificationRate 수정률
     * @param learningRate 학습률
     * @return 학습 결과
     */
    private LearningResult updateFactors(
            LearningEvent event,
            Double modificationRate,
            Double learningRate) {

        // 학생 조회
        Student student = studentRepository.findById(event.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + event.getStudentId()));

        // 학생 계수 조회 또는 생성
        StudentsFactors studentFactor = studentFactorService.getOrCreateStudentFactor(student);

        // 기존 계수
        double oldGlobalFactor = studentFactor.getGlobalFactor();

        // 난이도별 계수 조회
        StudentsQuestFactors questFactor = studentsQuestFactorsRepository
                .findByStudentFactorAndDifficulty(studentFactor, event.getDifficulty())
                .orElse(null);

        double oldQuestFactor = questFactor != null ? questFactor.getFactorValue() : oldGlobalFactor;

        // actualRatio 계산
        double safeAiCoral = event.getAiCoral() == 0 ? 1.0 : event.getAiCoral();
        double safeAiResearch = event.getAiResearchData() == 0 ? 1.0 : event.getAiResearchData();

        double coralRatio = event.getTeacherCoral() / safeAiCoral;
        double researchRatio = event.getTeacherResearchData() / safeAiResearch;

        double actualRatio = coralRatio * AIConstants.CORAL_REWARD_WEIGHT
                + researchRatio * AIConstants.EXPLORATION_REWARD_WEIGHT;

        // 시그모이드 변환 적용
        double transformedRatio = transformActualRatio(actualRatio);

        // EMA 적용 (변환된 비율 사용)
        double newGlobalFactor = learningRate * transformedRatio + (1 - learningRate) * oldGlobalFactor;
        double newQuestFactor = learningRate * transformedRatio + (1 - learningRate) * oldQuestFactor;

        // 시그모이드 변환으로 이미 범위가 보장되지만, 안전을 위한 최종 클램핑
        newGlobalFactor = Math.max(AIConstants.FACTOR_MIN, Math.min(AIConstants.FACTOR_MAX, newGlobalFactor));
        newQuestFactor = Math.max(AIConstants.FACTOR_MIN, Math.min(AIConstants.FACTOR_MAX, newQuestFactor));

        // DB 업데이트
        studentFactor.updateGlobalFactor(newGlobalFactor);
        studentFactor.incrementLearningCount();

        if (questFactor == null) {
            // 새로 생성
            questFactor = StudentsQuestFactors.builder()
                    .studentFactor(studentFactor)
                    .difficulty(event.getDifficulty())
                    .factorValue(newQuestFactor)
                    .build();
            studentsQuestFactorsRepository.save(questFactor);
        } else {
            // 업데이트
            questFactor.updateFactorValue(newQuestFactor);
            questFactor.incrementLearningCount();
        }

        // 수정 유형 결정
        String modificationType;
        if (modificationRate > AIConstants.OVERRIDE_THRESHOLD) {
            modificationType = "OVERRIDE";
        } else if (modificationRate > AIConstants.FINE_TUNE_THRESHOLD) {
            modificationType = "FINE_TUNE";
        } else {
            modificationType = "MINOR";
        }

        log.info("Factors updated - Global: {} -> {}, Quest: {} -> {}",
                String.format("%.3f", oldGlobalFactor),
                String.format("%.3f", newGlobalFactor),
                String.format("%.3f", oldQuestFactor),
                String.format("%.3f", newQuestFactor));

        return LearningResult.builder()
                .newGlobalFactor(newGlobalFactor)
                .newQuestFactor(newQuestFactor)
                .actualRatio(actualRatio)
                .modificationRate(modificationRate)
                .modificationType(modificationType)
                .learningRate(learningRate)
                .explorationRatio(researchRatio)
                .coralRatio(coralRatio)
                .explanation(String.format(
                        "Type: %s (LR: %.2f), Ratio: %.2f (E:%.1f/C:%.1f). Global: %.3f -> %.3f, Quest: %.3f -> %.3f",
                        modificationType, learningRate, actualRatio,
                        AIConstants.EXPLORATION_REWARD_WEIGHT, AIConstants.CORAL_REWARD_WEIGHT,
                        oldGlobalFactor, newGlobalFactor, oldQuestFactor, newQuestFactor
                ))
                .build();
    }

    /**
     * 5. 학습 로그 저장
     *
     * @param event 학습 이벤트
     * @param result 학습 결과
     */
    private void saveLearningLog(LearningEvent event, LearningResult result) {
        // 학생 조회
        Student student = studentRepository.findById(event.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + event.getStudentId()));

        // 할당 조회
        QuestAssignment assignment = questAssignmentRepository.findById(event.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 할당을 찾을 수 없습니다: " + event.getAssignmentId()));

        // 학습 로그 생성
        AiLearningLogs learningLog = AiLearningLogs.builder()
                .questAssignment(assignment)
                .student(student)
                .difficulty(event.getDifficulty())
                .cognitiveScore(event.getCognitiveScore())
                .effortScore(event.getEffortScore())
                .aiCoral(event.getAiCoral())
                .aiResearchData(event.getAiResearchData())
                .teacherCoral(event.getTeacherCoral())
                .teacherResearchData(event.getTeacherResearchData())
                .build();

        learningLog.markAsLearned();
        aiLearningLogsRepository.save(learningLog);

        log.info("Learning log saved - Assignment: {}, Student: {}",
                event.getAssignmentId(), event.getStudentId());
    }

    /**
     * 수정 여부 확인
     *
     * @param event 학습 이벤트
     * @return true if modified
     */
    private boolean isModified(LearningEvent event) {
        return !event.getAiCoral().equals(event.getTeacherCoral())
                || !event.getAiResearchData().equals(event.getTeacherResearchData());
    }

    /**
     * 학기 초 기간 확인 (3주 이내)
     *
     * @param studentId 학생 ID
     * @return true if within cold start period
     */
    private boolean isColdStartPeriod(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentId));

        StudentsFactors studentFactor = studentsFactorsRepository.findByStudent(student)
                .orElse(null);

        if (studentFactor == null || studentFactor.getCreatedAt() == null) {
            return false;
        }

        long weeksSinceCreation = ChronoUnit.WEEKS.between(
                studentFactor.getCreatedAt(),
                LocalDateTime.now()
        );

        return weeksSinceCreation < AIConstants.COLD_START_WEEKS;
    }
}
