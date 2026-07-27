package dev.youthpolicy.policy;

/** 요청받은 policy_id가 카탈로그에 없을 때. */
public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(String policyId) {
        super("등록되지 않은 policy_id 입니다: " + policyId);
    }
}
