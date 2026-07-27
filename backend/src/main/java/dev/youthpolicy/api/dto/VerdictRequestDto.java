package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** contracts/openapi.yaml VerdictRequest. */
public record VerdictRequestDto(
        @JsonProperty("policy_id") @NotBlank(message = "policy_id는 필수입니다.") String policyId,
        @JsonProperty("answers") @NotNull(message = "answers는 필수입니다.") @Valid AnswersDto answers) {
}
