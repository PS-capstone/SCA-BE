package com.example.sca_be.domain.personalquest.dto;

import com.example.sca_be.domain.personalquest.entity.QuestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyQuestListResponse {
    private String status;

    private List<QuestItem> quests;

    @JsonProperty("total_count")
    private Integer totalCount;

    @Getter
    @Builder
    public static class QuestItem {
        @JsonProperty("assignment_id")
        private Integer assignmentId;

        @JsonProperty("quest_id")
        private Integer questId;

        private String title;

        @JsonProperty("teacher_content")
        private String teacherContent;

        @JsonProperty("reward_coral_personal")
        private Integer rewardCoralPersonal;

        @JsonProperty("reward_research_data_personal")
        private Integer rewardResearchDataPersonal;

        private QuestStatus status;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        private SubmissionSummary submission;

        @Getter
        @Builder
        public static class SubmissionSummary {
            @JsonProperty("submitted_at")
            private LocalDateTime submittedAt;

            private String comment;
        }
    }
}
