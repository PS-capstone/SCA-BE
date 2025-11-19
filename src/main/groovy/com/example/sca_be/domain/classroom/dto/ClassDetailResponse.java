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
        private String title;
        private QuestProgress progress;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class QuestProgress {
        private Integer completed;
        private Integer required;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OngoingRaid {
        @JsonProperty("raid_id")
        private Integer raidId;
        private String title;
        @JsonProperty("boss_hp")
        private BossHp bossHp;
        private Integer participants;
        @JsonProperty("end_date")
        private String endDate;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BossHp {
        private Integer current;
        private Integer total;
        private Integer percentage;
    }
}
