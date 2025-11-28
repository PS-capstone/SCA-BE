package com.example.sca_be.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 학생 계수 초기화 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitializeFactorRequest {

    @JsonProperty("student_scores")
    private List<StudentScore> studentScores;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentScore {
        @JsonProperty("student_id")
        private Integer studentId;

        @JsonProperty("initial_score")
        private Integer initialScore;
    }
}
