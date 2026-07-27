package dev.youthpolicy.domain.atom;

/** 불리언 답변 입력 변수(예: 무주택 여부). known=false면 '모름' — value는 무시된다. */
public record AnswerBool(boolean known, Boolean value) {
}
