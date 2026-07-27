# 폴핏 (policy-fit)

_Find the Korean youth-policy benefits that fit you and instantly check your eligibility — with reasons, no login._

> 무인증으로 **내 상황에 맞는 청년 정책을 찾아, 신청 자격 가/부를 근거와 함께 즉시 판정**하는 서비스

[![ci-backend](https://github.com/ssggii/policy-fit/actions/workflows/ci-backend.yml/badge.svg)](https://github.com/ssggii/policy-fit/actions/workflows/ci-backend.yml)
[![ci-web](https://github.com/ssggii/policy-fit/actions/workflows/ci-web.yml/badge.svg)](https://github.com/ssggii/policy-fit/actions/workflows/ci-web.yml)
[![ci-contracts](https://github.com/ssggii/policy-fit/actions/workflows/ci-contracts.yml/badge.svg)](https://github.com/ssggii/policy-fit/actions/workflows/ci-contracts.yml)


## 왜
청년 정책은 수천 개지만, 사람들은 "나에게 맞는 걸 찾아도 **내가 될지**"를 스스로 판단하지 못한다. 중위소득 %·세대주·재직요건 같은 행정 용어를 자기 상황에 대입하는 부담은 온전히 사용자 몫이며, 커뮤니티에 "저 이거 되나요?"를 묻는다 — **자격 질문이 전 채널 1위 신호(143건).** 폴핏은 그 판정을 대신한다.

## 무엇을
일상어 분기 질문에 답하면, 내 상황에 맞는 정책을 추리고 자격 요건에 대입하여
**가능 / 부적합 / 추가 확인 필요 / 자가판정 불가**를 근거·출처와 함께 즉시 제시한다.

## 핵심 설계
- **요건 원자 + 선언적 rule DSL** — 정책마다 로직을 새로 짜지 않고 재사용 원자(나이·소득·거주 등)의 AND/OR 조합으로 표현한다. 정책 추가는 곧 규칙 데이터 한 건을 의미한다. → [ADR-0003](adr/0003-판정-도메인-모델.md)
- **Kleene 3치 논리** — 무인증이라 '모름'(unknown)이 일급 값이다. 전파 규칙을 정의해 경계 사용자에게 잘못된 확신을 주지 않는다.
- **결정론 판정** — 판정 경로에 런타임 LLM이 없다. 정확도가 핵심이라 Java sealed/record/switch로 분기 누락을 컴파일 타임에 잡는다.
- **계약 우선(contract-first)** — `contracts/`의 OpenAPI·JSON Schema가 Java·TS 타입의 단일 원천이다(SSOT). 드리프트를 구조적으로 차단한다. → [ADR-0004](adr/0004-기술-스택.md)

## 아키텍처
```
web/ (Next.js + TS) ──POST /verdicts──▶ backend/ (Spring, Java 21)
                                          판정 엔진(원자·Kleene·사전분류)
contracts/ (OpenAPI·JSON Schema = SSOT)
   ├─ TS 타입 → web/
   └─ Java 모델 → backend/
```

## 기술 스택
| 영역 | 스택 |
|---|---|
| 백엔드 | Java 21 · Spring Boot 4.x · Gradle · JUnit 5 |
| 프론트 | Next.js (App Router) · TypeScript · pnpm · Vitest · Playwright |
| 계약 | OpenAPI · JSON Schema (SSOT) |
| CI/CD | GitHub Actions |

## 저장소 구조
- `backend/` · `web/` · `contracts/` · `contract-tests/`
- 제품 문서 — [PRD](PRD.md) · [SPEC](SPEC.md) · [DOMAIN](DOMAIN.md) · [ARCHITECTURE](ARCHITECTURE.md) · [adr/](adr/)
- 운영 — [CLAUDE.md](CLAUDE.md) · [loop.md](loop.md) · [CONTRIBUTING](CONTRIBUTING.md)

## 개발 방식
**1인 풀스택 개발 + AI 에이전트 루프**
- 작업 단위·게이트·리뷰·회고를 [`loop.md`](loop.md)로 정의한다.
- 계약·제품문서·컨벤션을 문서로 고정해 에이전트가 일관되게 구현한다.

## 상태
문서·계약(freeze)·하네스·스캐폴딩 완료. 첫 정책 버티컬 슬라이스 착수 예정.
