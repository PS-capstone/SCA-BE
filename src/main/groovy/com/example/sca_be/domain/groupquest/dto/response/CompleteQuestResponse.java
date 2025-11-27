package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CompleteQuestResponse {
    @JsonProperty("quest_id")
    private Integer questId;

    private String title;
    private String status;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("rewards_granted")
    private RewardsGranted rewardsGranted;

    @JsonProperty("completed_students_id")
    private List<Integer> completedStudentsId;

    @Getter
    @Builder
    public static class RewardsGranted {
        @JsonProperty("reward_per_student")
        private RewardPerStudent rewardPerStudent;
    }

    @Getter
    @Builder
    public static class RewardPerStudent {
        private Integer coral;

        @JsonProperty("research_data")
        private Integer researchData;
    }
}
