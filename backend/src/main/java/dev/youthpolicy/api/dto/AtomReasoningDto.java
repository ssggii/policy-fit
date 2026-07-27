package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** contracts/openapi.yaml AtomReasoning — F-004 판정 근거 투명 공개. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AtomReasoningDto(
        @JsonProperty("atom") String atom,
        @JsonProperty("label") String label,
        @JsonProperty("result") String result,
        @JsonProperty("detail") String detail,
        @JsonProperty("source") String source,
        @JsonProperty("year") Integer year) {
}
