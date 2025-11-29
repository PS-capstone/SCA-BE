package com.example.sca_be.domain.notification.service;

import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.auth.repository.StudentRepository;
import com.example.sca_be.domain.groupquest.entity.GroupQuest;
import com.example.sca_be.domain.groupquest.entity.GroupQuestProgress;
import com.example.sca_be.domain.groupquest.entity.GroupQuestStatus;
import com.example.sca_be.domain.groupquest.repository.GroupQuestProgressRepository;
import com.example.sca_be.domain.groupquest.repository.GroupQuestRepository;
import com.example.sca_be.domain.notification.dto.StudentDashboardResponse;
import com.example.sca_be.domain.raid.entity.Raid;
import com.example.sca_be.domain.raid.entity.RaidStatus;
import com.example.sca_be.domain.raid.repository.ContributionRepository;
import com.example.sca_be.domain.raid.repository.RaidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final RaidRepository raidRepository;
    private final ContributionRepository contributionRepository;
    private final GroupQuestRepository groupQuestRepository;
    private final GroupQuestProgressRepository groupQuestProgressRepository;
    private final NotificationService notificationService;

    public StudentDashboardResponse getStudentDashboard(Integer studentId) {
        // 1. 학생 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 2. 학생 정보 구성
        StudentDashboardResponse.StudentInfo studentInfo = buildStudentInfo(student);

        // 3. 알림 정보 (실제 DB 조회)
        StudentDashboardResponse.Notifications notifications = buildNotifications(studentId);

        // 4. 활성 레이드 정보
        StudentDashboardResponse.ActiveRaid activeRaid = null;
        if (student.getClasses() != null) {
            activeRaid = buildActiveRaid(student);
        }

        // 5. 단체 퀘스트 정보
        List<StudentDashboardResponse.GroupQuest> groupQuests = new ArrayList<>();
        if (student.getClasses() != null) {
            groupQuests = buildGroupQuests(student);
        }

        // 6. 최근 활동 정보 (실제 DB 조회)
        List<StudentDashboardResponse.RecentActivity> recentActivities = buildRecentActivities(studentId);

        return StudentDashboardResponse.builder()
                .studentInfo(studentInfo)
                .notifications(notifications)
                .activeRaid(activeRaid)
                .groupQuests(groupQuests)
                .recentActivities(recentActivities)
                .build();
    }

    private StudentDashboardResponse.StudentInfo buildStudentInfo(Student student) {
        String className = student.getClasses() != null ? student.getClasses().getClassName() : "미배정";
        Member member = student.getMember();

        return StudentDashboardResponse.StudentInfo.builder()
                .studentId(student.getMemberId())
                .username(member.getUsername())
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .className(className)
                .coral(student.getCoral() != null ? student.getCoral() : 0)
                .researchData(student.getResearchData() != null ? student.getResearchData() : 0)
                .build();
    }

    private StudentDashboardResponse.Notifications buildNotifications(Integer studentId) {
        // 실제 DB에서 Notice 조회 (최신 10개)
        List<com.example.sca_be.domain.notification.dto.NotificationMessage> allNotifications =
                notificationService.getRecentNotifications(studentId, 10);

        // 타입별로 분리
        // Announcements: 개인 퀘스트 관련
        List<StudentDashboardResponse.Announcement> announcements = allNotifications.stream()
                .filter(n -> n.getType().startsWith("PERSONAL_QUEST"))
                .limit(5)
                .map(n -> StudentDashboardResponse.Announcement.builder()
                        .id(n.getId().intValue())
                        .type(n.getType())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .createdAt(n.getCreatedAt().toString())
                        .timeAgo(n.getTimeAgo())
                        .build())
                .collect(Collectors.toList());

        // Events: 레이드, 단체 퀘스트
        List<StudentDashboardResponse.Event> events = allNotifications.stream()
                .filter(n -> n.getType().startsWith("RAID") || n.getType().startsWith("COMMUNITY_QUEST"))
                .limit(5)
                .map(n -> StudentDashboardResponse.Event.builder()
                        .id(n.getId().intValue())
                        .type(n.getType())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .createdAt(n.getCreatedAt().toString())
                        .timeAgo(n.getTimeAgo())
                        .build())
                .collect(Collectors.toList());

        return StudentDashboardResponse.Notifications.builder()
                .announcements(announcements)
                .events(events)
                .build();
    }

    private StudentDashboardResponse.ActiveRaid buildActiveRaid(Student student) {
        Optional<Raid> activeRaidOpt = raidRepository.findByClasses_ClassIdAndStatus(
                student.getClasses().getClassId(),
                RaidStatus.ACTIVE
        );

        if (activeRaidOpt.isEmpty()) {
            return null;
        }

        Raid raid = activeRaidOpt.get();

        // Boss HP 계산
        Long currentHp = raid.getCurrentBossHp() != null ? raid.getCurrentBossHp() : 0L;
        Long totalHp = raid.getTotalBossHp() != null ? raid.getTotalBossHp() : 1L;
        int percentage = totalHp > 0 ? (int) ((currentHp * 100) / totalHp) : 0;

        StudentDashboardResponse.BossHp bossHp = StudentDashboardResponse.BossHp.builder()
                .current(currentHp)
                .total(totalHp)
                .percentage(percentage)
                .build();

        // 남은 시간 계산
        String remainingTime = calculateRemainingTime(raid.getEndDate());

        // 참여자 수 (공격한 사람들)
        int participants = contributionRepository.countByRaid(raid);

        return StudentDashboardResponse.ActiveRaid.builder()
                .raidId(raid.getRaidId())
                .raidName(raid.getRaidName())
                .template(raid.getBossType() != null ? raid.getBossType().name() : "UNKNOWN")
                .bossHp(bossHp)
                .remainingTime(remainingTime)
                .participants(participants)
                .build();
    }

    private List<StudentDashboardResponse.GroupQuest> buildGroupQuests(Student student) {
        List<GroupQuest> activeQuests = groupQuestRepository.findByClassIdAndStatus(
                student.getClasses().getClassId(),
                GroupQuestStatus.ACTIVE
        );

        return activeQuests.stream().map(quest -> {
            // 전체 학생 수
            int totalCount = student.getClasses().getStudents().size();

            // 완료한 학생 수
            Integer completedCount = groupQuestProgressRepository.countCompletedByGroupQuestId(quest.getGroupQuestId());
            if (completedCount == null) {
                completedCount = 0;
            }

            // 완료율
            int completionRate = totalCount > 0 ? (completedCount * 100 / totalCount) : 0;

            // 내 상태
            Optional<GroupQuestProgress> myProgress = groupQuestProgressRepository
                    .findByGroupQuestIdAndStudentId(quest.getGroupQuestId(), student.getMemberId());
            String myStatus = myProgress.map(p -> p.getIsCompleted() ? "COMPLETED" : "IN_PROGRESS")
                    .orElse("NOT_STARTED");

            // 미완료 학생 목록 (최대 3명)
            List<String> incompleteStudents = quest.getProgressList().stream()
                    .filter(p -> p.getIsCompleted() == null || !p.getIsCompleted())
                    .limit(3)
                    .map(p -> p.getStudent().getMember().getNickname())
                    .collect(Collectors.toList());

            return StudentDashboardResponse.GroupQuest.builder()
                    .questId(quest.getGroupQuestId())
                    .title(quest.getTitle())
                    .description(quest.getContent() != null ? quest.getContent() : "")
                    .completedCount(completedCount)
                    .totalCount(totalCount)
                    .completionRate(completionRate)
                    .myStatus(myStatus)
                    .incompleteStudents(incompleteStudents)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<StudentDashboardResponse.RecentActivity> buildRecentActivities(Integer studentId) {
        // 실제 DB에서 ActionLog 조회 (최신 10개)
        List<com.example.sca_be.domain.notification.dto.ActivityLogMessage> activityLogs =
                notificationService.getRecentActivityLogs(studentId, 10);

        return activityLogs.stream()
                .map(log -> {
                    // 보상 텍스트 생성
                    StringBuilder reward = new StringBuilder();
                    if (log.getRewardCoral() != null && log.getRewardCoral() > 0) {
                        reward.append("+").append(log.getRewardCoral()).append(" 코랄");
                    }
                    if (log.getRewardResearchData() != null && log.getRewardResearchData() > 0) {
                        if (reward.length() > 0) {
                            reward.append(", ");
                        }
                        reward.append("+").append(log.getRewardResearchData()).append(" 탐사데이터");
                    }

                    return StudentDashboardResponse.RecentActivity.builder()
                            .logId(log.getLogId().intValue())
                            .type(log.getType())
                            .icon(log.getIcon())
                            .title(log.getTitle())
                            .description(log.getDescription())
                            .reward(reward.toString())
                            .createdAt(log.getCreatedAt().toString())
                            .timeAgo(log.getTimeAgo())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String calculateRemainingTime(LocalDateTime endDate) {
        if (endDate == null) {
            return "기한 없음";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, endDate);

        if (duration.isNegative()) {
            return "종료됨";
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;

        if (days > 0) {
            return String.format("%d일 %d시간", days, hours);
        } else if (hours > 0) {
            return String.format("%d시간", hours);
        } else {
            long minutes = duration.toMinutes();
            return String.format("%d분", minutes);
        }
    }
}
