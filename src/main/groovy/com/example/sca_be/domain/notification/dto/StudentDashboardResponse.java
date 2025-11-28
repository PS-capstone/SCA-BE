package com.example.sca_be.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    @JsonProperty("student_info")
    private StudentInfo studentInfo;

    private Notifications notifications;

    @JsonProperty("active_raid")
    private ActiveRaid activeRaid;

    @JsonProperty("group_quests")
    private List<GroupQuest> groupQuests;

    @JsonProperty("recent_activities")
    private List<RecentActivity> recentActivities;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        @JsonProperty("student_id")
        private Integer studentId;

        private String username;

        @JsonProperty("real_name")
        private String realName;

        private String nickname;

        @JsonProperty("class_name")
        private String className;

        private Integer coral;

        @JsonProperty("research_data")
        private Integer researchData;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notifications {
        private List<Announcement> announcements;
        private List<Event> events;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Announcement {
        private Integer id;
        private String type;
        private String title;
        private String content;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("time_ago")
        private String timeAgo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        private Integer id;
        private String type;
        private String title;
        private String content;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("time_ago")
        private String timeAgo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveRaid {
        @JsonProperty("raid_id")
        private Integer raidId;

        @JsonProperty("raid_name")
        private String raidName;

        private String template;

        @JsonProperty("boss_hp")
        private BossHp bossHp;

        @JsonProperty("remaining_time")
        private String remainingTime;

        private Integer participants;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BossHp {
        private Long current;
        private Long total;
        private Integer percentage;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupQuest {
        @JsonProperty("quest_id")
        private Integer questId;

        private String title;
        private String description;

        @JsonProperty("completed_count")
        private Integer completedCount;

        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("completion_rate")
        private Integer completionRate;

        @JsonProperty("my_status")
        private String myStatus;

        @JsonProperty("incomplete_students")
        private List<String> incompleteStudents;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        @JsonProperty("log_id")
        private Integer logId;

        private String type;
        private String icon;
        private String title;
        private String description;
        private String reward;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("time_ago")
        private String timeAgo;
    }
}
