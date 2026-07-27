package dev.youthpolicy.domain.rule;

import dev.youthpolicy.domain.atom.AtomId;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** contract-tests/examples/valid/rule-dsl/01-jutaekdream.json 파싱 성공 확인. */
class RuleDslParserTest {

    private static final Path EXAMPLE_PATH =
            Path.of("..", "contract-tests", "examples", "valid", "rule-dsl", "01-jutaekdream.json");

    @Test
    void parsesJutaekDreamExample() throws IOException {
        String json = Files.readString(EXAMPLE_PATH);
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode root = mapper.readTree(json);

        RuleNode node = RuleDslParser.parse(root);

        assertThat(node).isInstanceOf(RuleNode.AllOf.class);
        RuleNode.AllOf allOf = (RuleNode.AllOf) node;
        assertThat(allOf.children()).hasSize(3);
        assertThat(allOf.children()).allSatisfy(child -> assertThat(child).isInstanceOf(RuleNode.AtomRef.class));

        RuleNode.AtomRef ageRef = (RuleNode.AtomRef) allOf.children().get(0);
        assertThat(ageRef.atom()).isEqualTo(AtomId.AGE);
        assertThat(ageRef.params()).containsEntry("min", 19).containsEntry("max", 34);
        assertThat(ageRef.meta()).isNotNull();
        assertThat(ageRef.meta().source()).isEqualTo("국토교통부");
        assertThat(ageRef.meta().year()).isEqualTo(2026);

        RuleNode.AtomRef housingRef = (RuleNode.AtomRef) allOf.children().get(1);
        assertThat(housingRef.atom()).isEqualTo(AtomId.HOUSING_NONE);
        assertThat(housingRef.params()).isEmpty();

        RuleNode.AtomRef incomeRef = (RuleNode.AtomRef) allOf.children().get(2);
        assertThat(incomeRef.atom()).isEqualTo(AtomId.INCOME_SELF);
        assertThat(incomeRef.params()).containsEntry("max_krw", 50_000_000);
    }

    @Test
    void rejectsNodeWithoutRecognizedKey() throws IOException {
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode root = mapper.readTree("{\"unexpected\": true}");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> RuleDslParser.parse(root))
                .isInstanceOf(RuleDslParseException.class);
    }
}
