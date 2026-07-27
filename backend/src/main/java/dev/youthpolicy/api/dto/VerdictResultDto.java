package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * contracts/openapi.yaml VerdictResult. application은 nullable — F-006: ineligible엔 null/생략.
 * NON_NULL로 null일 때 필드 자체를 생략한다.
 */
public record VerdictResultDto(
        @JsonProperty("policy_id") String policyId,
        @JsonProperty("verdict") VerdictDto verdict,
        @JsonProperty("reasoning") List<AtomReasoningDto> reasoning,
        @JsonProperty("application") @JsonInclude(JsonInclude.Include.NON_NULL) ApplicationDto application) {
}
