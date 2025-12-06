package com.example.sca_be.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학습 이벤트 DTO
 * 선생님의 피드백(AI 추천 수정)을 기반으로 학습 엔진에 전달되는 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningEvent {

    /**
     * 학생 ID
     */
    private Integer studentId;

    /**
     * 퀘스트 할당 ID
     */
    private Integer assignmentId;

    /**
     * 난이도 (1-5)
     */
    private Integer difficulty;

    /**
     * 인지 과정 점수
     */
    private Integer cognitiveScore;

    /**
     * 예상 노력 점수
     */
    private Integer effortScore;

    /**
     * AI가 추천한 코랄
     */
    private Integer aiCoral;

    /**
     * AI가 추천한 탐사 데이터
     */
    private Integer aiResearchData;

    /**
     * 선생님이 최종 확정한 코랄
     */
    private Integer teacherCoral;

    /**
     * 선생님이 최종 확정한 탐사 데이터
     */
    private Integer teacherResearchData;

    /**
     * 학생 전역 계수
     */
    private Double globalFactor;

    /**
     * 해당 난이도에 대한 학생 계수
     */
    private Double difficultyFactor;
}
