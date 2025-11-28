package com.example.sca_be.domain.groupquest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckStudentRequest {
    @NotNull
    @JsonProperty("is_completed")
    private Boolean isCompleted;
}
