package com.example.sca_be.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학습 결과 DTO
 * 학습 엔진 실행 후 반환되는 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningResult {

    /**
     * 업데이트된 전역 계수
     */
    private Double newGlobalFactor;

    /**
     * 업데이트된 퀘스트별 계수
     */
    private Double newQuestFactor;

    /**
     * 실제 비율 (actualRatio)
     */
    private Double actualRatio;

    /**
     * 수정률 (modificationRate)
     */
    private Double modificationRate;

    /**
     * 수정 유형 (OVERRIDE, FINE_TUNE, MINOR)
     */
    private String modificationType;

    /**
     * 적용된 학습률 (alpha)
     */
    private Double learningRate;

    /**
     * 탐사 데이터 비율
     */
    private Double explorationRatio;

    /**
     * 코랄 비율
     */
    private Double coralRatio;

    /**
     * 설명
     */
    private String explanation;
}
