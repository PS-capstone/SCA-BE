package com.example.sca_be.domain.ai.controller;

import com.example.sca_be.domain.ai.config.AIConstants;
import com.example.sca_be.domain.ai.dto.InitializeFactorRequest;
import com.example.sca_be.domain.ai.dto.InitializeFactorResponse;
import com.example.sca_be.domain.ai.service.StudentFactorService;
import com.example.sca_be.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 학생 계수 관리 컨트롤러
 * - 학생별 보정계수 초기화 API 제공
 */
@Slf4j
@RestController
// [수정 1] URL 경로에 {classId}를 포함시켜야 @PathVariable이 인식할 수 있습니다.
@RequestMapping("/api/v1/classes/{classId}/students")
@RequiredArgsConstructor
public class StudentFactorController {

    private final StudentFactorService studentFactorService;

    /**
     * 학생 계수 초기화
     * PDF 2.2 참고
     *
     * POST /api/v1/classes/{classId}/students/initialize-factors
     */
    @PutMapping("")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<InitializeFactorResponse>> initializeFactors(
            // [수정 2] ("classId")를 명시하여 컴파일러 파라미터 이름 오류 해결
            @PathVariable("classId") Integer classId,
            @RequestBody InitializeFactorRequest request) {

        log.info("Factor initialization requested for {} students in class {}",
                request.getStudentScores().size(), classId);

        // 각 학생별 계수 초기화
        List<InitializeFactorResponse.StudentFactorInfo> studentFactorInfos = request.getStudentScores().stream()
                .map(studentScore -> {
                    // 계수 초기화
                    studentFactorService.initializeFactor(
                            studentScore.getStudentId(),
                            studentScore.getInitialScore()
                    );

                    // 계산된 계수 값
                    double initialFactor = 1.0 + (AIConstants.BASELINE_SCORE - studentScore.getInitialScore()) / 100.0;
                    double limitedFactor = Math.max(
                            AIConstants.INITIAL_FACTOR_MIN,
                            Math.min(AIConstants.INITIAL_FACTOR_MAX, initialFactor)
                    );

                    return InitializeFactorResponse.StudentFactorInfo.builder()
                            .studentId(studentScore.getStudentId())
                            .initialScore(studentScore.getInitialScore())
                            .globalFactor(limitedFactor)
                            .build();
                })
                .collect(Collectors.toList());

        InitializeFactorResponse response = InitializeFactorResponse.builder()
                .initializedCount(studentFactorInfos.size())
                .students(studentFactorInfos)
                .build();

        log.info("Factor initialization completed for {} students", response.getInitializedCount());

        return ResponseEntity.ok(
                ApiResponse.success(response, "학생 계수 초기화가 완료되었습니다.")
        );
    }
}