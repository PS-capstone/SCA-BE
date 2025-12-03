package com.example.sca_be.domain.auth.controller;

import com.example.sca_be.domain.auth.dto.*;
import com.example.sca_be.domain.auth.service.AuthService;
import com.example.sca_be.global.common.ApiResponse;
import com.example.sca_be.global.common.ApiVersion;
import com.example.sca_be.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiVersion.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. 선생님 회원가입
    @PostMapping("/teacher/signup")
    public ResponseEntity<ApiResponse<TeacherSignupResponse>> teacherSignup(
            @Valid @RequestBody TeacherSignupRequest request) {

        TeacherSignupResponse response = authService.teacherSignup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("선생님 회원가입이 완료되었습니다.", response));
    }

    // 2. 학생 회원가입
    @PostMapping("/student/signup")
    public ResponseEntity<ApiResponse<StudentSignupResponse>> studentSignup(
            @Valid @RequestBody StudentSignupRequest request) {

        StudentSignupResponse response = authService.studentSignup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("학생 회원가입이 완료되었습니다.", response));
    }

    // 3. 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(
            @Valid @RequestBody LoginRequest request) {

        Object response = authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("로그인 성공", response));
    }

    // 4. 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        TokenResponse response = authService.refreshToken(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("토큰이 갱신되었습니다.", response));
    }

    // 5. 선생님 프로필 조회
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> getTeacherProfile(
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer memberId = userDetails.getMemberId(); // 로그인 사용자 = Member ID
        
        TeacherProfileResponse response = authService.getTeacherProfile(memberId);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // 6. 선생님 프로필 수정
    @PatchMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> updateTeacherProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer memberId = userDetails.getMemberId();
        
        TeacherProfileResponse response = authService.updateTeacherProfile(memberId, request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("프로필이 수정되었습니다.", response));
    }

    // 7. 비밀번호 변경
    @PostMapping("/password/change")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer memberId = userDetails.getMemberId();
        
        authService.changePassword(memberId, request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("비밀번호가 변경되었습니다."));
    }

    // 8. 회원 탈퇴
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody AccountDeleteRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer memberId = userDetails.getMemberId();
        
        authService.deleteAccount(memberId, request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
