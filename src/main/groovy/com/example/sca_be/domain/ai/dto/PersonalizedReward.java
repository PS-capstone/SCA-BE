package com.example.sca_be.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 개인화된 보상 DTO
 * 학생별 보정계수가 적용된 보상
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedReward {

    /**
     * 학생 ID
     */
    private Integer studentId;

    /**
     * 학생 이름
     */
    private String studentName;

    /**
     * 개인화된 탐사 데이터
     */
    private Integer explorationData;

    /**
     * 개인화된 코랄
     */
    private Integer coral;

    /**
     * 적용된 최종 계수
     * = (0.6 × questFactor) + (0.4 × globalFactor)
     */
    private Double effectiveFactor;

    /**
     * 분석 근거 (AI 분석 결과)
     */
    private String reason;
}
