package com.example.sca_be.domain.personalquest.service;

import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.auth.repository.TeacherRepository;
import com.example.sca_be.domain.classroom.entity.Classes;
import com.example.sca_be.domain.classroom.repository.ClassesRepository;
import com.example.sca_be.domain.personalquest.dto.*;
import com.example.sca_be.domain.personalquest.entity.Quest;
import com.example.sca_be.domain.personalquest.entity.QuestAssignment;
import com.example.sca_be.domain.personalquest.entity.QuestStatus;
import com.example.sca_be.domain.personalquest.entity.Submission;
import com.example.sca_be.global.exception.CustomException;
import com.example.sca_be.global.exception.ErrorCode;
import com.example.sca_be.domain.personalquest.repository.QuestAssignmentRepository;
import com.example.sca_be.domain.personalquest.repository.QuestRepository;
import com.example.sca_be.domain.personalquest.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalQuestService {

    private final QuestRepository questRepository;
    private final QuestAssignmentRepository questAssignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassesRepository classesRepository;

    /**
     * 퀘스트 생성 및 할당
     */
    @Transactional
    public QuestCreateResponse createQuest(Integer teacherId, QuestCreateRequest request) {
        // Validation
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CustomException(ErrorCode.TITLE_REQUIRED);
        }
        if (request.getClassId() == null) {
            throw new CustomException(ErrorCode.CLASS_ID_REQUIRED);
        }
        if (request.getAssignments() == null || request.getAssignments().isEmpty()) {
            throw new CustomException(ErrorCode.STUDENTS_REQUIRED);
        }

        // AI 사용 시 AI 추천 보상 정보 검증
        if (Boolean.TRUE.equals(request.getAiUsed())) {
            for (QuestCreateRequest.AssignmentRequest assignmentReq : request.getAssignments()) {
                if (assignmentReq.getAiRewardCoral() == null || assignmentReq.getAiRewardResearchData() == null) {
                    throw new CustomException(ErrorCode.AI_REWARD_REQUIRED);
                }
            }
        }

        // Teacher 조회
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));

        // Class 조회 및 권한 확인
        Classes classes = classesRepository.findById(request.getClassId())
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        if (!classes.getTeacher().getMemberId().equals(teacherId)) {
            throw new CustomException(ErrorCode.CLASS_ACCESS_DENIED);
        }

        // Quest 생성
        Quest quest = Quest.builder()
                .teacher(teacher)
                .title(request.getTitle())
                .teacherContent(request.getTeacherContent())
                .difficulty(request.getDifficulty())
                .deadline(request.getDeadline())
                .rewardCoralDefault(request.getRewardCoralDefault())
                .rewardResearchDataDefault(request.getRewardResearchDataDefault())
                .build();

        Quest savedQuest = questRepository.save(quest);

        // QuestAssignment 생성 - 해당 반의 학생들만 대상으로
        List<QuestAssignment> assignments = new ArrayList<>();
        for (QuestCreateRequest.AssignmentRequest assignmentReq : request.getAssignments()) {
            Student student = studentRepository.findById(assignmentReq.getStudentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

            // 학생이 해당 반에 소속되어 있는지 확인
            if (student.getClasses() == null ||
                !student.getClasses().getClassId().equals(request.getClassId())) {
                throw new CustomException(ErrorCode.STUDENT_NOT_IN_CLASS);
            }

            QuestAssignment assignment = QuestAssignment.builder()
                    .quest(savedQuest)
                    .student(student)
                    .rewardCoralPersonal(assignmentReq.getRewardCoralPersonal())
                    .rewardResearchDataPersonal(assignmentReq.getRewardResearchDataPersonal())
                    .status(QuestStatus.ASSIGNED)
                    .build();

            assignments.add(questAssignmentRepository.save(assignment));
        }

        // Response 생성
        List<QuestCreateResponse.AssignmentInfo> assignmentInfos = assignments.stream()
                .map(assignment -> QuestCreateResponse.AssignmentInfo.builder()
                        .assignmentId(assignment.getAssignmentId())
                        .studentId(assignment.getStudent().getMemberId())
                        .studentName(assignment.getStudent().getMember().getRealName())
                        .rewardCoralPersonal(assignment.getRewardCoralPersonal())
                        .rewardResearchDataPersonal(assignment.getRewardResearchDataPersonal())
                        .status(assignment.getStatus())
                        .build())
                .collect(Collectors.toList());

        return QuestCreateResponse.builder()
                .questId(savedQuest.getQuestId())
                .title(savedQuest.getTitle())
                .teacherContent(savedQuest.getTeacherContent())
                .difficulty(savedQuest.getDifficulty())
                .deadline(savedQuest.getDeadline())
                .classId(request.getClassId())
                .rewardCoralDefault(savedQuest.getRewardCoralDefault())
                .rewardResearchDataDefault(savedQuest.getRewardResearchDataDefault())
                .createdAt(savedQuest.getCreatedAt())
                .assignments(assignmentInfos)
                .totalAssigned(assignments.size())
                .build();
    }

    /**
     * AI 보상 추천 (하드코딩)
     */
    public AIRecommendResponse recommendRewards(AIRecommendRequest request) {
        // 하드코딩된 데이터 반환
        List<AIRecommendResponse.RecommendationInfo> recommendations = new ArrayList<>();

        for (Integer studentId : request.getStudentIds()) {
            // 학생 정보 조회 (이름만 가져오기 위함)
            Student student = studentRepository.findById(studentId)
                    .orElse(null);

            String studentName = student != null ? student.getMember().getRealName() : "학생" + studentId;

            // 하드코딩된 추천 값
            recommendations.add(AIRecommendResponse.RecommendationInfo.builder()
                    .studentId(studentId)
                    .studentName(studentName)
                    .recommendedCoral(50)
                    .recommendedResearchData(30)
                    .reason("기본 보상 적용")
                    .build());
        }

        return AIRecommendResponse.builder()
                .rewardCoralDefault(50)
                .rewardResearchDataDefault(70)
                .recommendations(recommendations)
                .build();
    }

    /**
     * 승인 대기 목록 조회
     */
    public PendingQuestResponse getPendingQuests(Integer teacherId, Integer classId) {
        List<QuestAssignment> assignments;

        if (classId != null) {
            assignments = questAssignmentRepository.findPendingAssignmentsByTeacherAndClass(
                    teacherId, QuestStatus.SUBMITTED, classId);
        } else {
            assignments = questAssignmentRepository.findPendingAssignmentsByTeacher(
                    teacherId, QuestStatus.SUBMITTED);
        }

        List<PendingQuestResponse.PendingAssignment> assignmentList = assignments.stream()
                .map(assignment -> PendingQuestResponse.PendingAssignment.builder()
                        .assignmentId(assignment.getAssignmentId())
                        .questId(assignment.getQuest().getQuestId())
                        .title(assignment.getQuest().getTitle())
                        .studentId(assignment.getStudent().getMemberId())
                        .studentName(assignment.getStudent().getMember().getRealName())
                        .className(assignment.getStudent().getClassEntity() != null ?
                                assignment.getStudent().getClassEntity().getClassName() : null)
                        .submittedAt(assignment.getSubmission() != null ?
                                assignment.getSubmission().getSubmittedAt() : null)
                        .rewardCoralPersonal(assignment.getRewardCoralPersonal())
                        .rewardResearchDataPersonal(assignment.getRewardResearchDataPersonal())
                        .status(assignment.getStatus())
                        .build())
                .collect(Collectors.toList());

        return PendingQuestResponse.builder()
                .assignments(assignmentList)
                .totalCount(assignments.size())
                .build();
    }

    /**
     * 퀘스트 할당 상세 조회
     */
    public QuestDetailResponse getAssignmentDetail(Integer teacherId, Integer assignmentId) {
        QuestAssignment assignment = questAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // 권한 체크
        if (!assignment.getQuest().getTeacher().getMemberId().equals(teacherId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }

        QuestDetailResponse.SubmissionInfo submissionInfo = null;
        if (assignment.getSubmission() != null) {
            Submission submission = assignment.getSubmission();
            submissionInfo = QuestDetailResponse.SubmissionInfo.builder()
                    .submissionId(submission.getSubmissionId())
                    .studentContent(submission.getStudentContent())
                    .attachmentUrl(submission.getAttachmentUrl())
                    .submittedAt(submission.getSubmittedAt())
                    .comment(submission.getComment())
                    .build();
        }

        return QuestDetailResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .quest(QuestDetailResponse.QuestInfo.builder()
                        .questId(assignment.getQuest().getQuestId())
                        .title(assignment.getQuest().getTitle())
                        .teacherContent(assignment.getQuest().getTeacherContent())
                        .build())
                .student(QuestDetailResponse.StudentInfo.builder()
                        .studentId(assignment.getStudent().getMemberId())
                        .studentName(assignment.getStudent().getMember().getRealName())
                        .className(assignment.getStudent().getClassEntity() != null ?
                                assignment.getStudent().getClassEntity().getClassName() : null)
                        .build())
                .rewardCoralPersonal(assignment.getRewardCoralPersonal())
                .rewardResearchDataPersonal(assignment.getRewardResearchDataPersonal())
                .status(assignment.getStatus())
                .submission(submissionInfo)
                .build();
    }

    /**
     * 퀘스트 승인
     */
    @Transactional
    public QuestApproveResponse approveQuest(Integer teacherId, Integer assignmentId, QuestApproveRequest request) {
        QuestAssignment assignment = questAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // 권한 체크
        if (!assignment.getQuest().getTeacher().getMemberId().equals(teacherId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }

        // 상태 체크
        if (assignment.getStatus() != QuestStatus.SUBMITTED) {
            throw new CustomException(ErrorCode.ASSIGNMENT_NOT_SUBMITTED);
        }

        // 보상 지급
        Student student = assignment.getStudent();
        student.addCoral(assignment.getRewardCoralPersonal());
        student.addResearchData(assignment.getRewardResearchDataPersonal());

        // 상태 업데이트
        assignment.updateStatus(QuestStatus.APPROVED);

        // 코멘트 업데이트
        if (assignment.getSubmission() != null) {
            assignment.getSubmission().updateComment(request.getComment());
        }

        return QuestApproveResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .questId(assignment.getQuest().getQuestId())
                .studentId(student.getMemberId())
                .studentName(student.getMember().getRealName())
                .status(QuestStatus.APPROVED)
                .rewardsGranted(QuestApproveResponse.RewardsGranted.builder()
                        .coral(assignment.getRewardCoralPersonal())
                        .researchData(assignment.getRewardResearchDataPersonal())
                        .build())
                .comment(request.getComment())
                .approvedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 퀘스트 반려
     */
    @Transactional
    public QuestRejectResponse rejectQuest(Integer teacherId, Integer assignmentId, QuestRejectRequest request) {
        QuestAssignment assignment = questAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // 권한 체크
        if (!assignment.getQuest().getTeacher().getMemberId().equals(teacherId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }

        // 상태 업데이트
        assignment.updateStatus(QuestStatus.REJECTED);

        // 코멘트 업데이트
        if (assignment.getSubmission() != null) {
            assignment.getSubmission().updateComment(request.getComment());
        }

        return QuestRejectResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .questId(assignment.getQuest().getQuestId())
                .studentId(assignment.getStudent().getMemberId())
                .studentName(assignment.getStudent().getMember().getRealName())
                .status(QuestStatus.REJECTED)
                .comment(request.getComment())
                .rejectedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 내 퀘스트 목록 조회 (학생)
     */
    public MyQuestListResponse getMyQuests(Integer studentId, String status) {
        List<QuestAssignment> assignments;
        String responseStatus;

        if (status == null || status.isEmpty()) {
            status = "ACTIVE";
        }

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        switch (status.toUpperCase()) {
            case "ACTIVE":
                List<QuestStatus> activeStatuses = Arrays.asList(
                        QuestStatus.ASSIGNED, QuestStatus.SUBMITTED, QuestStatus.REJECTED);
                assignments = questAssignmentRepository.findByStudentAndStatusIn(
                        studentId, activeStatuses);
                responseStatus = "ACTIVE";
                break;

            case "APPROVED":
                assignments = questAssignmentRepository.findApprovedQuestsWithinWeek(
                        studentId, QuestStatus.APPROVED, oneWeekAgo);
                responseStatus = "APPROVED";
                break;

            case "EXPIRED":
                assignments = questAssignmentRepository.findExpiredQuestsWithinWeek(
                        studentId, QuestStatus.EXPIRED, oneWeekAgo);
                responseStatus = "EXPIRED";
                break;

            default:
                List<QuestStatus> defaultActiveStatuses = Arrays.asList(
                        QuestStatus.ASSIGNED, QuestStatus.SUBMITTED, QuestStatus.REJECTED);
                assignments = questAssignmentRepository.findByStudentAndStatusIn(
                        studentId, defaultActiveStatuses);
                responseStatus = "ACTIVE";
                break;
        }

        return MyQuestListResponse.builder()
                .status(responseStatus)
                .quests(convertToQuestItems(assignments))
                .totalCount(assignments.size())
                .build();
    }

    private List<MyQuestListResponse.QuestItem> convertToQuestItems(List<QuestAssignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    MyQuestListResponse.QuestItem.SubmissionSummary submissionSummary = null;
                    if (assignment.getSubmission() != null) {
                        submissionSummary = MyQuestListResponse.QuestItem.SubmissionSummary.builder()
                                .submittedAt(assignment.getSubmission().getSubmittedAt())
                                .comment(assignment.getSubmission().getComment())
                                .build();
                    }

                    return MyQuestListResponse.QuestItem.builder()
                            .assignmentId(assignment.getAssignmentId())
                            .questId(assignment.getQuest().getQuestId())
                            .title(assignment.getQuest().getTitle())
                            .teacherContent(assignment.getQuest().getTeacherContent())
                            .rewardCoralPersonal(assignment.getRewardCoralPersonal())
                            .rewardResearchDataPersonal(assignment.getRewardResearchDataPersonal())
                            .status(assignment.getStatus())
                            .createdAt(assignment.getQuest().getCreatedAt())
                            .submission(submissionSummary)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 퀘스트 제출
     */
    @Transactional
    public QuestSubmitResponse submitQuest(Integer studentId, Integer assignmentId, QuestSubmitRequest request) {
        QuestAssignment assignment = questAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // 권한 체크
        if (!assignment.getStudent().getMemberId().equals(studentId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }

        // 상태 체크
        if (assignment.getStatus() == QuestStatus.SUBMITTED) {
            throw new CustomException(ErrorCode.ALREADY_SUBMITTED);
        }
        if (assignment.getStatus() == QuestStatus.EXPIRED) {
            throw new CustomException(ErrorCode.QUEST_EXPIRED);
        }
        if (assignment.getStatus() == QuestStatus.APPROVED) {
            throw new CustomException(ErrorCode.CANNOT_MODIFY_APPROVED_QUEST);
        }

        // Submission 생성
        Submission submission = Submission.builder()
                .questAssignment(assignment)
                .studentContent(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        Submission savedSubmission = submissionRepository.save(submission);

        // 상태 업데이트
        assignment.updateStatus(QuestStatus.SUBMITTED);

        return QuestSubmitResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .questId(assignment.getQuest().getQuestId())
                .status(QuestStatus.SUBMITTED)
                .submission(QuestSubmitResponse.SubmissionInfo.builder()
                        .submissionId(savedSubmission.getSubmissionId())
                        .studentContent(savedSubmission.getStudentContent())
                        .attachmentUrl(savedSubmission.getAttachmentUrl())
                        .submittedAt(savedSubmission.getSubmittedAt())
                        .build())
                .build();
    }

    /**
     * 퀘스트 제출 수정
     */
    @Transactional
    public QuestSubmitResponse updateSubmission(Integer studentId, Integer assignmentId, QuestSubmitRequest request) {
        QuestAssignment assignment = questAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // 권한 체크
        if (!assignment.getStudent().getMemberId().equals(studentId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }

        // 상태 체크
        if (assignment.getStatus() == QuestStatus.APPROVED) {
            throw new CustomException(ErrorCode.CANNOT_MODIFY_APPROVED_QUEST);
        }
        if (assignment.getStatus() != QuestStatus.SUBMITTED && assignment.getStatus() != QuestStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_QUEST_STATUS, "제출되지 않은 퀘스트는 수정할 수 없습니다.");
        }

        // Submission 수정
        Submission submission = assignment.getSubmission();
        if (submission == null) {
            throw new CustomException(ErrorCode.ASSIGNMENT_NOT_SUBMITTED, "제출 내용이 없습니다.");
        }

        submission.updateContent(request.getContent(), request.getAttachmentUrl());
        assignment.updateStatus(QuestStatus.SUBMITTED);

        return QuestSubmitResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .questId(assignment.getQuest().getQuestId())
                .status(QuestStatus.SUBMITTED)
                .submission(QuestSubmitResponse.SubmissionInfo.builder()
                        .submissionId(submission.getSubmissionId())
                        .studentContent(submission.getStudentContent())
                        .attachmentUrl(submission.getAttachmentUrl())
                        .submittedAt(submission.getSubmittedAt())
                        .build())
                .build();
    }

    /**
     * 퀘스트 코멘트 조회
     */
    public QuestCommentResponse getQuestComment(Integer studentId, Integer assignmentId) {
        QuestAssignment assignment = questAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // 권한 체크
        if (!assignment.getStudent().getMemberId().equals(studentId)) {
            throw new CustomException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }

        // 상태 체크
        if (assignment.getStatus() != QuestStatus.APPROVED && assignment.getStatus() != QuestStatus.REJECTED) {
            throw new CustomException(ErrorCode.NOT_PROCESSED_YET);
        }

        Submission submission = assignment.getSubmission();
        if (submission == null) {
            throw new CustomException(ErrorCode.ASSIGNMENT_NOT_SUBMITTED, "제출 내용이 없습니다.");
        }

        return QuestCommentResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .quest(QuestCommentResponse.QuestInfo.builder()
                        .questId(assignment.getQuest().getQuestId())
                        .title(assignment.getQuest().getTitle())
                        .build())
                .status(assignment.getStatus())
                .submission(QuestCommentResponse.SubmissionInfo.builder()
                        .submittedAt(submission.getSubmittedAt())
                        .comment(submission.getComment())
                        .build())
                .processedAt(submission.getSubmittedAt())
                .build();
    }
}
