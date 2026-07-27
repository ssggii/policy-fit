package dev.youthpolicy.golden;

import dev.youthpolicy.api.dto.AnswersDto;

import java.util.List;

/**
 * 골든셋 케이스 1건. {@code answers}는 실제 API 요청 answers 객체와 동일한 형태를 재사용해
 * (RequestToAnswersMapper까지 그대로 태워) 실제 요청 형태에 가깝게 검증한다.
 */
public record GoldenCase(
        String caseId,
        AnswersDto answers,
        String expectedState,
        List<String> expectedUnknownReasons,
        String source) {

    @Override
    public String toString() {
        return caseId;
    }
}
