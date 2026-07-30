package dev.youthpolicy.domain.atom;

/** 문자열 답변 입력 변수(예: 임차 형태). known=false면 '모름' — value는 무시된다. */
public record AnswerString(boolean known, String value) {
}
