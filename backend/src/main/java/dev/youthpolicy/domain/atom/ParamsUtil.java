package dev.youthpolicy.domain.atom;

import java.util.Map;
import java.util.Optional;

/** Rule DSL atom params(Map&lt;String,Object&gt;)에서 숫자 파라미터를 안전하게 꺼내는 헬퍼. */
public final class ParamsUtil {

    private ParamsUtil() {
    }

    public static Optional<Long> getLong(Map<String, Object> params, String key) {
        if (params == null) {
            return Optional.empty();
        }
        Object value = params.get(key);
        if (value instanceof Number number) {
            return Optional.of(number.longValue());
        }
        return Optional.empty();
    }
}
