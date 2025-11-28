package com.example.sca_be.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 기본 보상 DTO
 * 모든 학생에게 공통으로 적용되는 기본 보상
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseReward {

    /**
     * 탐사 데이터 (exploration_data)
     * 공식: (cognitive² × 5) + (effort × 2)
     */
    private Integer explorationData;

    /**
     * 코랄 (coral)
     * 공식: (effort × 5) + (cognitive × 2)
     */
    private Integer coral;
}
