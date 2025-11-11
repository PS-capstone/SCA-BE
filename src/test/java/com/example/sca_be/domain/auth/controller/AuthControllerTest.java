package com.example.sca_be.domain.auth.controller;

import com.example.sca_be.domain.auth.dto.*;
import com.example.sca_be.domain.auth.service.AuthService;
import com.example.sca_be.global.exception.CustomException;
import com.example.sca_be.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 단위 테스트
 * - MockMvc를 사용하여 Controller 레이어만 테스트
 * - Service 레이어는 Mock 객체로 대체
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("선생님 회원가입 성공")
    void teacherSignup_Success() throws Exception {
        // given
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("teacher1")
                .password("password123!")
                .realName("김선생")
                .nickname("김쌤")
                .email("teacher1@example.com")
                .build();

        TeacherSignupResponse response = TeacherSignupResponse.builder()
                .teacherId(1)
                .username("teacher1")
                .email("teacher1@example.com")
                .realName("김선생")
                .nickname("김쌤")
                .role("ROLE_TEACHER")
                .createdAt(LocalDateTime.now())
                .build();

        given(authService.teacherSignup(any(TeacherSignupRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/teacher/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("선생님 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.username").value("teacher1"))
                .andExpect(jsonPath("$.data.email").value("teacher1@example.com"))
                .andExpect(jsonPath("$.data.role").value("ROLE_TEACHER"));

        verify(authService).teacherSignup(any(TeacherSignupRequest.class));
    }

    @Test
    @DisplayName("선생님 회원가입 실패 - 중복된 아이디")
    void teacherSignup_Fail_DuplicateUsername() throws Exception {
        // given
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("duplicate")
                .password("password123!")
                .realName("김선생")
                .nickname("김쌤")
                .email("teacher1@example.com")
                .build();

        given(authService.teacherSignup(any(TeacherSignupRequest.class)))
                .willThrow(new CustomException(ErrorCode.DUPLICATE_USERNAME));

        // when & then
        mockMvc.perform(post("/api/auth/teacher/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(authService).teacherSignup(any(TeacherSignupRequest.class));
    }

    @Test
    @DisplayName("학생 회원가입 성공")
    void studentSignup_Success() throws Exception {
        // given
        StudentSignupRequest request = StudentSignupRequest.builder()
                .username("student1")
                .password("password123!")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .inviteCode("CLASS123")
                .build();

        StudentSignupResponse response = StudentSignupResponse.builder()
                .studentId(1)
                .username("student1")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .classId(1)
                .className("1반")
                .role("ROLE_STUDENT")
                .coral(0)
                .researchData(0)
                .createdAt(LocalDateTime.now())
                .build();

        given(authService.studentSignup(any(StudentSignupRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("학생 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.data.className").value("1반"));

        verify(authService).studentSignup(any(StudentSignupRequest.class));
    }

    @Test
    @DisplayName("학생 회원가입 실패 - 잘못된 초대 코드")
    void studentSignup_Fail_InvalidInviteCode() throws Exception {
        // given
        StudentSignupRequest request = StudentSignupRequest.builder()
                .username("student1")
                .password("password123!")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .inviteCode("INVALID_CODE")
                .build();

        given(authService.studentSignup(any(StudentSignupRequest.class)))
                .willThrow(new CustomException(ErrorCode.INVALID_INVITE_CODE));

        // when & then
        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(authService).studentSignup(any(StudentSignupRequest.class));
    }

    @Test
    @DisplayName("선생님 로그인 성공")
    void login_Teacher_Success() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("teacher1")
                .password("password123!")
                .build();

        TeacherLoginResponse response = TeacherLoginResponse.builder()
                .userType("teacher")
                .teacherId(1)
                .username("teacher1")
                .email("teacher1@example.com")
                .realName("김선생")
                .nickname("김쌤")
                .role("ROLE_TEACHER")
                .accessToken("mock.access.token")
                .refreshToken("mock.refresh.token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        given(authService.login(any(LoginRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.userType").value("teacher"))
                .andExpect(jsonPath("$.data.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("학생 로그인 성공")
    void login_Student_Success() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("student1")
                .password("password123!")
                .build();

        StudentLoginResponse response = StudentLoginResponse.builder()
                .userType("student")
                .studentId(1)
                .username("student1")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .classId(1)
                .className("1반")
                .coral(100)
                .researchData(50)
                .role("ROLE_STUDENT")
                .accessToken("mock.access.token")
                .refreshToken("mock.refresh.token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        given(authService.login(any(LoginRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userType").value("student"))
                .andExpect(jsonPath("$.data.className").value("1반"))
                .andExpect(jsonPath("$.data.coral").value(100));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 인증 정보")
    void login_Fail_InvalidCredentials() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("wronguser")
                .password("wrongpass")
                .build();

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() throws Exception {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid.refresh.token")
                .build();

        TokenResponse response = TokenResponse.builder()
                .accessToken("new.access.token")
                .refreshToken("new.refresh.token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        given(authService.refreshToken(any(RefreshTokenRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료된 리프레시 토큰")
    void refreshToken_Fail_ExpiredToken() throws Exception {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("expired.refresh.token")
                .build();

        given(authService.refreshToken(any(RefreshTokenRequest.class)))
                .willThrow(new CustomException(ErrorCode.TOKEN_EXPIRED));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // given
        LogoutRequest request = LogoutRequest.builder()
                .refreshToken("valid.refresh.token")
                .build();

        doNothing().when(authService).logout(any(LogoutRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));

        verify(authService).logout(any(LogoutRequest.class));
    }
}