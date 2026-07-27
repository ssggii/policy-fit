package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** contracts/openapi.yaml VerdictResult.application — F-006/F-007. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationDto(
        @JsonProperty("url") String url,
        @JsonProperty("selection_method") String selectionMethod,
        @JsonProperty("period") String period) {
}
