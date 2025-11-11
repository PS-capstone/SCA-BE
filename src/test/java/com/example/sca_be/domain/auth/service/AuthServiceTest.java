package com.example.sca_be.domain.auth.service;

import com.example.sca_be.domain.auth.dto.*;
import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.domain.auth.entity.Role;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.auth.repository.MemberRepository;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.auth.repository.TeacherRepository;
import com.example.sca_be.domain.classroom.entity.Classes;
import com.example.sca_be.domain.classroom.repository.ClassesRepository;
import com.example.sca_be.global.exception.CustomException;
import com.example.sca_be.global.exception.ErrorCode;
import com.example.sca_be.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AuthService 단위 테스트
 * - Mockito를 사용하여 Service 레이어 로직만 테스트
 * - Repository와 외부 의존성은 Mock 객체로 대체
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassesRepository classesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("선생님 회원가입 성공")
    void teacherSignup_Success() {
        // given
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("teacher1")
                .password("password123!")
                .realName("김선생")
                .nickname("김쌤")
                .email("teacher1@example.com")
                .build();

        Member savedMember = Member.builder()
                .username("teacher1")
                .password("encodedPassword")
                .realName("김선생")
                .nickname("김쌤")
                .email("teacher1@example.com")
                .role(Role.TEACHER)
                .build();

        Teacher savedTeacher = Teacher.builder()
                .member(savedMember)
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(teacherRepository.save(any(Teacher.class))).willReturn(savedTeacher);

        // when
        TeacherSignupResponse response = authService.teacherSignup(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("teacher1");
        assertThat(response.getEmail()).isEqualTo("teacher1@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_TEACHER");

        verify(memberRepository).existsByUsername("teacher1");
        verify(memberRepository).existsByEmail("teacher1@example.com");
        verify(memberRepository).existsByNickname("김쌤");
        verify(passwordEncoder).encode("password123!");
        verify(memberRepository).save(any(Member.class));
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    @DisplayName("선생님 회원가입 실패 - 중복된 아이디")
    void teacherSignup_Fail_DuplicateUsername() {
        // given
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("duplicate")
                .password("password123!")
                .realName("김선생")
                .nickname("김쌤")
                .email("teacher1@example.com")
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.teacherSignup(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_USERNAME.getMessage());

        verify(memberRepository).existsByUsername("duplicate");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("선생님 회원가입 실패 - 중복된 이메일")
    void teacherSignup_Fail_DuplicateEmail() {
        // given
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("teacher1")
                .password("password123!")
                .realName("김선생")
                .nickname("김쌤")
                .email("duplicate@example.com")
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.teacherSignup(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

        verify(memberRepository).existsByUsername("teacher1");
        verify(memberRepository).existsByEmail("duplicate@example.com");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("선생님 회원가입 실패 - 중복된 닉네임")
    void teacherSignup_Fail_DuplicateNickname() {
        // given
        TeacherSignupRequest request = TeacherSignupRequest.builder()
                .username("teacher1")
                .password("password123!")
                .realName("김선생")
                .nickname("중복닉네임")
                .email("teacher1@example.com")
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.teacherSignup(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_NICKNAME.getMessage());

        verify(memberRepository).existsByNickname("중복닉네임");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("학생 회원가입 성공")
    void studentSignup_Success() {
        // given
        StudentSignupRequest request = StudentSignupRequest.builder()
                .username("student1")
                .password("password123!")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .inviteCode("CLASS123")
                .build();

        Classes mockClass = Classes.builder()
                .className("1반")
                .inviteCode("CLASS123")
                .build();

        Member savedMember = Member.builder()
                .username("student1")
                .password("encodedPassword")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .role(Role.STUDENT)
                .build();

        Student savedStudent = Student.builder()
                .member(savedMember)
                .classes(mockClass)
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        given(classesRepository.findByInviteCode(anyString())).willReturn(Optional.of(mockClass));
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(studentRepository.save(any(Student.class))).willReturn(savedStudent);

        // when
        StudentSignupResponse response = authService.studentSignup(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("student1");
        assertThat(response.getClassName()).isEqualTo("1반");
        assertThat(response.getRole()).isEqualTo("ROLE_STUDENT");

        verify(classesRepository).findByInviteCode("CLASS123");
        verify(memberRepository).save(any(Member.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("학생 회원가입 실패 - 잘못된 초대 코드")
    void studentSignup_Fail_InvalidInviteCode() {
        // given
        StudentSignupRequest request = StudentSignupRequest.builder()
                .username("student1")
                .password("password123!")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .inviteCode("INVALID_CODE")
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        given(classesRepository.findByInviteCode(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.studentSignup(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_INVITE_CODE.getMessage());

        verify(classesRepository).findByInviteCode("INVALID_CODE");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("선생님 로그인 성공")
    void login_Teacher_Success() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("teacher1")
                .password("password123!")
                .build();

        Member member = Member.builder()
                .username("teacher1")
                .password("encodedPassword")
                .realName("김선생")
                .nickname("김쌤")
                .email("teacher1@example.com")
                .role(Role.TEACHER)
                .build();

        Teacher teacher = Teacher.builder()
                .member(member)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createToken(any(), anyString())).willReturn("mock.access.token");
        given(jwtTokenProvider.createRefreshToken(any(), anyString())).willReturn("mock.refresh.token");
        given(jwtTokenProvider.getExpirationTime()).willReturn(900L);

        // when
        Object response = authService.login(request);

        // then
        assertThat(response).isInstanceOf(TeacherLoginResponse.class);
        TeacherLoginResponse teacherResponse = (TeacherLoginResponse) response;
        assertThat(teacherResponse.getUserType()).isEqualTo("teacher");
        assertThat(teacherResponse.getUsername()).isEqualTo("teacher1");
        assertThat(teacherResponse.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(teacherResponse.getTokenType()).isEqualTo("Bearer");

        verify(memberRepository).findByUsername("teacher1");
        verify(passwordEncoder).matches("password123!", "encodedPassword");
        verify(jwtTokenProvider).createToken(any(), eq("teacher1"));
        verify(jwtTokenProvider).createRefreshToken(any(), eq("teacher1"));
    }

    @Test
    @DisplayName("학생 로그인 성공")
    void login_Student_Success() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("student1")
                .password("password123!")
                .build();

        Classes mockClass = Classes.builder()
                .className("1반")
                .inviteCode("CLASS123")
                .build();

        Member member = Member.builder()
                .username("student1")
                .password("encodedPassword")
                .realName("이학생")
                .nickname("이학")
                .email("student1@example.com")
                .role(Role.STUDENT)
                .build();

        Student student = Student.builder()
                .member(member)
                .classes(mockClass)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createToken(any(), anyString())).willReturn("mock.access.token");
        given(jwtTokenProvider.createRefreshToken(any(), anyString())).willReturn("mock.refresh.token");
        given(jwtTokenProvider.getExpirationTime()).willReturn(900L);

        // when
        Object response = authService.login(request);

        // then
        assertThat(response).isInstanceOf(StudentLoginResponse.class);
        StudentLoginResponse studentResponse = (StudentLoginResponse) response;
        assertThat(studentResponse.getUserType()).isEqualTo("student");
        assertThat(studentResponse.getUsername()).isEqualTo("student1");
        assertThat(studentResponse.getClassName()).isEqualTo("1반");
        assertThat(studentResponse.getAccessToken()).isEqualTo("mock.access.token");

        verify(memberRepository).findByUsername("student1");
        verify(passwordEncoder).matches("password123!", "encodedPassword");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("password123!")
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());

        verify(memberRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("teacher1")
                .password("wrongpassword")
                .build();

        Member member = Member.builder()
                .username("teacher1")
                .password("encodedPassword")
                .realName("김선생")
                .role(Role.TEACHER)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());

        verify(memberRepository).findByUsername("teacher1");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtTokenProvider, never()).createToken(any(), anyString());
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid.refresh.token")
                .build();

        Member member = Member.builder()
                .username("teacher1")
                .password("encodedPassword")
                .role(Role.TEACHER)
                .build();

        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn("teacher1");
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(jwtTokenProvider.createToken(any(), anyString())).willReturn("new.access.token");
        given(jwtTokenProvider.createRefreshToken(any(), anyString())).willReturn("new.refresh.token");
        given(jwtTokenProvider.getExpirationTime()).willReturn(900L);

        // when
        TokenResponse response = authService.refreshToken(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L);

        verify(jwtTokenProvider).validateToken("valid.refresh.token");
        verify(jwtTokenProvider).getUsername("valid.refresh.token");
        verify(memberRepository).findByUsername("teacher1");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refreshToken_Fail_InvalidToken() {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid.refresh.token")
                .build();

        given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("유효하지 않거나 만료된 리프레시 토큰입니다.");

        verify(jwtTokenProvider).validateToken("invalid.refresh.token");
        verify(memberRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 사용자를 찾을 수 없음")
    void refreshToken_Fail_UserNotFound() {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid.refresh.token")
                .build();

        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn("nonexistent");
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(jwtTokenProvider).validateToken("valid.refresh.token");
        verify(memberRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        LogoutRequest request = LogoutRequest.builder()
                .refreshToken("valid.refresh.token")
                .build();

        // when
        authService.logout(request);

        // then
        // 현재 로그아웃은 별도 로직이 없으므로 예외가 발생하지 않으면 성공
        verifyNoInteractions(memberRepository, jwtTokenProvider);
    }
}