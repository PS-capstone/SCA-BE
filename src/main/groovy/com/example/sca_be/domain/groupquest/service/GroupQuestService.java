package com.example.sca_be.domain.groupquest.service;

import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.entity.Teacher;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.auth.repository.TeacherRepository;
import com.example.sca_be.domain.classroom.entity.Classes;
import com.example.sca_be.domain.classroom.repository.ClassesRepository;
import com.example.sca_be.domain.groupquest.dto.request.CreateGroupQuestRequest;
import com.example.sca_be.domain.groupquest.dto.response.*;
import com.example.sca_be.domain.groupquest.entity.GroupQuest;
import com.example.sca_be.domain.groupquest.entity.GroupQuestProgress;
import com.example.sca_be.domain.groupquest.entity.GroupQuestStatus;
import com.example.sca_be.domain.groupquest.entity.GroupQuestTemplate;
import com.example.sca_be.domain.groupquest.repository.GroupQuestProgressRepository;
import com.example.sca_be.domain.groupquest.repository.GroupQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupQuestService {

    private final GroupQuestRepository groupQuestRepository;
    private final GroupQuestProgressRepository progressRepository;
    private final ClassesRepository classesRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final com.example.sca_be.domain.notification.service.NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public ClassInfoResponse getClassInfo(Integer classId, Integer teacherId) {
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다."));

        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getClasses() != null && s.getClasses().getClassId().equals(classId))
                .collect(Collectors.toList());

        List<ClassInfoResponse.TemplateInfo> templates = List.of(
                ClassInfoResponse.TemplateInfo.builder()
                        .code("ATTENDANCE")
                        .name("출석 체크")
                        .description("수업 출석 확인")
                        .build(),
                ClassInfoResponse.TemplateInfo.builder()
                        .code("ASSIGNMENT")
                        .name("과제 제출")
                        .description("숙제 제출 확인")
                        .build(),
                ClassInfoResponse.TemplateInfo.builder()
                        .code("PARTICIPATION")
                        .name("수업 참여")
                        .description("적극적인 수업 참여")
                        .build(),
                ClassInfoResponse.TemplateInfo.builder()
                        .code("EXAM")
                        .name("학교 시험 참수")
                        .description("학교 시험 참여 일력")
                        .build(),
                ClassInfoResponse.TemplateInfo.builder()
                        .code("OTHER")
                        .name("기타")
                        .description("기타 퀘스트")
                        .build()
        );

        return ClassInfoResponse.builder()
                .classId(classes.getClassId())
                .className(classes.getClassName())
                .totalStudents(students.size())
                .templates(templates)
                .build();
    }

    @Transactional
    public GroupQuestResponse createGroupQuest(CreateGroupQuestRequest request, Integer teacherId) {
        if (request.getRequiredCount() > request.getTotalCount()) {
            throw new IllegalArgumentException("완료 필요 학생 수는 전체 학생 수를 초과할 수 없습니다.");
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선생님입니다."));

        Classes classes = classesRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다."));

        LocalDateTime deadline = LocalDateTime.parse(request.getDeadline() + "T23:59:59");

        GroupQuest groupQuest = GroupQuest.builder()
                .teacher(teacher)
                .classes(classes)
                .title(request.getTitle())
                .content(request.getContent())
                .status(GroupQuestStatus.ACTIVE)
                .rewardCoral(request.getRewardCoral())
                .rewardResearchData(request.getRewardResearchData())
                .endDate(deadline)
                .type(request.getTemplate())
                .build();

        GroupQuest savedQuest = groupQuestRepository.save(groupQuest);

        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getClasses() != null && s.getClasses().getClassId().equals(request.getClassId()))
                .collect(Collectors.toList());

        for (Student student : students) {
            GroupQuestProgress progress = GroupQuestProgress.builder()
                    .groupQuest(savedQuest)
                    .student(student)
                    .isCompleted(false)
                    .build();
            progressRepository.save(progress);

            // 학급 전체 학생에게 단체 퀘스트 할당 공지
            notificationService.createAndBroadcastNotification(
                    student,
                    com.example.sca_be.domain.notification.entity.NoticeType.COMMUNITY_QUEST_ASSIGNED,
                    "새로운 단체 퀘스트가 부여되었습니다",
                    savedQuest.getTitle(),
                    null,
                    savedQuest,
                    null
            );
        }

        Integer completedCount = progressRepository.countCompletedByGroupQuestId(savedQuest.getGroupQuestId());

        return GroupQuestResponse.builder()
                .groupQuestId(savedQuest.getGroupQuestId())
                .classId(classes.getClassId())
                .className(classes.getClassName())
                .template(savedQuest.getType().name())
                .title(savedQuest.getTitle())
                .content(savedQuest.getContent())
                .status(savedQuest.getStatus().name())
                .rewardCoral(savedQuest.getRewardCoral())
                .deadline(savedQuest.getEndDate().format(DATE_FORMATTER))
                .completionStatus(GroupQuestResponse.CompletionStatus.builder()
                        .completedCount(completedCount)
                        .requiredCount(request.getRequiredCount())
                        .totalCount(request.getTotalCount())
                        .isAchievable(completedCount >= request.getRequiredCount())
                        .build())
                .createdAt(savedQuest.getCreatedAt().format(DATETIME_FORMATTER))
                .build();
    }

    public GroupQuestListResponse getGroupQuestList(Integer classId, GroupQuestStatus status) {
        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다."));

        List<GroupQuest> quests;
        if (status != null) {
            quests = groupQuestRepository.findByClassIdAndStatus(classId, status);
        } else {
            quests = groupQuestRepository.findByClassId(classId);
        }

        List<GroupQuestListResponse.QuestSummary> questSummaries = quests.stream()
                .map(quest -> {
                    Integer completedCount = progressRepository.countCompletedByGroupQuestId(quest.getGroupQuestId());
                    Integer totalCount = progressRepository.findByGroupQuestId(quest.getGroupQuestId()).size();
                    Integer requiredCount = (int) Math.ceil(totalCount * 0.5);

                    return GroupQuestListResponse.QuestSummary.builder()
                            .questId(quest.getGroupQuestId())
                            .template(quest.getType().name())
                            .title(quest.getTitle())
                            .content(quest.getContent())
                            .status(quest.getStatus().name())
                            .rewardCoral(quest.getRewardCoral())
                            .deadline(quest.getEndDate().format(DATETIME_FORMATTER).replace("T", " ").replace("Z", ""))
                            .completionStatus(GroupQuestListResponse.CompletionStatusInfo.builder()
                                    .completedCount(completedCount)
                                    .requiredCount(requiredCount)
                                    .totalCount(totalCount)
                                    .completionRate(totalCount > 0 ? (completedCount * 100 / totalCount) : 0)
                                    .isAchievable(completedCount >= requiredCount)
                                    .build())
                            .createdAt(quest.getCreatedAt().format(DATETIME_FORMATTER))
                            .build();
                })
                .collect(Collectors.toList());

        return GroupQuestListResponse.builder()
                .classId(classes.getClassId())
                .className(classes.getClassName())
                .quests(questSummaries)
                .totalCount(questSummaries.size())
                .build();
    }

    public GroupQuestDetailResponse getGroupQuestDetail(Integer questId) {
        GroupQuest quest = groupQuestRepository.findByIdWithProgress(questId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단체 퀘스트입니다."));

        List<GroupQuestProgress> progressList = progressRepository.findByGroupQuestId(questId);
        Integer completedCount = progressList.stream()
                .filter(GroupQuestProgress::getIsCompleted)
                .collect(Collectors.toList())
                .size();
        Integer totalCount = progressList.size();
        Integer requiredCount = (int) Math.ceil(totalCount * 0.5);

        List<GroupQuestDetailResponse.StudentProgressInfo> studentInfos = progressList.stream()
                .map(progress -> GroupQuestDetailResponse.StudentProgressInfo.builder()
                        .studentId(progress.getStudent().getMemberId())
                        .studentName(progress.getStudent().getMember().getRealName())
                        .className(quest.getClasses().getClassName())
                        .isCompleted(progress.getIsCompleted())
                        .completedAt(progress.getCompletedAt() != null ?
                                progress.getCompletedAt().format(DATETIME_FORMATTER) : null)
                        .build())
                .collect(Collectors.toList());

        return GroupQuestDetailResponse.builder()
                .questId(quest.getGroupQuestId())
                .classId(quest.getClasses().getClassId())
                .className(quest.getClasses().getClassName())
                .template(quest.getType().name())
                .title(quest.getTitle())
                .content(quest.getContent())
                .status(quest.getStatus().name())
                .rewardCoral(quest.getRewardCoral())
                .deadline(quest.getEndDate().format(DATE_FORMATTER))
                .completionStatus(GroupQuestDetailResponse.CompletionStatusInfo.builder()
                        .completedCount(completedCount)
                        .requiredCount(requiredCount)
                        .totalCount(totalCount)
                        .completionRate(totalCount > 0 ? (completedCount * 100 / totalCount) : 0)
                        .isAchievable(completedCount >= requiredCount)
                        .build())
                .completionCondition(GroupQuestDetailResponse.CompletionCondition.builder()
                        .description(requiredCount + "명 이상 완료 시 조건 달성")
                        .build())
                .students(studentInfos)
                .createdAt(quest.getCreatedAt().format(DATETIME_FORMATTER))
                .build();
    }

    @Transactional
    public CheckStudentResponse checkStudent(Integer questId, Integer studentId, Boolean isCompleted) {
        GroupQuest quest = groupQuestRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단체 퀘스트입니다."));

        if (quest.getStatus() == GroupQuestStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 단체 퀘스트는 수정할 수 없습니다.");
        }

        GroupQuestProgress progress = progressRepository.findByGroupQuestIdAndStudentId(questId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));

        if (isCompleted) {
            progress.completeProgress();
        }
        progressRepository.save(progress);

        Integer completedCount = progressRepository.countCompletedByGroupQuestId(questId);
        Integer totalCount = progressRepository.findByGroupQuestId(questId).size();
        Integer requiredCount = (int) Math.ceil(totalCount * 0.5);
        Boolean isAchievable = completedCount >= requiredCount;

        return CheckStudentResponse.builder()
                .questId(questId)
                .studentId(studentId)
                .studentName(progress.getStudent().getMember().getRealName())
                .isCompleted(progress.getIsCompleted())
                .checkedAt(progress.getCompletedAt() != null ?
                        progress.getCompletedAt().format(DATETIME_FORMATTER) : null)
                .questStatus(CheckStudentResponse.QuestStatusInfo.builder()
                        .currentStatus(quest.getStatus().name())
                        .completedCount(completedCount)
                        .requiredCount(requiredCount)
                        .isAchievable(isAchievable)
                        .build())
                .build();
    }

    @Transactional
    public CheckStudentResponse uncheckStudent(Integer questId, Integer studentId) {
        GroupQuest quest = groupQuestRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단체 퀘스트입니다."));

        if (quest.getStatus() == GroupQuestStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 단체 퀘스트는 수정할 수 없습니다.");
        }

        GroupQuestProgress progress = progressRepository.findByGroupQuestIdAndStudentId(questId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));

        progress.uncompleteProgress();
        progressRepository.save(progress);

        Integer completedCount = progressRepository.countCompletedByGroupQuestId(questId);
        Integer totalCount = progressRepository.findByGroupQuestId(questId).size();
        Integer requiredCount = (int) Math.ceil(totalCount * 0.5);

        return CheckStudentResponse.builder()
                .questId(questId)
                .studentId(studentId)
                .studentName(progress.getStudent().getMember().getRealName())
                .isCompleted(false)
                .questStatus(CheckStudentResponse.QuestStatusInfo.builder()
                        .currentStatus(quest.getStatus().name())
                        .completedCount(completedCount)
                        .requiredCount(requiredCount)
                        .isAchievable(completedCount >= requiredCount)
                        .build())
                .build();
    }

    @Transactional
    public CompleteQuestResponse completeQuest(Integer questId) {
        GroupQuest quest = groupQuestRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단체 퀘스트입니다."));

        if (quest.getStatus() == GroupQuestStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 단체 퀘스트입니다.");
        }

        List<GroupQuestProgress> progressList = progressRepository.findByGroupQuestId(questId);
        List<Integer> completedStudentIds = progressList.stream()
                .filter(GroupQuestProgress::getIsCompleted)
                .map(p -> p.getStudent().getMemberId())
                .collect(Collectors.toList());

        Integer completedCount = completedStudentIds.size();
        Integer totalCount = progressList.size();
        Integer requiredCount = (int) Math.ceil(totalCount * 0.5);

        GroupQuestStatus newStatus = (completedCount >= requiredCount) ?
                GroupQuestStatus.COMPLETED : GroupQuestStatus.FAILED;

        quest.updateStatus(newStatus);
        groupQuestRepository.save(quest);

        // 학급 전체 학생에게 공지 전송
        List<Student> allStudents = studentRepository.findAll().stream()
                .filter(s -> s.getClasses() != null && s.getClasses().getClassId().equals(quest.getClasses().getClassId()))
                .collect(Collectors.toList());

        if (newStatus == GroupQuestStatus.COMPLETED) {
            // 성공: 전체 학생에게 완료 공지
            for (Student student : allStudents) {
                notificationService.createAndBroadcastNotification(
                        student,
                        com.example.sca_be.domain.notification.entity.NoticeType.COMMUNITY_QUEST_FINISHED,
                        "단체 퀘스트가 완료되었습니다",
                        quest.getTitle(),
                        null,
                        quest,
                        null
                );
            }

            // 수행한 학생들에게만 보상 지급 및 활동로그 생성
            List<GroupQuestProgress> completedProgress = progressList.stream()
                    .filter(GroupQuestProgress::getIsCompleted)
                    .collect(Collectors.toList());

            for (GroupQuestProgress progress : completedProgress) {
                Student student = progress.getStudent();
                // 보상 지급
                student.addCoral(quest.getRewardCoral());
                student.addResearchData(quest.getRewardResearchData());
                studentRepository.save(student);

                // 활동로그 생성 및 웹소켓 전송
                notificationService.createAndBroadcastActivityLog(
                        student,
                        com.example.sca_be.domain.notification.entity.ActionLogType.GROUP_QUEST_COMPLETE,
                        quest.getTitle() + " 완료",
                        quest.getRewardCoral(),
                        quest.getRewardResearchData(),
                        null,
                        quest,
                        null
                );
            }
        } else {
            // 실패: 전체 학생에게 실패 공지
            for (Student student : allStudents) {
                notificationService.createAndBroadcastNotification(
                        student,
                        com.example.sca_be.domain.notification.entity.NoticeType.COMMUNITY_QUEST_REJECTED,
                        "단체 퀘스트가 종료되었습니다",
                        quest.getTitle() + " - 목표 미달",
                        null,
                        quest,
                        null
                );
            }
        }

        return CompleteQuestResponse.builder()
                .questId(quest.getGroupQuestId())
                .title(quest.getTitle())
                .status(newStatus.name())
                .completedAt(LocalDateTime.now().format(DATETIME_FORMATTER))
                .rewardsGranted(CompleteQuestResponse.RewardsGranted.builder()
                        .rewardPerStudent(CompleteQuestResponse.RewardPerStudent.builder()
                                .coral(quest.getRewardCoral())
                                .researchData(quest.getRewardResearchData())
                                .build())
                        .build())
                .completedStudentsId(completedStudentIds)
                .build();
    }

    public StudentQuestDetailResponse getStudentQuestDetail(Integer questId, Integer studentId) {
        GroupQuest quest = groupQuestRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단체 퀘스트입니다."));

        if (quest.getStatus() != GroupQuestStatus.ACTIVE) {
            throw new IllegalStateException("진행 중이 아닌 퀘스트는 조회할 수 없습니다.");
        }

        List<GroupQuestProgress> progressList = progressRepository.findByGroupQuestId(questId);
        GroupQuestProgress myProgress = progressRepository.findByGroupQuestIdAndStudentId(questId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 퀘스트에 참여하지 않은 학생입니다."));

        Integer completedCount = progressList.stream()
                .filter(GroupQuestProgress::getIsCompleted)
                .collect(Collectors.toList())
                .size();
        Integer totalCount = progressList.size();
        Integer requiredCount = (int) Math.ceil(totalCount * 0.5);

        List<StudentQuestDetailResponse.CompletedStudentInfo> completedStudents = progressList.stream()
                .filter(GroupQuestProgress::getIsCompleted)
                .map(p -> StudentQuestDetailResponse.CompletedStudentInfo.builder()
                        .studentId(p.getStudent().getMemberId())
                        .studentName(p.getStudent().getMember().getRealName())
                        .completedAt(p.getCompletedAt().format(DATETIME_FORMATTER))
                        .build())
                .collect(Collectors.toList());

        List<StudentQuestDetailResponse.IncompleteStudentInfo> incompleteStudents = progressList.stream()
                .filter(p -> !p.getIsCompleted())
                .map(p -> StudentQuestDetailResponse.IncompleteStudentInfo.builder()
                        .studentId(p.getStudent().getMemberId())
                        .studentName(p.getStudent().getMember().getRealName())
                        .statusText("미완료")
                        .build())
                .collect(Collectors.toList());

        return StudentQuestDetailResponse.builder()
                .questId(quest.getGroupQuestId())
                .template(quest.getType().name())
                .title(quest.getTitle())
                .content(quest.getContent())
                .status(quest.getStatus().name())
                .rewardCoral(quest.getRewardCoral())
                .rewardResearchData(quest.getRewardResearchData())
                .deadline(quest.getEndDate().format(DATE_FORMATTER))
                .createdAt(quest.getCreatedAt().format(DATETIME_FORMATTER))
                .completionStatus(StudentQuestDetailResponse.CompletionStatusInfo.builder()
                        .completedCount(completedCount)
                        .requiredCount(requiredCount)
                        .totalCount(totalCount)
                        .completionRate(totalCount > 0 ? (completedCount * 100 / totalCount) : 0)
                        .isAchievable(completedCount >= requiredCount)
                        .completionConditionText(requiredCount + "명 이상 완료 시 조건 달성")
                        .build())
                .myStatus(StudentQuestDetailResponse.MyStatusInfo.builder()
                        .studentId(myProgress.getStudent().getMemberId())
                        .studentName(myProgress.getStudent().getMember().getRealName())
                        .isCompleted(myProgress.getIsCompleted())
                        .completedAt(myProgress.getCompletedAt() != null ?
                                myProgress.getCompletedAt().format(DATETIME_FORMATTER) : null)
                        .statusText(myProgress.getIsCompleted() ? "완료" : "미완료")
                        .build())
                .completedStudents(completedStudents)
                .incompleteStudents(incompleteStudents)
                .build();
    }
}
