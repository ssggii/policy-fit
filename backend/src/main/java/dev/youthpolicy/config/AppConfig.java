package dev.youthpolicy.config;

import dev.youthpolicy.api.RequestToAnswersMapper;
import dev.youthpolicy.domain.evaluate.RuleEvaluator;
import dev.youthpolicy.domain.scope.OutOfScopeClassifier;
import dev.youthpolicy.policy.PolicyCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring 배선 전용. domain/·policy/ 패키지는 프레임워크에 의존하지 않는 순수 Java로 유지하고,
 * 여기서만 빈으로 감싼다.
 */
@Configuration
public class AppConfig {

    @Bean
    public PolicyCatalog policyCatalog() {
        return new PolicyCatalog();
    }

    @Bean
    public OutOfScopeClassifier outOfScopeClassifier() {
        return new OutOfScopeClassifier();
    }

    @Bean
    public RuleEvaluator ruleEvaluator() {
        return new RuleEvaluator();
    }

    @Bean
    public RequestToAnswersMapper requestToAnswersMapper() {
        return new RequestToAnswersMapper();
    }
}
