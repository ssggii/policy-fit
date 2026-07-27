package dev.youthpolicy.domain.atom;

/** 정수 답변 입력 변수(예: 나이). known=false면 '모름' — value는 무시된다. */
public record AnswerInt(boolean known, Integer value) {
}
