package com.example.sca_be.domain.auth.integration;

import com.example.sca_be.domain.auth.dto.*;
import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.domain.auth.entity.Role;
import com.example.sca_be.domain.auth.repository.MemberRepository;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.auth.repository.TeacherRepository;
import com.example.sca_be.domain.classroom.entity.Classes;
import com.example.sca_be.domain.classroom.repository.ClassesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth 통합 테스트
 * - 실제 Spring 컨텍스트와 DB를 사용한 E2E 테스트
 * - H2 인메모리 DB 사용
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Classes testClass;

    @BeforeEach
    void setUp() {
        // 테스트용 반 생성
        testClass = Classes.builder()
                .className("테스트반")
                .inviteCode("TEST123")
                .build();
        classesRepository.save(testClass);
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후 데이터 정리
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        memberRepository.deleteAll();
        classesRepository.deleteAll();
    }

    @Test
    @DisplayName("선생님 회원가입 -> 로그인 -> 토큰 갱신 전체 플로우 테스트")
    void teacherFullAuthFlow_Success() throws Exception {
        // 1. 선생님 회원가입
        TeacherSignupRequest signupRequest = TeacherSignupRequest.builder()
                .username("teacher_integration")
                .password("password123!")
                .realName("통합테스트선생")
                .nickname("통합쌤")
                .email("teacher_integration@test.com")
                .build();

        mockMvc.perform(post("/api/auth/teacher/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("teacher_integration"))
                .andExpect(jsonPath("$.data.role").value("ROLE_TEACHER"));

        // 2. 로그인
        LoginRequest loginRequest = LoginRequest.builder()
                .username("teacher_integration")
                .password("password123!")
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userType").value("teacher"))
                .andExpect(jsonPath("$.data.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 3. 응답에서 refreshToken 추출
        TeacherLoginResponse loginResponseObj = objectMapper.readValue(
                objectMapper.readTree(loginResponse).get("data").toString(),
                TeacherLoginResponse.class
        );

        // 4. 토큰 갱신
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(loginResponseObj.getRefreshToken())
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken").value(notNullValue()));

        // 5. 로그아웃
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .refreshToken(loginResponseObj.getRefreshToken())
                .build();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("학생 회원가입 -> 로그인 전체 플로우 테스트")
    void studentFullAuthFlow_Success() throws Exception {
        // 1. 학생 회원가입
        StudentSignupRequest signupRequest = StudentSignupRequest.builder()
                .username("student_integration")
                .password("password123!")
                .realName("통합테스트학생")
                .nickname("통합학생")
                .email("student_integration@test.com")
                .inviteCode("TEST123")
                .build();

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("student_integration"))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.data.className").value("테스트반"));

        // 2. 로그인
        LoginRequest loginRequest = LoginRequest.builder()
                .username("student_integration")
                .password("password123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userType").value("student"))
                .andExpect(jsonPath("$.data.className").value("테스트반"))
                .andExpect(jsonPath("$.data.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken").value(notNullValue()));
    }

    @Test
    @DisplayName("중복된 아이디로 회원가입 시 실패")
    void signup_WithDuplicateUsername_Fail() throws Exception {
        // given - 기존 회원 생성
        Member existingMember = Member.builder()
                .username("duplicate_user")
                .password(passwordEncoder.encode("password"))
                .realName("기존회원")
                .nickname("기존닉")
                .email("existing@test.com")
                .role(Role.TEACHER)
                .build();
        memberRepository.save(existingMember);

        // when & then - 동일한 아이디로 회원가입 시도
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("duplicate_user")
                .password("password123!")
                .realName("새회원")
                .nickname("새닉")
                .email("new@test.com")
                .build();

        mockMvc.perform(post("/api/auth/teacher/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("잘못된 초대 코드로 학생 회원가입 시 실패")
    void studentSignup_WithInvalidInviteCode_Fail() throws Exception {
        // given
        StudentSignupRequest request = StudentSignupRequest.builder()
                .username("student_fail")
                .password("password123!")
                .realName("실패학생")
                .nickname("실패학생")
                .email("fail@test.com")
                .inviteCode("INVALID_CODE")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 실패")
    void login_WithWrongPassword_Fail() throws Exception {
        // given - 기존 회원 생성
        Member member = Member.builder()
                .username("test_user")
                .password(passwordEncoder.encode("correct_password"))
                .realName("테스트회원")
                .nickname("테스트닉")
                .email("test@test.com")
                .role(Role.TEACHER)
                .build();
        memberRepository.save(member);

        // when & then - 잘못된 비밀번호로 로그인
        LoginRequest request = LoginRequest.builder()
                .username("test_user")
                .password("wrong_password")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 로그인 시 실패")
    void login_WithNonExistentUser_Fail() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent_user")
                .password("password123!")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 리프레시 토큰으로 토큰 갱신 시 실패")
    void refreshToken_WithInvalidToken_Fail() throws Exception {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid.refresh.token")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("비밀번호가 올바르게 암호화되어 저장되는지 검증")
    void signup_PasswordEncrypted() throws Exception {
        // given
        String plainPassword = "password123!";
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("password_test")
                .password(plainPassword)
                .realName("암호화테스트")
                .nickname("암호화")
                .email("encrypt@test.com")
                .build();

        // when
        mockMvc.perform(post("/api/auth/teacher/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // then - DB에서 조회하여 비밀번호가 암호화되었는지 확인
        Member savedMember = memberRepository.findByUsername("password_test").orElseThrow();
        assertThat(savedMember.getPassword()).isNotEqualTo(plainPassword);
        assertThat(passwordEncoder.matches(plainPassword, savedMember.getPassword())).isTrue();
    }

    private static org.assertj.core.api.AbstractBooleanAssert<?> assertThat(boolean condition) {
        return org.assertj.core.api.Assertions.assertThat(condition);
    }

    private static org.assertj.core.api.AbstractStringAssert<?> assertThat(String actual) {
        return org.assertj.core.api.Assertions.assertThat(actual);
    }
}