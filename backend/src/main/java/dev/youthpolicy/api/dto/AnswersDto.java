package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

/** contracts/openapi.yaml VerdictRequest.answers. 각 필드는 개별적으로 선택적이다(스키마상 required 없음). */
public record AnswersDto(
        @JsonProperty("age") @Valid AnswerIntDto age,
        @JsonProperty("housing_none") @Valid AnswerBoolDto housingNone,
        @JsonProperty("income_self_monthly_krw") @Valid AnswerApproxIntDto incomeSelfMonthlyKrw) {
}
