package dev.youthpolicy.policy;

import dev.youthpolicy.domain.rule.RuleDslParser;
import dev.youthpolicy.domain.rule.RuleNode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 정책 레코드 카탈로그. {@code src/main/resources/policies/*.json}에서 로드한다.
 * 정책 추가 = 이 클래스를 고치는 게 아니라 리소스 파일을 추가하고 {@link #RESOURCE_PATHS}에 등록하는 것.
 *
 * 프레임워크 독립적으로 유지하기 위해 Spring의 리소스 로더 대신 클래스로더로 직접 읽는다.
 * Spring 배선(빈 등록)은 {@code dev.youthpolicy.config.AppConfig}가 담당한다.
 */
public final class PolicyCatalog {

    private static final List<String> RESOURCE_PATHS =
            List.of("policies/jutaek-dream.json", "policies/beotimmok-jeonse.json",
                    "policies/cheongnyeon-wolse.json");

    private final Map<String, PolicyRecord> policiesById;

    public PolicyCatalog() {
        this(RESOURCE_PATHS);
    }

    public PolicyCatalog(List<String> resourcePaths) {
        JsonMapper mapper = JsonMapper.builder().build();
        this.policiesById = resourcePaths.stream()
                .map(path -> loadPolicy(mapper, path))
                .collect(Collectors.toUnmodifiableMap(PolicyRecord::id, policy -> policy));
    }

    public Optional<PolicyRecord> findById(String id) {
        return Optional.ofNullable(policiesById.get(id));
    }

    private static PolicyRecord loadPolicy(JsonMapper mapper, String classpathResource) {
        try (InputStream in = PolicyCatalog.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException("정책 리소스를 찾을 수 없습니다: " + classpathResource);
            }
            JsonNode root = mapper.readTree(in);
            String id = root.path("id").asString(null);
            String name = root.path("name").asString(null);
            RuleNode rule = RuleDslParser.parse(root.get("rule"));
            RuleNode outOfScopeGate = parseOptionalRule(root.get("out_of_scope_gate"));
            PolicyApplication application = parseApplication(root.get("application"));
            return new PolicyRecord(id, name, rule, outOfScopeGate, application);
        } catch (IOException e) {
            throw new IllegalStateException("정책 리소스를 읽는 중 오류가 발생했습니다: " + classpathResource, e);
        }
    }

    /** 선택적 Rule DSL 트리(out_of_scope_gate, ADR-0005 D5). 없으면 null. */
    private static RuleNode parseOptionalRule(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return RuleDslParser.parse(node);
    }

    private static PolicyApplication parseApplication(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String url = node.path("url").asString(null);
        String selectionMethod = node.path("selection_method").asString(null);
        String period = node.path("period").asString(null);
        return new PolicyApplication(url, selectionMethod, period);
    }
}
