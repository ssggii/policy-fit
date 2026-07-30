package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/** contracts/openapi.yaml AnswerString. */
public record AnswerStringDto(
        @JsonProperty("known") @NotNull(message = "known은 필수입니다.") Boolean known,
        @JsonProperty("value") String value) {
}
