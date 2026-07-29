#!/usr/bin/env python3
"""계약 검증 — openapi.yaml 구조 검증 + 스키마 메타검증 + 예시 픽스처 검증.

- contracts/openapi.yaml이 OpenAPI 3.1 스펙에 유효한 문서인지(상대 $ref resolve 포함)
- contracts/ 의 JSON Schema가 유효한 JSON Schema인지(메타검증)
- contract-tests/examples/valid/<schema>/*.json  은 반드시 통과
- contract-tests/examples/invalid/<schema>/*.json 은 반드시 실패

<schema> 디렉토리명 → 스키마 파일 매핑:
  rule-dsl → contracts/rule-dsl.schema.json
  verdict  → contracts/verdict.schema.json

픽스처는 DOMAIN §1·§4의 예시를 기계 검증본으로 고정한 것이다.
스키마 변경 시 이 검증이 회귀로 걸린다(메모리: CI 스키마-예시 검증).
"""
import json
import sys
from pathlib import Path

from jsonschema import Draft202012Validator
from openapi_spec_validator import validate as validate_openapi
from openapi_spec_validator.readers import read_from_filename

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
CONTRACTS = ROOT / "contracts"
EXAMPLES = HERE / "examples"
OPENAPI = CONTRACTS / "openapi.yaml"

SCHEMAS = {
    "rule-dsl": CONTRACTS / "rule-dsl.schema.json",
    "verdict": CONTRACTS / "verdict.schema.json",
}

fails = 0


def fail(msg: str) -> None:
    global fails
    print(f"FAIL {msg}")
    fails += 1


# 0) openapi.yaml 구조 검증 — VerdictResult.verdict가 verdict.schema.json을
#    상대 $ref로 참조하므로 base_uri를 넘겨 resolve되게 한다.
try:
    spec_dict, base_uri = read_from_filename(str(OPENAPI))
    validate_openapi(spec_dict, base_uri=base_uri)
    print("OK   openapi: openapi.yaml은 유효한 OpenAPI 3.1 문서")
except Exception as e:  # noqa: BLE001
    fail(f"openapi: openapi.yaml 검증 실패 — {e}")

# 1) 스키마 메타검증 + 검증기 준비
validators = {}
for name, path in SCHEMAS.items():
    schema = json.loads(path.read_text(encoding="utf-8"))
    try:
        Draft202012Validator.check_schema(schema)
        print(f"OK   meta: {name} 스키마는 유효한 JSON Schema")
    except Exception as e:  # noqa: BLE001
        fail(f"meta: {name} 스키마 자체가 유효하지 않음 — {e}")
    validators[name] = Draft202012Validator(schema)


def iter_examples(kind: str):
    base = EXAMPLES / kind
    if not base.exists():
        return
    for schema_dir in sorted(base.iterdir()):
        if not schema_dir.is_dir():
            continue
        if schema_dir.name not in validators:
            fail(f"{kind}: 알 수 없는 스키마 디렉토리 '{schema_dir.name}'")
            continue
        for f in sorted(schema_dir.glob("*.json")):
            yield schema_dir.name, f


# 2) valid — 반드시 통과
for name, f in iter_examples("valid"):
    doc = json.loads(f.read_text(encoding="utf-8"))
    errors = sorted(validators[name].iter_errors(doc), key=lambda e: list(e.path))
    if errors:
        fail(f"valid/{name}/{f.name} 통과해야 하는데 실패 — {errors[0].message}")
    else:
        print(f"OK   valid/{name}/{f.name}")

# 3) invalid — 반드시 실패
for name, f in iter_examples("invalid"):
    doc = json.loads(f.read_text(encoding="utf-8"))
    if validators[name].is_valid(doc):
        fail(f"invalid/{name}/{f.name} 실패해야 하는데 통과함")
    else:
        print(f"OK   invalid/{name}/{f.name} (기대대로 거부)")

print()
if fails:
    print(f"❌ {fails}건 실패")
    sys.exit(1)
print("✅ 모든 계약 검증 통과")
