package dev.youthpolicy.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * contracts/verdict.schema.json embed. unknown_reasons는 needs_review일 때만 존재해야 하므로
 * null이면 직렬화에서 아예 생략한다(빈 배열 []도 스키마 minItems:1 위반이라 허용 안 함).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerdictDto(
        @JsonProperty("state") String state,
        @JsonProperty("unknown_reasons") List<String> unknownReasons) {
}
