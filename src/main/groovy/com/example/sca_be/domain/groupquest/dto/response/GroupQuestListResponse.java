package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupQuestListResponse {
    @JsonProperty("class_id")
    private Integer classId;

    @JsonProperty("class_name")
    private String className;

    private List<QuestSummary> quests;

    @JsonProperty("total_count")
    private Integer totalCount;

    @Getter
    @Builder
    public static class QuestSummary {
        @JsonProperty("quest_id")
        private Integer questId;

        private String template;
        private String title;
        private String content;
        private String status;

        @JsonProperty("reward_coral")
        private Integer rewardCoral;

        private String deadline;

        @JsonProperty("completion_status")
        private CompletionStatusInfo completionStatus;

        @JsonProperty("created_at")
        private String createdAt;
    }

    @Getter
    @Builder
    public static class CompletionStatusInfo {
        @JsonProperty("completed_count")
        private Integer completedCount;

        @JsonProperty("required_count")
        private Integer requiredCount;

        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("completion_rate")
        private Integer completionRate;

        @JsonProperty("is_achievable")
        private Boolean isAchievable;
    }
}
