package com.example.sca_be.domain.groupquest.dto.request;

import com.example.sca_be.domain.groupquest.entity.GroupQuestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateGroupQuestRequest {
    @NotNull
    @JsonProperty("class_id")
    private Integer classId;

    @NotNull
    private GroupQuestTemplate template;

    @NotNull
    private String title;

    private String content;

    @NotNull
    @JsonProperty("reward_coral")
    private Integer rewardCoral;

    @NotNull
    @JsonProperty("reward_research_data")
    private Integer rewardResearchData;

    @NotNull
    private String deadline;

    @NotNull
    @JsonProperty("required_count")
    private Integer requiredCount;

    @NotNull
    @JsonProperty("total_count")
    private Integer totalCount;
}