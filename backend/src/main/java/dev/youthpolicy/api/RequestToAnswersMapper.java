package dev.youthpolicy.api;

import dev.youthpolicy.api.dto.AnswerApproxIntDto;
import dev.youthpolicy.api.dto.AnswerBoolDto;
import dev.youthpolicy.api.dto.AnswerIntDto;
import dev.youthpolicy.api.dto.AnswersDto;
import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.Answers;

/**
 * API 요청 DTO(answers) → 도메인 {@link Answers}. answers의 각 필드는 요청에서 아예 생략될 수
 * 있는데(계약상 required 없음), 이는 known=false('모름')와 동일하게 취급한다.
 */
public final class RequestToAnswersMapper {

    public Answers toAnswers(AnswersDto dto) {
        if (dto == null) {
            return new Answers(unknownInt(), unknownBool(), unknownApproxInt());
        }
        return new Answers(
                toAnswerInt(dto.age()),
                toAnswerBool(dto.housingNone()),
                toAnswerApproxInt(dto.incomeSelfMonthlyKrw()));
    }

    private AnswerInt toAnswerInt(AnswerIntDto dto) {
        if (dto == null || !Boolean.TRUE.equals(dto.known())) {
            return unknownInt();
        }
        return new AnswerInt(true, dto.value());
    }

    private AnswerBool toAnswerBool(AnswerBoolDto dto) {
        if (dto == null || !Boolean.TRUE.equals(dto.known())) {
            return unknownBool();
        }
        return new AnswerBool(true, dto.value());
    }

    private AnswerApproxInt toAnswerApproxInt(AnswerApproxIntDto dto) {
        if (dto == null || !Boolean.TRUE.equals(dto.known())) {
            return unknownApproxInt();
        }
        return new AnswerApproxInt(true, Boolean.TRUE.equals(dto.approx()), dto.value());
    }

    private AnswerInt unknownInt() {
        return new AnswerInt(false, null);
    }

    private AnswerBool unknownBool() {
        return new AnswerBool(false, null);
    }

    private AnswerApproxInt unknownApproxInt() {
        return new AnswerApproxInt(false, false, null);
    }
}
