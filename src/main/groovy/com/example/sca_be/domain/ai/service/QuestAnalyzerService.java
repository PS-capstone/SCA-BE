package com.example.sca_be.domain.ai.service;

import com.example.sca_be.domain.ai.dto.QuestAnalysisResult;
import com.example.sca_be.domain.ai.entity.QuestDifficulty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 퀘스트 분석 서비스
 * 파이썬 quest_analyzer.py를 Spring AI로 이식
 *
 * 역할: OpenAI GPT-4o-mini를 사용하여 퀘스트의 난이도를 분석
 * 설정: application.yaml에서 model, temperature, top-p 설정 사용
 */
@Slf4j
@Service
public class QuestAnalyzerService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String promptTemplate;

    public QuestAnalyzerService(
            ChatClient chatClient,
            ObjectMapper objectMapper,
            @Value("classpath:ai/prompts/quest-analysis.txt") Resource promptResource
    ) throws IOException {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;

        // 프롬프트 템플릿 로드
        this.promptTemplate = promptResource.getContentAsString(StandardCharsets.UTF_8);

        log.info("QuestAnalyzerService initialized (using application.yaml OpenAI settings)");
    }

    /**
     * 퀘스트 분석 메인 메서드
     *
     * @param questText 퀘스트 내용 (제목 + 내용)
     * @param difficulty 선생님이 지정한 난이도 (참고용)
     * @return 분석 결과 (인지 점수, 노력 점수, AI 판단 난이도, 분석 이유)
     */
    public QuestAnalysisResult analyzeQuest(String questText, QuestDifficulty difficulty) {
        log.info("Analyzing quest with difficulty: {}", difficulty);

        try {
            // 프롬프트 생성 (템플릿에 값 주입)
            String prompt = promptTemplate
                    .replace("{difficulty}", difficulty.name())
                    .replace("{questContent}", questText);

            // OpenAI API 호출
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("OpenAI Response: {}", response);

            // JSON 파싱
            QuestAnalysisResult result = parseResponse(response);

            log.info("Quest analysis completed - Cognitive: {}, Effort: {}, Difficulty: {}",
                    result.getCognitiveProcessScore(),
                    result.getEffortScore(),
                    result.getDifficulty());

            return result;

        } catch (Exception e) {
            log.error("Failed to analyze quest", e);
            throw new RuntimeException("퀘스트 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * OpenAI 응답 JSON 파싱
     *
     * @param jsonResponse OpenAI API 응답 (JSON 문자열)
     * @return QuestAnalysisResult 객체
     */
    private QuestAnalysisResult parseResponse(String jsonResponse) throws JsonProcessingException {
        // JSON 정리 (코드 블록 마크다운 제거)
        String cleanedJson = jsonResponse
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // snake_case -> camelCase 매핑을 위한 임시 DTO
        QuestAnalysisRawResponse rawResponse = objectMapper.readValue(cleanedJson, QuestAnalysisRawResponse.class);

        // QuestAnalysisResult로 변환
        return QuestAnalysisResult.builder()
                .cognitiveProcessScore(rawResponse.cognitive_process_score)
                .effortScore(rawResponse.effort_score)
                .difficulty(QuestDifficulty.valueOf(rawResponse.difficulty))
                .analysisReason(rawResponse.analysis_reason)
                .build();
    }

    /**
     * OpenAI 응답 파싱용 내부 DTO (snake_case)
     */
    private static class QuestAnalysisRawResponse {
        public Integer cognitive_process_score;
        public Integer effort_score;
        public String difficulty;
        public String analysis_reason;
    }
}
