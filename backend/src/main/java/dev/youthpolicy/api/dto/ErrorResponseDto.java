package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** contracts/openapi.yaml ErrorResponse. */
public record ErrorResponseDto(@JsonProperty("message") String message) {
}
