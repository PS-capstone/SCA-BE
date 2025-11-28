package com.example.sca_be.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 학생 계수 초기화 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializeFactorResponse {

    @JsonProperty("initialized_count")
    private Integer initializedCount;

    private List<StudentFactorInfo> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentFactorInfo {
        @JsonProperty("student_id")
        private Integer studentId;

        @JsonProperty("initial_score")
        private Integer initialScore;

        @JsonProperty("global_factor")
        private Double globalFactor;
    }
}
