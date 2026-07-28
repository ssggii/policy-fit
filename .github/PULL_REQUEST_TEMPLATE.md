## 개요

> 이 PR에서 무엇을 변경했는지 한 줄로 요약해 주세요.

## 연관 이슈

> ex) close #123

## PR 유형

- [ ] 기능 추가 (feat)
- [ ] 버그 수정 (fix)
- [ ] 리팩토링 (refactor)
- [ ] 설정/빌드/인프라 (build·ci·chore)
- [ ] 문서 (docs)
- [ ] 테스트 추가/수정 (test)

## 변경 내용

> 어떤 문제를 해결했는지, 왜 이렇게 구현했는지 설명해 주세요.

## 테스트 방법

> 변경 사항을 확인하는 방법을 적어 주세요. (실행 순서, 확인 엔드포인트 등)

```
# 예시
1. backend: ./gradlew test   (또는 ./gradlew bootRun)
2. web:     pnpm test         (또는 pnpm dev)
3. 확인: POST /verdicts 응답의 verdict 상태·근거 표시
```

## 리뷰 포인트

> 리뷰어가 특히 확인해줬으면 하는 부분을 적어 주세요.

## 관련 / 스코프

- SPEC 기능: F-  <!-- 커밋 푸터 Refs와 일치 -->
- 스코프:
  - [ ] backend
  - [ ] web
  - [ ] contracts

## 체크리스트 (loop.md 게이트)

> 해당 없는 항목은 삭제해 주세요.

- [ ] 테스트 green (게이트 2) — backend JUnit / web Vitest·Playwright
- [ ] 정합성·리뷰 통과 (게이트 3·3-1)
- [ ] 계약(`contracts/`) 변경 시 사람 승인됨 (게이트 C)
- [ ] 외부 라이브러리 추가 시 승인됨 (게이트 D)
- [ ] F-ID의 SPEC 수용 기준 충족 (DoD)
