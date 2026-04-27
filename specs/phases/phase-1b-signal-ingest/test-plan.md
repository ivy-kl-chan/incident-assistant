# Test plan — Phase 1b

**Principle:** No full OpenTelemetry demo in default CI. **PostgreSQL** (e.g. **Testcontainers**) is **required** for default **1b** integration tests (ingest dedup, advisory lock, idempotency store). **H2** is **not** normative for those paths.

## Required (1b)

### Unit

- Registry: **`ruleId`** not in **`registry.yaml`** → validation / **`400`**.
- Startup: misconfigured registry (YAML **`id`** without evaluator binding) → process **fails to start** (or equivalent test hook).
- Rule **`demo.otel.signal_v1`**: `match: true` / `metric: demo.synthetic` / no match → `{ "matched": false }`.
- Rule **`demo.stub.always_false_v1`**: always `{ "matched": false }` in reference impl.
- Fingerprint: stable across key-order permutations.

### Integration

- Ingest: **401** bad token; **404** if `signals.enabled=false`.
- **201** new draft; **200** dedup; **200** not matched; **400** bad body / **unknown** `ruleId`; **415** wrong `Content-Type`; **409** idempotency key conflict (same key, different body); **422** bad telemetry/clock; **500** rule throws (test double); **429** if rate limit on.
- **`Idempotency-Key`:** same key + same body → identical **200**/**201** response without duplicate incident; replay still requires valid token.
- **Concurrency:** two parallel same-fingerprint → one row (**PostgreSQL**).
- **`GET` list:** omitted **`source`** → **`MANUAL`** rows only; **`source=ALL`** returns mixed; invalid **`source`** → **400**.
- `GET` incident by id: optional fields for signal row.

### Manual

- README: OTel Demo pinned version, compose, expected **201** path.

## Tooling

- Same as 1a + **WireMock** optional for future client adapters (not 1b default).
