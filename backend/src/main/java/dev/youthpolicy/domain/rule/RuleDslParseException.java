package dev.youthpolicy.domain.rule;

/** Rule DSL JSON을 RuleNode 트리로 변환하는 중 발생한 형식 오류. */
public class RuleDslParseException extends RuntimeException {

    public RuleDslParseException(String message) {
        super(message);
    }
}
