package com.example.sca_be.domain.notification.controller;

import com.example.sca_be.domain.notification.dto.StudentDashboardResponse;
import com.example.sca_be.domain.notification.service.StudentDashboardService;
import com.example.sca_be.global.common.ApiResponse;
import com.example.sca_be.global.common.ApiVersion;
import com.example.sca_be.global.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.STUDENTS)
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    /**
     * 학생 대시보드 조회
     * GET /api/v1/students/dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard(
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer studentId = userDetails.getMemberId();

        StudentDashboardResponse response = studentDashboardService.getStudentDashboard(studentId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
