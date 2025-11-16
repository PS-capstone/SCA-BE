package com.example.sca_be.domain.gacha.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GachaInfoResponse {
    @JsonProperty("student_coral")
    private Integer studentCoral;

    @JsonProperty("gacha_cost")
    private Integer gachaCost;
}
