package com.example.sca_be.domain.classroom.service;

import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.auth.repository.MemberRepository;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.auth.repository.TeacherRepository;
import com.example.sca_be.domain.classroom.dto.*;
import com.example.sca_be.domain.classroom.entity.Classes;
import com.example.sca_be.domain.classroom.repository.ClassesRepository;
import com.example.sca_be.domain.classroom.util.InviteCodeGenerator;
import com.example.sca_be.global.exception.CustomException;
import com.example.sca_be.global.exception.ErrorCode;
import com.example.sca_be.global.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final MemberRepository memberRepository;
    private final Random random = new Random();

    //현재 로그인한 선생님의 반 목록 조회
    public ClassListResponse getClassList() {
        Teacher teacher = getCurrentTeacher();
        Member member = teacher.getMember();

        List<Classes> classes = classesRepository.findByTeacherOrderByCreatedAtDesc(teacher);

        List<ClassListResponse.ClassSummary> classSummaries = classes.stream()
                .map(c -> {
                    int studentCount = studentRepository.countByClasses(c);

                    int ongoingQuestCount = random.nextInt(4) + 2;//이거 나중에 퀘스트 관련 로직 잡고 고쳐야 함

                    return ClassListResponse.ClassSummary.builder()
                            .classId(c.getClassId())
                            .className(c.getClassName())
                            .studentCount(studentCount)
                            .ongoingQuestCount(ongoingQuestCount)
                            .createdAt(c.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build();
                })
                .collect(Collectors.toList());

        return ClassListResponse.builder()
                .teacherName(member.getRealName())
                .classes(classSummaries)
                .totalCount(classSummaries.size())
                .build();
    }

    //반 생성
    @Transactional
    public CreateClassResponse createClass(CreateClassRequest request) {
        Teacher teacher = getCurrentTeacher();
        Member member = teacher.getMember();

        // 초대 코드 생성 (중복 체크)
        String inviteCode = generateUniqueInviteCode();

        Classes classes = Classes.builder()
                .teacher(teacher)
                .className(request.getClassName())
                .inviteCode(inviteCode)
                .grade(request.getGrade())
                .subject(request.getSubject())
                .build();

        Classes savedClass = classesRepository.save(classes);

        return CreateClassResponse.builder()
                .classId(savedClass.getClassId())
                .className(savedClass.getClassName())
                .grade(savedClass.getGrade())
                .subject(savedClass.getSubject())
                .inviteCode(savedClass.getInviteCode())
                .teacherId(teacher.getMemberId())
                .teacherName(member.getRealName())
                .studentCount(0)
                .createdAt(savedClass.getCreatedAt())
                .build();
    }

    //반의 학생 리스트 조회
    public StudentListResponse getStudentList(Integer classId) {
        Teacher teacher = getCurrentTeacher();

        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // 본인이 생성한 반만 조회 가능
        if (!classes.getTeacher().getMemberId().equals(teacher.getMemberId())) {
            throw new CustomException(ErrorCode.CLASS_ACCESS_DENIED);
        }

        List<Student> students = studentRepository.findByClassesOrderByMember_RealNameAsc(classes);

        List<StudentListResponse.StudentInfo> studentInfos = students.stream()
                .map(s -> {

                    int pendingQuests = random.nextInt(4);//일단 quest 구현 전이어서 랜덤으로 설정

                    return StudentListResponse.StudentInfo.builder()
                            .studentId(s.getMemberId())
                            .name(s.getMember().getRealName())
                            .pendingQuests(pendingQuests)
                            .coral(s.getCoral() != null ? s.getCoral() : 0)
                            .researchData(s.getResearchData() != null ? s.getResearchData() : 0)
                            .build();
                })
                .collect(Collectors.toList());

        return StudentListResponse.builder()
                .classId(classes.getClassId())
                .className(classes.getClassName())
                .studentCount(studentInfos.size())
                .students(studentInfos)
                .build();
    }

    //반 상세 조회 (진행 중인 퀘스트 및 레이드 정보 포함)
    public ClassDetailResponse getClassDetail(Integer classId) {
        Teacher teacher = getCurrentTeacher();

        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // 본인이 생성한 반만 조회 가능
        if (!classes.getTeacher().getMemberId().equals(teacher.getMemberId())) {
            throw new CustomException(ErrorCode.CLASS_ACCESS_DENIED);
        }

        int studentCount = studentRepository.countByClasses(classes);

        // 임시 하드코딩: 진행 중인 단체 퀘스트
        List<ClassDetailResponse.OngoingGroupQuest> ongoingGroupQuests = List.of(
                ClassDetailResponse.OngoingGroupQuest.builder()
                        .questId(101)
                        .title("출석 체크")
                        .progress(ClassDetailResponse.QuestProgress.builder()
                                .completed(Math.min(studentCount - 3, studentCount))
                                .required(studentCount)
                                .build())
                        .build(),
                ClassDetailResponse.OngoingGroupQuest.builder()
                        .questId(102)
                        .title("수업 참여도")
                        .progress(ClassDetailResponse.QuestProgress.builder()
                                .completed(Math.min(studentCount - 1, studentCount))
                                .required(studentCount)
                                .build())
                        .build(),
                ClassDetailResponse.OngoingGroupQuest.builder()
                        .questId(103)
                        .title("과제 제출")
                        .progress(ClassDetailResponse.QuestProgress.builder()
                                .completed(Math.min(studentCount - 4, studentCount))
                                .required(studentCount)
                                .build())
                        .build()
        );

        // 임시 하드코딩: 진행 중인 레이드 정보
        int bossHpCurrent = 4200;
        int bossHpTotal = 10000;
        ClassDetailResponse.OngoingRaid ongoingRaid = ClassDetailResponse.OngoingRaid.builder()
                .raidId(1)
                .title("중간고사 대비 크라켄")
                .bossHp(ClassDetailResponse.BossHp.builder()
                        .current(bossHpCurrent)
                        .total(bossHpTotal)
                        .percentage((bossHpCurrent * 100) / bossHpTotal)
                        .build())
                .participants(studentCount)
                .endDate("2025-10-31T23:59:59Z")
                .build();

        return ClassDetailResponse.builder()
                .classId(classes.getClassId())
                .className(classes.getClassName())
                .inviteCode(classes.getInviteCode())
                .ongoingGroupQuests(ongoingGroupQuests)
                .ongoingRaid(ongoingRaid)
                .build();
    }

    //현재 로그인한 선생님 조회
    private Teacher getCurrentTeacher() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Integer memberId = userDetails.getMemberId();

        return teacherRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));
    }

    //코드 생성 로직
    private String generateUniqueInviteCode() {
        String inviteCode;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            inviteCode = InviteCodeGenerator.generate();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new CustomException(ErrorCode.INVITE_CODE_GENERATION_FAILED);
            }
        } while (classesRepository.existsByInviteCode(inviteCode));

        return inviteCode;
    }
}
