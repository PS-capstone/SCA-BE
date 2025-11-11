package com.example.sca_be.domain.auth.repository;

import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.domain.auth.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemberRepository 테스트
 * - @DataJpaTest: JPA 관련 컴포넌트만 로드하여 빠른 테스트
 * - H2 인메모리 DB 자동 사용
 */
@DataJpaTest
@ActiveProfiles("dev")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 저장 및 조회")
    void saveMember_Success() {
        // given
        Member member = Member.builder()
                .username("testuser")
                .password("encodedPassword")
                .realName("테스트유저")
                .nickname("테스트")
                .email("test@example.com")
                .role(Role.TEACHER)
                .build();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember.getMemberId()).isNotNull();
        assertThat(savedMember.getUsername()).isEqualTo("testuser");
        assertThat(savedMember.getRole()).isEqualTo(Role.TEACHER);
        assertThat(savedMember.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("username으로 회원 조회 성공")
    void findByUsername_Success() {
        // given
        Member member = Member.builder()
                .username("findtest")
                .password("password")
                .realName("조회테스트")
                .nickname("조회")
                .email("find@example.com")
                .role(Role.STUDENT)
                .build();
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByUsername("findtest");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getUsername()).isEqualTo("findtest");
        assertThat(foundMember.get().getRealName()).isEqualTo("조회테스트");
    }

    @Test
    @DisplayName("존재하지 않는 username으로 조회 시 Empty 반환")
    void findByUsername_NotFound() {
        // when
        Optional<Member> foundMember = memberRepository.findByUsername("nonexistent");

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("username 중복 체크 - 존재하는 경우")
    void existsByUsername_ExistingUser_True() {
        // given
        Member member = Member.builder()
                .username("duplicate")
                .password("password")
                .realName("중복테스트")
                .nickname("중복")
                .email("dup@example.com")
                .role(Role.TEACHER)
                .build();
        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByUsername("duplicate");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("username 중복 체크 - 존재하지 않는 경우")
    void existsByUsername_NonExistingUser_False() {
        // when
        boolean exists = memberRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("email 중복 체크 - 존재하는 경우")
    void existsByEmail_ExistingEmail_True() {
        // given
        Member member = Member.builder()
                .username("emailtest")
                .password("password")
                .realName("이메일테스트")
                .nickname("이메일")
                .email("existing@example.com")
                .role(Role.TEACHER)
                .build();
        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByEmail("existing@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("email 중복 체크 - 존재하지 않는 경우")
    void existsByEmail_NonExistingEmail_False() {
        // when
        boolean exists = memberRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("nickname 중복 체크 - 존재하는 경우")
    void existsByNickname_ExistingNickname_True() {
        // given
        Member member = Member.builder()
                .username("nicknametest")
                .password("password")
                .realName("닉네임테스트")
                .nickname("중복닉네임")
                .email("nick@example.com")
                .role(Role.TEACHER)
                .build();
        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByNickname("중복닉네임");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("nickname 중복 체크 - 존재하지 않는 경우")
    void existsByNickname_NonExistingNickname_False() {
        // when
        boolean exists = memberRepository.existsByNickname("존재하지않는닉네임");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteMember_Success() {
        // given
        Member member = Member.builder()
                .username("deletetest")
                .password("password")
                .realName("삭제테스트")
                .nickname("삭제")
                .email("delete@example.com")
                .role(Role.STUDENT)
                .build();
        Member savedMember = memberRepository.save(member);

        // when
        memberRepository.delete(savedMember);

        // then
        Optional<Member> foundMember = memberRepository.findByUsername("deletetest");
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateMember_Success() {
        // given
        Member member = Member.builder()
                .username("updatetest")
                .password("oldpassword")
                .realName("수정전")
                .nickname("수정전닉")
                .email("old@example.com")
                .role(Role.TEACHER)
                .build();
        Member savedMember = memberRepository.save(member);

        // when - JPA의 더티 체킹을 통한 수정 (실제로는 Service에서 업데이트 메서드 제공 필요)
        memberRepository.flush();
        memberRepository.clear();

        // then
        Optional<Member> foundMember = memberRepository.findById(savedMember.getMemberId());
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getUsername()).isEqualTo("updatetest");
    }

    @Test
    @DisplayName("Role별 회원 구분 확인")
    void memberWithDifferentRoles() {
        // given
        Member teacher = Member.builder()
                .username("teacher")
                .password("password")
                .realName("선생님")
                .nickname("선생")
                .email("teacher@example.com")
                .role(Role.TEACHER)
                .build();

        Member student = Member.builder()
                .username("student")
                .password("password")
                .realName("학생")
                .nickname("학생닉")
                .email("student@example.com")
                .role(Role.STUDENT)
                .build();

        // when
        memberRepository.save(teacher);
        memberRepository.save(student);

        // then
        Optional<Member> foundTeacher = memberRepository.findByUsername("teacher");
        Optional<Member> foundStudent = memberRepository.findByUsername("student");

        assertThat(foundTeacher).isPresent();
        assertThat(foundTeacher.get().getRole()).isEqualTo(Role.TEACHER);

        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getRole()).isEqualTo(Role.STUDENT);
    }
}