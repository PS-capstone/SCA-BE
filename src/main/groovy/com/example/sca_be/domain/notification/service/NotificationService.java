package com.example.sca_be.domain.notification.service;

import com.example.sca_be.domain.auth.entity.Student;
import com.example.sca_be.domain.groupquest.entity.GroupQuest;
import com.example.sca_be.domain.notification.dto.ActivityLogMessage;
import com.example.sca_be.domain.notification.dto.NotificationMessage;
import com.example.sca_be.domain.notification.entity.ActionLog;
import com.example.sca_be.domain.notification.entity.ActionLogType;
import com.example.sca_be.domain.notification.entity.Notice;
import com.example.sca_be.domain.notification.entity.NoticeType;
import com.example.sca_be.domain.notification.repository.ActionLogRepository;
import com.example.sca_be.domain.notification.repository.NoticeRepository;
import com.example.sca_be.domain.personalquest.entity.QuestAssignment;
import com.example.sca_be.domain.raid.entity.Raid;
import com.example.sca_be.global.util.TimeAgoUtil;
import com.example.sca_be.global.websocket.ActivityLogBroadcaster;
import com.example.sca_be.global.websocket.NotificationBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NoticeRepository noticeRepository;
    private final ActionLogRepository actionLogRepository;
    private final NotificationBroadcaster notificationBroadcaster;
    private final ActivityLogBroadcaster activityLogBroadcaster;

    /**
     * Get all notifications for a student
     * @param studentId The student's member ID
     * @return List of notification messages
     */
    public List<NotificationMessage> getNotifications(Integer studentId) {
        List<Notice> notices = noticeRepository.findByStudent_MemberIdOrderByCreatedAtDesc(studentId);
        return notices.stream()
                .map(this::convertToNotificationMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get recent N notifications for a student
     * @param studentId The student's member ID
     * @param limit Maximum number of notifications
     * @return List of notification messages
     */
    public List<NotificationMessage> getRecentNotifications(Integer studentId, int limit) {
        List<Notice> notices = noticeRepository.findTopNByStudentId(studentId, limit);
        return notices.stream()
                .map(this::convertToNotificationMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get all activity logs for a student
     * @param studentId The student's member ID
     * @return List of activity log messages
     */
    public List<ActivityLogMessage> getActivityLogs(Integer studentId) {
        List<ActionLog> logs = actionLogRepository.findByStudent_MemberIdOrderByCreatedAtDesc(studentId);
        return logs.stream()
                .map(this::convertToActivityLogMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get activity logs for a student filtered by type
     * @param studentId The student's member ID
     * @param type The action log type (can be null for all types)
     * @return List of activity log messages
     */
    public List<ActivityLogMessage> getActivityLogsByType(Integer studentId, ActionLogType type) {
        List<ActionLog> logs;
        if (type != null) {
            logs = actionLogRepository.findByStudent_MemberIdAndActionLogTypeOrderByCreatedAtDesc(studentId, type);
        } else {
            logs = actionLogRepository.findByStudent_MemberIdOrderByCreatedAtDesc(studentId);
        }
        return logs.stream()
                .map(this::convertToActivityLogMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get recent N activity logs for a student
     * @param studentId The student's member ID
     * @param limit Maximum number of logs
     * @return List of activity log messages
     */
    public List<ActivityLogMessage> getRecentActivityLogs(Integer studentId, int limit) {
        List<ActionLog> logs = actionLogRepository.findTopNByStudentId(studentId, limit);
        return logs.stream()
                .map(this::convertToActivityLogMessage)
                .collect(Collectors.toList());
    }

    /**
     * Create and broadcast a notification to a student
     * @param student The student to notify
     * @param noticeType The type of notification
     * @param title The notification title
     * @param content The notification content
     * @param questAssignment Optional quest assignment reference
     * @param groupQuest Optional group quest reference
     * @param raid Optional raid reference
     */
    @Transactional
    public void createAndBroadcastNotification(
            Student student,
            NoticeType noticeType,
            String title,
            String content,
            QuestAssignment questAssignment,
            GroupQuest groupQuest,
            Raid raid
    ) {
        // Save to database
        Notice notice = Notice.builder()
                .noticeType(noticeType)
                .student(student)
                .title(title)
                .content(content)
                .questAssignment(questAssignment)
                .groupQuest(groupQuest)
                .raid(raid)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // Broadcast via WebSocket
        NotificationMessage message = convertToNotificationMessage(savedNotice);
        notificationBroadcaster.broadcast(student.getMemberId(), message);

        log.info("Created and broadcasted notification to student {}: {}", student.getMemberId(), title);
    }

    /**
     * Create and broadcast an activity log to a student
     * @param student The student
     * @param actionLogType The type of action
     * @param logMessage The log message
     * @param changeCoral Coral reward (can be null)
     * @param changeResearch Research data reward (can be null)
     * @param questAssignment Optional quest assignment reference
     * @param groupQuest Optional group quest reference
     * @param raid Optional raid reference
     */
    @Transactional
    public void createAndBroadcastActivityLog(
            Student student,
            ActionLogType actionLogType,
            String logMessage,
            Integer changeCoral,
            Integer changeResearch,
            QuestAssignment questAssignment,
            GroupQuest groupQuest,
            Raid raid
    ) {
        // Save to database
        ActionLog actionLog = ActionLog.builder()
                .student(student)
                .actionLogType(actionLogType)
                .logMessage(logMessage)
                .changeCoral(changeCoral)
                .changeResearch(changeResearch)
                .questAssignment(questAssignment)
                .groupQuest(groupQuest)
                .raid(raid)
                .build();
        ActionLog savedLog = actionLogRepository.save(actionLog);

        // Broadcast via WebSocket
        ActivityLogMessage message = convertToActivityLogMessage(savedLog);
        activityLogBroadcaster.broadcast(student.getMemberId(), message);

        log.info("Created and broadcasted activity log to student {}: {}", student.getMemberId(), logMessage);
    }

    /**
     * Convert Notice entity to NotificationMessage DTO
     */
    private NotificationMessage convertToNotificationMessage(Notice notice) {
        return NotificationMessage.builder()
                .id(notice.getNoticeId())
                .type(notice.getNoticeType().name())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .timeAgo(TimeAgoUtil.calculate(notice.getCreatedAt()))
                .build();
    }

    /**
     * Convert ActionLog entity to ActivityLogMessage DTO
     */
    private ActivityLogMessage convertToActivityLogMessage(ActionLog actionLog) {
        // Determine icon based on action type
        String icon = determineIcon(actionLog.getActionLogType());

        // Determine title based on action type
        String title = determineTitle(actionLog.getActionLogType());

        return ActivityLogMessage.builder()
                .logId(actionLog.getLogId())
                .type(actionLog.getActionLogType().name())
                .icon(icon)
                .title(title)
                .description(actionLog.getLogMessage() != null ? actionLog.getLogMessage() : "")
                .rewardCoral(actionLog.getChangeCoral() != null ? actionLog.getChangeCoral() : 0)
                .rewardResearchData(actionLog.getChangeResearch() != null ? actionLog.getChangeResearch() : 0)
                .createdAt(actionLog.getCreatedAt())
                .timeAgo(TimeAgoUtil.calculate(actionLog.getCreatedAt()))
                .build();
    }

    /**
     * Determine icon based on action log type
     */
    private String determineIcon(ActionLogType type) {
        switch (type) {
            case REWARD_RECEIVED:
                return "C"; // Complete
            case GROUP_QUEST_COMPLETE:
                return "C"; // Complete
            case RAID_REWARD_RECEIVED:
                return "E"; // Event
            default:
                return "C";
        }
    }

    /**
     * Determine title based on action log type
     */
    private String determineTitle(ActionLogType type) {
        switch (type) {
            case REWARD_RECEIVED:
                return "퀘스트 완료";
            case GROUP_QUEST_COMPLETE:
                return "단체 퀘스트 완료";
            case RAID_REWARD_RECEIVED:
                return "레이드 보상 수령";
            default:
                return "활동";
        }
    }
}
