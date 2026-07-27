package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/** contracts/openapi.yaml AnswerBool. */
public record AnswerBoolDto(
        @JsonProperty("known") @NotNull(message = "known은 필수입니다.") Boolean known,
        @JsonProperty("value") Boolean value) {
}
