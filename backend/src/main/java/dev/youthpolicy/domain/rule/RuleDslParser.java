package dev.youthpolicy.domain.rule;

import dev.youthpolicy.domain.atom.AtomId;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rule DSL(JSON) → RuleNode 트리 파서 — DOMAIN.md §4.
 *
 * 문법의 기계 검증(형식)은 contracts/rule-dsl.schema.json이 SSOT다. 이 파서는 이미 유효한
 * 형식을 가정하되, 방어적으로 최소한의 구조 검증을 병행한다.
 */
public final class RuleDslParser {

    private RuleDslParser() {
    }

    public static RuleNode parse(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isObject()) {
            throw new RuleDslParseException("규칙 노드는 JSON 객체여야 합니다: " + node);
        }
        if (node.has("all_of")) {
            return new RuleNode.AllOf(parseChildren(node.get("all_of")));
        }
        if (node.has("any_of")) {
            return new RuleNode.AnyOf(parseChildren(node.get("any_of")));
        }
        if (node.has("not")) {
            return new RuleNode.Not(parse(node.get("not")));
        }
        if (node.has("atom")) {
            return parseAtom(node);
        }
        throw new RuleDslParseException("all_of/any_of/not/atom 중 어느 것도 없는 노드입니다: " + node);
    }

    private static List<RuleNode> parseChildren(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray() || arrayNode.isEmpty()) {
            throw new RuleDslParseException("all_of/any_of는 최소 1개 이상의 자식 노드가 필요합니다.");
        }
        List<RuleNode> children = new ArrayList<>();
        for (JsonNode child : arrayNode) {
            children.add(parse(child));
        }
        return children;
    }

    private static RuleNode.AtomRef parseAtom(JsonNode node) {
        String atomWireValue = node.path("atom").asString(null);
        AtomId atomId = AtomId.fromWireValue(atomWireValue)
                .orElseThrow(() -> new RuleDslParseException("알 수 없는 atom id입니다: " + atomWireValue));
        Map<String, Object> params = node.has("params") ? toMap(node.get("params")) : Map.of();
        RuleNode.Meta meta = node.has("meta") ? parseMeta(node.get("meta")) : null;
        return new RuleNode.AtomRef(atomId, params, meta);
    }

    private static RuleNode.Meta parseMeta(JsonNode metaNode) {
        String source = metaNode.path("source").asString(null);
        Integer year = metaNode.has("year") ? metaNode.get("year").asInt() : null;
        return new RuleNode.Meta(source, year);
    }

    private static Map<String, Object> toMap(JsonNode paramsNode) {
        Map<String, Object> map = new LinkedHashMap<>();
        paramsNode.forEachEntry((key, value) -> map.put(key, toJavaValue(value)));
        return map;
    }

    private static Object toJavaValue(JsonNode value) {
        if (value.isNumber()) {
            return value.numberValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isString()) {
            return value.asString();
        }
        if (value.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : value) {
                list.add(toJavaValue(item));
            }
            return list;
        }
        if (value.isObject()) {
            return toMap(value);
        }
        return null;
    }
}
