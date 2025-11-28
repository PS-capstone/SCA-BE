package com.example.sca_be.domain.ai.dto;

import com.example.sca_be.domain.ai.entity.QuestDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 퀘스트 분석 결과 DTO
 * OpenAI API로부터 받은 분석 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestAnalysisResult {

    /**
     * 인지 과정 점수 (1-6)
     * 블룸 분류 기준: 기억(1) ~ 창안(6)
     */
    private Integer cognitiveProcessScore;

    /**
     * 예상 노력 점수 (1-10)
     * 예상 소요 시간 기반
     */
    private Integer effortScore;

    /**
     * AI가 판단한 난이도
     */
    private QuestDifficulty difficulty;

    /**
     * 분석 근거 설명
     */
    private String analysisReason;
}
