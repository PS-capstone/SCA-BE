package com.example.sca_be.domain.personalquest.service;

import com.example.sca_be.domain.ai.dto.BaseReward;
import com.example.sca_be.domain.ai.dto.LearningEvent;
import com.example.sca_be.domain.ai.dto.PersonalizedReward;
import com.example.sca_be.domain.ai.dto.QuestAnalysisResult;
import com.example.sca_be.domain.ai.entity.QuestDifficulty;
import com.example.sca_be.domain.ai.entity.StudentsFactors;
import com.example.sca_be.domain.ai.entity.StudentsQuestFactors;
import com.example.sca_be.domain.ai.repository.StudentsFactorsRepository;
import com.example.sca_be.domain.ai.repository.StudentsQuestFactorsRepository;
import com.example.sca_be.domain.ai.service.LearningEngineService;
import com.example.sca_be.domain.ai.service.QuestAnalyzerService;
import com.example.sca_be.domain.ai.service.StudentFactorService;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.auth.repository.TeacherRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalQuestService {

    private final QuestRepository questRepository;
    private final QuestAssignmentRepository questAssignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final QuestAnalyzerService questAnalyzerService;
    private final StudentFactorService studentFactorService;
    private final LearningEngineService learningEngineService;
    private final com.example.sca_be.domain.notification.service.NotificationService notificationService;
    private final StudentsFactorsRepository studentsFactorsRepository;
    private final StudentsQuestFactorsRepository studentsQuestFactorsRepository;

    /**
     * 퀘스트 생성 및 할당
     */
    @Transactional
    public QuestCreateResponse createQuest(Integer teacherId, QuestCreateRequest request) {
        // Validation
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CustomException(ErrorCode.TITLE_REQUIRED);
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
        questRepository.flush(); // Quest를 먼저 DB에 반영
        System.out.println("=== 퀘스트 생성 완료 ===");
        System.out.println("Quest ID: " + savedQuest.getQuestId() + ", 제목: " + savedQuest.getTitle());

        // QuestAssignment 생성
        List<QuestAssignment> assignments = new ArrayList<>();
        for (QuestCreateRequest.AssignmentRequest assignmentReq : request.getAssignments()) {
            try {
                Student student = studentRepository.findById(assignmentReq.getStudentId())
                        .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

                System.out.println("퀘스트 할당 생성 시작 - 학생 ID: " + student.getMemberId() + ", 학생 이름: " + student.getMember().getRealName());

                QuestAssignment assignment = QuestAssignment.builder()
                        .quest(savedQuest)
                        .student(student)
                        .rewardCoralPersonal(assignmentReq.getRewardCoralPersonal())
                        .rewardResearchDataPersonal(assignmentReq.getRewardResearchDataPersonal())
                        .status(QuestStatus.ASSIGNED)
                        .build();

                QuestAssignment savedAssignment = questAssignmentRepository.save(assignment);
                questAssignmentRepository.flush(); // 즉시 DB에 반영
                
                System.out.println("✅ 퀘스트 할당 저장 완료 - Assignment ID: " + savedAssignment.getAssignmentId() +
                                 ", Quest ID: " + savedQuest.getQuestId() +
                                 ", Student ID: " + savedAssignment.getStudent().getMemberId() +
                                 ", Status: " + savedAssignment.getStatus());

                assignments.add(savedAssignment);

                // 공지 생성 및 웹소켓 전송
                notificationService.createAndBroadcastNotification(
                        student,
                        com.example.sca_be.domain.notification.entity.NoticeType.PERSONAL_QUEST_ASSIGNED,
                        "새로운 퀘스트가 추가되었습니다",
                        savedQuest.getTitle(),
                        savedAssignment,
                        null,
                        null
                );
            } catch (Exception e) {
                System.err.println("❌ 퀘스트 할당 저장 실패 - 학생 ID: " + assignmentReq.getStudentId());
                System.err.println("에러: " + e.getMessage());
                e.printStackTrace();
                throw e; // 에러를 다시 던져서 트랜잭션 롤백
            }
        }
        
        System.out.println("총 " + assignments.size() + "개의 할당이 저장되었습니다.");

        // Response 생성 (트랜잭션 내에서 member 정보를 미리 로드)
        List<QuestCreateResponse.AssignmentInfo> assignmentInfos = assignments.stream()
                .map(assignment -> {
                    // Lazy loading을 트랜잭션 내에서 처리
                    String studentName = assignment.getStudent().getMember().getRealName();
                    return QuestCreateResponse.AssignmentInfo.builder()
                            .assignmentId(assignment.getAssignmentId())
                            .studentId(assignment.getStudent().getMemberId())
                            .studentName(studentName)
                            .rewardCoralPersonal(assignment.getRewardCoralPersonal())
                            .rewardResearchDataPersonal(assignment.getRewardResearchDataPersonal())
                            .status(assignment.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        System.out.println("Response 생성 완료 - " + assignmentInfos.size() + "개의 할당 정보 포함");

        // 학습 엔진 실행 (AI 사용 시 + 수정 발생 시)
        if (Boolean.TRUE.equals(request.getAiUsed())) {
            triggerLearning(request, assignments);
        }

        return QuestCreateResponse.builder()
                .questId(savedQuest.getQuestId())
                .title(savedQuest.getTitle())
                .teacherContent(savedQuest.getTeacherContent())
                .difficulty(savedQuest.getDifficulty())
                .deadline(savedQuest.getDeadline())
                .rewardCoralDefault(savedQuest.getRewardCoralDefault())
                .rewardResearchDataDefault(savedQuest.getRewardResearchDataDefault())
                .createdAt(savedQuest.getCreatedAt())
                .assignments(assignmentInfos)
                .totalAssigned(assignments.size())
                .build();
    }

    /**
     * 학습 엔진 트리거
     * AI 추천과 선생님 최종값이 다른 경우 비동기로 학습 실행
     */
    private void triggerLearning(QuestCreateRequest request, List<QuestAssignment> assignments) {
        log.info("Triggering learning for {} assignments", assignments.size());

        for (int i = 0; i < request.getAssignments().size(); i++) {
            QuestCreateRequest.AssignmentRequest assignmentReq = request.getAssignments().get(i);
            QuestAssignment savedAssignment = assignments.get(i);

            // AI 추천값과 선생님 최종값 비교
            boolean coralModified = !assignmentReq.getAiRewardCoral().equals(assignmentReq.getRewardCoralPersonal());
            boolean researchModified = !assignmentReq.getAiRewardResearchData().equals(assignmentReq.getRewardResearchDataPersonal());

            if (coralModified || researchModified) {
                log.info("Modification detected for student {}, triggering learning",
                        savedAssignment.getStudent().getMemberId());

                // cognitive_score와 effort_score를 AI 추천값으로부터 역산
                int cognitiveScore = estimateCognitiveScore(
                        assignmentReq.getAiRewardCoral(),
                        assignmentReq.getAiRewardResearchData()
                );
                int effortScore = estimateEffortScore(
                        assignmentReq.getAiRewardCoral(),
                        assignmentReq.getAiRewardResearchData()
                );

                // 학생의 전역 계수 조회
                Double globalFactor = 1.0; // 기본값
                Double difficultyFactor = 1.0; // 기본값

                StudentsFactors studentFactors = studentsFactorsRepository.findByStudent(savedAssignment.getStudent())
                        .orElse(null);

                if (studentFactors != null) {
                    globalFactor = studentFactors.getGlobalFactor();

                    // 해당 난이도의 계수 조회
                    StudentsQuestFactors questFactors = studentsQuestFactorsRepository
                            .findByStudentFactorAndDifficulty(studentFactors, request.getDifficulty())
                            .orElse(null);

                    if (questFactors != null) {
                        difficultyFactor = questFactors.getFactorValue();
                    }
                }

                // 학습 이벤트 생성 및 비동기 실행
                LearningEvent event = LearningEvent.builder()
                        .studentId(savedAssignment.getStudent().getMemberId())
                        .assignmentId(savedAssignment.getAssignmentId())
                        .difficulty(request.getDifficulty())
                        .cognitiveScore(cognitiveScore)
                        .effortScore(effortScore)
                        .aiCoral(assignmentReq.getAiRewardCoral())
                        .aiResearchData(assignmentReq.getAiRewardResearchData())
                        .teacherCoral(assignmentReq.getRewardCoralPersonal())
                        .teacherResearchData(assignmentReq.getRewardResearchDataPersonal())
                        .globalFactor(globalFactor)
                        .difficultyFactor(difficultyFactor)
                        .build();

                learningEngineService.learnAsync(event);
            } else {
                log.debug("No modification for student {}, skipping learning",
                        savedAssignment.getStudent().getMemberId());
            }
        }
    }

    /**
     * AI 추천 보상으로부터 인지 점수 추정 (폴백)
     * 프론트엔드에서 cognitive_score를 전달하지 않은 경우 사용
     */
    private int estimateCognitiveScore(int coral, int researchData) {
        // coral = (effort × 5) + (cognitive × 2)
        // researchData = (cognitive² × 5) + (effort × 2)
        // 간단한 추정: coral 기반
        return Math.max(1, Math.min(6, (coral - 20) / 10));
    }

    /**
     * AI 추천 보상으로부터 노력 점수 추정 (폴백)
     * 프론트엔드에서 effort_score를 전달하지 않은 경우 사용
     */
    private int estimateEffortScore(int coral, int researchData) {
        // coral = (effort × 5) + (cognitive × 2)
        // 간단한 추정
        return Math.max(1, Math.min(10, coral / 6));
    }

    /**
     * AI 보상 추천
     * PDF 1.2 참고: CompletableFuture로 병렬 처리
     */
    public AIRecommendResponse recommendRewards(AIRecommendRequest request) {
        log.info("AI reward recommendation started for {} students", request.getStudentIds().size());

        try {
            // 퀘스트 텍스트 생성
            String questText = request.getQuestTitle() + "\n" + request.getQuestContent();
            QuestDifficulty difficulty = mapIntToDifficulty(request.getDifficulty());

            // Step 1: 병렬 처리 시작
            // Task A: AI 퀘스트 분석 (비동기)
            CompletableFuture<QuestAnalysisResult> analysisFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("Task A: Starting quest analysis");
                return questAnalyzerService.analyzeQuest(questText, difficulty);
            });

            // Task B: 학생 정보 조회 (비동기) - Member를 fetch join으로 즉시 로딩
            CompletableFuture<List<Student>> studentsFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("Task B: Fetching student information");
                return studentRepository.findByIdsWithMember(request.getStudentIds());
            });

            // Step 2: 두 작업 완료 대기 (약 500ms)
            CompletableFuture.allOf(analysisFuture, studentsFuture).join();

            QuestAnalysisResult analysis = analysisFuture.get();
            List<Student> students = studentsFuture.get();

            log.info("Analysis completed - Cognitive: {}, Effort: {}, Difficulty: {}",
                    analysis.getCognitiveProcessScore(),
                    analysis.getEffortScore(),
                    analysis.getDifficulty());

            // 조회된 학생 수 확인
            if (students.isEmpty()) {
                log.warn("No students found for IDs: {}", request.getStudentIds());
                throw new RuntimeException("선택한 학생을 찾을 수 없습니다.");
            }

            if (students.size() < request.getStudentIds().size()) {
                List<Integer> foundIds = students.stream()
                        .map(Student::getMemberId)
                        .collect(Collectors.toList());
                List<Integer> missingIds = request.getStudentIds().stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                log.warn("Some students not found. Requested: {}, Found: {}, Missing: {}",
                        request.getStudentIds(), foundIds, missingIds);
            }

            // Step 3: 기본 보상 계산
            BaseReward baseReward = StudentFactorService.calculateBaseReward(
                    analysis.getCognitiveProcessScore(),
                    analysis.getEffortScore()
            );

            log.debug("Base reward calculated - Exploration: {}, Coral: {}",
                    baseReward.getExplorationData(), baseReward.getCoral());

            // Step 4: 각 학생별 개인화 보상 계산
            List<AIRecommendResponse.RecommendationInfo> recommendations = students.stream()
                    .filter(student -> {
                        // Member가 null이거나 삭제된 경우 필터링
                        if (student.getMember() == null) {
                            log.warn("Student {} has no member, skipping", student.getMemberId());
                            return false;
                        }
                        return true;
                    })
                    .map(student -> {
                        PersonalizedReward personalizedReward = studentFactorService.calculatePersonalizedReward(
                                student.getMemberId(),
                                analysis.getCognitiveProcessScore(),
                                analysis.getEffortScore(),
                                analysis.getDifficulty()
                        );

                        // 학생 계수 조회
                        Double globalFactor = 1.0;
                        Double difficultyFactor = 1.0;

                        StudentsFactors studentFactors = studentsFactorsRepository.findByStudent(student)
                                .orElse(null);

                        if (studentFactors != null) {
                            globalFactor = studentFactors.getGlobalFactor();

                            // 해당 난이도의 계수 조회
                            Integer difficultyValue = mapDifficultyToInt(analysis.getDifficulty());
                            StudentsQuestFactors questFactors = studentsQuestFactorsRepository
                                    .findByStudentFactorAndDifficulty(studentFactors, difficultyValue)
                                    .orElse(null);

                            if (questFactors != null) {
                                difficultyFactor = questFactors.getFactorValue();
                            }
                        }

                        return AIRecommendResponse.RecommendationInfo.builder()
                                .studentId(student.getMemberId())
                                .studentName(student.getMember() != null ? student.getMember().getRealName() : "알 수 없음")
                                .recommendedCoral(personalizedReward.getCoral())
                                .recommendedResearchData(personalizedReward.getExplorationData())
                                .reason(analysis.getAnalysisReason())
                                .globalFactor(globalFactor)
                                .difficultyFactor(difficultyFactor)
                                .build();
                    })
                    .collect(Collectors.toList());

            // Step 5: 응답 반환
            AIRecommendResponse response = AIRecommendResponse.builder()
                    .rewardCoralDefault(baseReward.getCoral())
                    .rewardResearchDataDefault(baseReward.getExplorationData())
                    .recommendations(recommendations)
                    .build();

            log.info("AI reward recommendation completed for {} students", recommendations.size());

            return response;

        } catch (Exception e) {
            log.error("AI reward recommendation failed", e);
            throw new RuntimeException("AI 보상 추천 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Integer difficulty를 QuestDifficulty Enum으로 변환
     */
    private QuestDifficulty mapIntToDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return QuestDifficulty.MEDIUM;
        }
        return switch (difficulty) {
            case 1 -> QuestDifficulty.EASY;
            case 2 -> QuestDifficulty.BASIC;
            case 3 -> QuestDifficulty.MEDIUM;
            case 4 -> QuestDifficulty.HARD;
            case 5 -> QuestDifficulty.VERY_HARD;
            default -> QuestDifficulty.MEDIUM;
        };
    }

    /**
     * QuestDifficulty Enum을 Integer로 변환
     */
    private Integer mapDifficultyToInt(QuestDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 1;
            case BASIC -> 2;
            case MEDIUM -> 3;
            case HARD -> 4;
            case VERY_HARD -> 5;
        };
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
            log.info("[퀘스트 상세] Submission 조회 - submissionId: {}, attachmentUrl: {}", 
                    submission.getSubmissionId(), submission.getAttachmentUrl());
            submissionInfo = QuestDetailResponse.SubmissionInfo.builder()
                    .submissionId(submission.getSubmissionId())
                    .studentContent(submission.getStudentContent())
                    .attachmentUrl(submission.getAttachmentUrl())
                    .submittedAt(submission.getSubmittedAt())
                    .comment(submission.getComment())
                    .build();
            log.info("[퀘스트 상세] SubmissionInfo 생성 완료 - attachmentUrl: {}", submissionInfo.getAttachmentUrl());
        } else {
            log.warn("[퀘스트 상세] Submission이 null입니다. assignmentId: {}", assignmentId);
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
        Integer rewardCoral = assignment.getRewardCoralPersonal() != null ? assignment.getRewardCoralPersonal() : 0;
        Integer rewardResearchData = assignment.getRewardResearchDataPersonal() != null ? assignment.getRewardResearchDataPersonal() : 0;
        
        System.out.println("=== 퀘스트 승인 ===");
        System.out.println("학생 ID: " + student.getMemberId());
        System.out.println("Assignment ID: " + assignment.getAssignmentId());
        System.out.println("지급할 코랄: " + rewardCoral);
        System.out.println("지급할 탐사데이터: " + rewardResearchData);
        System.out.println("지급 전 코랄: " + student.getCoral());
        System.out.println("지급 전 탐사데이터: " + student.getResearchData());
        
        student.addCoral(rewardCoral);
        student.addResearchData(rewardResearchData);
        studentRepository.save(student); // 명시적으로 저장
        studentRepository.flush(); // 즉시 DB에 반영
        
        System.out.println("지급 후 코랄: " + student.getCoral());
        System.out.println("지급 후 탐사데이터: " + student.getResearchData());
        System.out.println("==================");

        // 상태 업데이트
        assignment.updateStatus(QuestStatus.APPROVED);

        // 코멘트 업데이트
        if (assignment.getSubmission() != null) {
            assignment.getSubmission().updateComment(request.getComment());
        }

        // 승인 공지 생성 및 웹소켓 전송
        notificationService.createAndBroadcastNotification(
                student,
                com.example.sca_be.domain.notification.entity.NoticeType.PERSONAL_QUEST_APPROVED,
                "퀘스트가 승인되었습니다",
                assignment.getQuest().getTitle(),
                assignment,
                null,
                null
        );

        // 보상 수령 활동로그 생성 및 웹소켓 전송
        notificationService.createAndBroadcastActivityLog(
                student,
                com.example.sca_be.domain.notification.entity.ActionLogType.REWARD_RECEIVED,
                assignment.getQuest().getTitle() + " 완료",
                rewardCoral,
                rewardResearchData,
                assignment,
                null,
                null
        );

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

        // 거절 공지 생성 및 웹소켓 전송
        notificationService.createAndBroadcastNotification(
                assignment.getStudent(),
                com.example.sca_be.domain.notification.entity.NoticeType.PERSONAL_QUEST_REJECTED,
                "퀘스트가 반려되었습니다",
                assignment.getQuest().getTitle(),
                assignment,
                null,
                null
        );

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
        List<QuestStatus> activeStatuses = Arrays.asList(
                QuestStatus.ASSIGNED, QuestStatus.SUBMITTED, QuestStatus.REJECTED);

        System.out.println("=== 퀘스트 조회 시작 ===");
        System.out.println("조회할 학생 ID: " + studentId);
        System.out.println("조회할 상태 목록: " + activeStatuses);
        
        // 전체 할당 조회 (디버깅용)
        List<QuestAssignment> allAssignments = questAssignmentRepository.findAll();
        System.out.println("전체 퀘스트 할당 수: " + allAssignments.size());
        
        // 쿼리로 활성 퀘스트 조회
        List<QuestAssignment> activeQuests = questAssignmentRepository.findByStudentAndStatusIn(
                studentId, activeStatuses);
        System.out.println("쿼리 결과 - 학생 ID: " + studentId + ", 활성 퀘스트 수: " + activeQuests.size());
        
        // 각 퀘스트 상세 정보 출력
        if (activeQuests.isEmpty()) {
            System.out.println("⚠️ 활성 퀘스트가 없습니다!");
        } else {
            for (QuestAssignment qa : activeQuests) {
                System.out.println("  ✅ Assignment ID: " + qa.getAssignmentId() + 
                                 ", Quest ID: " + qa.getQuest().getQuestId() + 
                                 ", Status: " + qa.getStatus() + 
                                 ", Student ID: " + qa.getStudent().getMemberId());
            }
        }
        
        List<QuestAssignment> allStudentAssignments = allAssignments.stream()
                .filter(qa -> {
                    Integer qaStudentId = qa.getStudent().getMemberId();
                    boolean matches = qaStudentId.equals(studentId);
                    if (matches) {
                        System.out.println("  🔍 발견 - Assignment ID: " + qa.getAssignmentId() + 
                                         ", Status: " + qa.getStatus() + 
                                         ", Student ID: " + qaStudentId);
                    }
                    return matches;
                })
                .collect(Collectors.toList());
        System.out.println("전체 할당 중 학생 ID " + studentId + "의 할당 수: " + allStudentAssignments.size());
        
        // ASSIGNED 상태인 할당만 필터링
        List<QuestAssignment> assignedQuests = allStudentAssignments.stream()
                .filter(qa -> qa.getStatus() == QuestStatus.ASSIGNED)
                .collect(Collectors.toList());
        System.out.println("학생 ID " + studentId + "의 ASSIGNED 상태 퀘스트 수: " + assignedQuests.size());
        
        System.out.println("======================");

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<QuestAssignment> expiredQuests = questAssignmentRepository.findExpiredQuestsWithinWeek(
                studentId, QuestStatus.EXPIRED, oneWeekAgo);
        System.out.println("만료 퀘스트 수: " + expiredQuests.size());

        List<QuestAssignment> approvedQuests = questAssignmentRepository.findApprovedQuestsWithinWeek(
                studentId, QuestStatus.APPROVED, oneWeekAgo);
        System.out.println("승인 퀘스트 수: " + approvedQuests.size());

        return MyQuestListResponse.builder()
                .activeQuests(convertToQuestItems(activeQuests))
                .expiredQuests(convertToQuestItems(expiredQuests))
                .approvedQuests(convertToQuestItems(approvedQuests))
                .totalCount(activeQuests.size() + expiredQuests.size() + approvedQuests.size())
                .build();
    }

    private List<MyQuestListResponse.QuestItem> convertToQuestItems(List<QuestAssignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    MyQuestListResponse.QuestItem.SubmissionSummary submissionSummary = null;
                    if (assignment.getSubmission() != null) {
                        submissionSummary = MyQuestListResponse.QuestItem.SubmissionSummary.builder()
                                .submittedAt(assignment.getSubmission().getSubmittedAt())
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
                            .approvedAt(assignment.getStatus() == QuestStatus.APPROVED &&
                                    assignment.getSubmission() != null ?
                                    assignment.getSubmission().getSubmittedAt() : null)
                            .comment(assignment.getSubmission() != null ?
                                    assignment.getSubmission().getComment() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 선생님이 특정 학생의 퀘스트 목록 조회
     */
    public MyQuestListResponse getStudentQuests(Integer teacherId, Integer studentId) {
        // 권한 체크: 해당 학생이 선생님의 반에 속해있는지 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        if (student.getClasses() == null || 
            !student.getClasses().getTeacher().getMemberId().equals(teacherId)) {
            throw new CustomException(ErrorCode.CLASS_ACCESS_DENIED);
        }

        // 진행 중인 퀘스트 (ASSIGNED 상태)
        List<QuestStatus> activeStatuses = Arrays.asList(QuestStatus.ASSIGNED);
        List<QuestAssignment> activeQuests = questAssignmentRepository.findByStudentAndStatusIn(
                studentId, activeStatuses);

        return MyQuestListResponse.builder()
                .activeQuests(convertToQuestItems(activeQuests))
                .expiredQuests(new ArrayList<>())
                .approvedQuests(new ArrayList<>())
                .totalCount(activeQuests.size())
                .build();
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
        log.info("[퀘스트 제출] 요청 데이터 - content: {}, attachmentUrl: {}", 
                request.getContent(), request.getAttachmentUrl());
        Submission submission = Submission.builder()
                .questAssignment(assignment)
                .studentContent(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        log.info("[퀘스트 제출] 저장 완료 - submissionId: {}, attachmentUrl: {}", 
                savedSubmission.getSubmissionId(), savedSubmission.getAttachmentUrl());

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
