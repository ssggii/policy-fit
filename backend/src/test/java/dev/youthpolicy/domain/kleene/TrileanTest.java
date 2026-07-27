package dev.youthpolicy.domain.kleene;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static dev.youthpolicy.domain.kleene.Trilean.FALSE;
import static dev.youthpolicy.domain.kleene.Trilean.TRUE;
import static dev.youthpolicy.domain.kleene.Trilean.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** DOMAIN.md §3.2 진리표 전부 + §3.3 n항 흡수원소 케이스. */
class TrileanTest {

    // --- §3.2 not ---

    @ParameterizedTest
    @CsvSource({
            "TRUE, FALSE",
            "FALSE, TRUE",
            "UNKNOWN, UNKNOWN"
    })
    void not(Trilean input, Trilean expected) {
        assertThat(input.not()).isEqualTo(expected);
    }

    // --- §3.2 all_of (AND, 이항) — false가 흡수원소 ---

    @ParameterizedTest
    @CsvSource({
            "TRUE, TRUE, TRUE",
            "TRUE, FALSE, FALSE",
            "TRUE, UNKNOWN, UNKNOWN",
            "FALSE, TRUE, FALSE",
            "FALSE, FALSE, FALSE",
            "FALSE, UNKNOWN, FALSE",
            "UNKNOWN, TRUE, UNKNOWN",
            "UNKNOWN, FALSE, FALSE",
            "UNKNOWN, UNKNOWN, UNKNOWN"
    })
    void allOfBinaryTruthTable(Trilean a, Trilean b, Trilean expected) {
        assertThat(Trilean.allOf(List.of(a, b))).isEqualTo(expected);
    }

    // --- §3.2 any_of (OR, 이항) — true가 흡수원소 ---

    @ParameterizedTest
    @CsvSource({
            "TRUE, TRUE, TRUE",
            "TRUE, FALSE, TRUE",
            "TRUE, UNKNOWN, TRUE",
            "FALSE, TRUE, TRUE",
            "FALSE, FALSE, FALSE",
            "FALSE, UNKNOWN, UNKNOWN",
            "UNKNOWN, TRUE, TRUE",
            "UNKNOWN, FALSE, UNKNOWN",
            "UNKNOWN, UNKNOWN, UNKNOWN"
    })
    void anyOfBinaryTruthTable(Trilean a, Trilean b, Trilean expected) {
        assertThat(Trilean.anyOf(List.of(a, b))).isEqualTo(expected);
    }

    // --- §3.3 n항 흡수원소 ---

    @Test
    void allOfNAryAbsorbsFalseRegardlessOfOtherUnknowns() {
        assertThat(Trilean.allOf(List.of(TRUE, UNKNOWN, FALSE, UNKNOWN))).isEqualTo(FALSE);
    }

    @Test
    void allOfNAryIsUnknownWhenNoFalseButHasUnknown() {
        assertThat(Trilean.allOf(List.of(TRUE, TRUE, UNKNOWN))).isEqualTo(UNKNOWN);
    }

    @Test
    void allOfNAryIsTrueWhenAllTrue() {
        assertThat(Trilean.allOf(List.of(TRUE, TRUE, TRUE))).isEqualTo(TRUE);
    }

    @Test
    void anyOfNAryAbsorbsTrueRegardlessOfOtherUnknowns() {
        assertThat(Trilean.anyOf(List.of(FALSE, UNKNOWN, TRUE, UNKNOWN))).isEqualTo(TRUE);
    }

    @Test
    void anyOfNAryIsUnknownWhenNoTrueButHasUnknown() {
        assertThat(Trilean.anyOf(List.of(FALSE, FALSE, UNKNOWN))).isEqualTo(UNKNOWN);
    }

    @Test
    void anyOfNAryIsFalseWhenAllFalse() {
        assertThat(Trilean.anyOf(List.of(FALSE, FALSE, FALSE))).isEqualTo(FALSE);
    }

    @Test
    void allOfRejectsEmptyList() {
        assertThatThrownBy(() -> Trilean.allOf(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anyOfRejectsEmptyList() {
        assertThatThrownBy(() -> Trilean.anyOf(List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}
