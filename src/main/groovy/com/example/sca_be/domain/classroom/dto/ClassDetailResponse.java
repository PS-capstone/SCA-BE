package com.example.sca_be.domain.classroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ClassDetailResponse {


    @JsonProperty("class_id")
    private Integer classId;
    @JsonProperty("class_name")
    private String className;
    @JsonProperty("invite_code")
    private String inviteCode;
    @JsonProperty("ongoing_group_quests")
    private List<OngoingGroupQuest> ongoingGroupQuests;
    @JsonProperty("ongoing_raid")
    private OngoingRaid ongoingRaid;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OngoingGroupQuest {

        @JsonProperty("quest_id")
        private Integer questId;
        @JsonProperty("title")
        private String title;
        @JsonProperty("progress")
        private QuestProgress progress;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class QuestProgress {
        @JsonProperty("completed")
        private Integer completed;
        @JsonProperty("required")
        private Integer required;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OngoingRaid {
        @JsonProperty("raid_id")
        private Integer raidId;
        @JsonProperty("title")
        private String title;
        @JsonProperty("boss_hp")
        private BossHp bossHp;
        @JsonProperty("participants")
        private Integer participants;
        @JsonProperty("end_date")
        private String endDate;
        @JsonProperty("status")
        private String status;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BossHp {
        @JsonProperty("current")
        private Long current;
        @JsonProperty("total")
        private Long total;
        @JsonProperty("percentage")
        private Integer percentage;
    }
}
