# Test plan — Phase 1b

**Principle:** No full OpenTelemetry demo in default CI; use **Testcontainers** PostgreSQL for lock/dedup tests when needed.

## Required (1b)

### Unit

- Registry: **`ruleId`** not in **`registry.yaml`** → validation / **`400`**.
- Rule **`demo.otel.signal_v1`**: `match: true` / `metric: demo.synthetic` / no match → `{ "matched": false }`.
- Rule **`demo.stub.always_false_v1`**: always `{ "matched": false }` in reference impl.
- Fingerprint: stable across key-order permutations.

### Integration

- Ingest: **401** bad token; **404** if `signals.enabled=false`.
- **201** new draft; **200** dedup; **200** not matched; **400** bad body / **unknown** `ruleId`; **415** wrong `Content-Type`; **422** bad telemetry/clock; **500** rule throws (test double); **429** if rate limit on.
- **Concurrency:** two parallel same-fingerprint → one row (Postgres; skip or separate profile on H2).
- `GET` incident: optional fields for signal row.

### Manual

- README: OTel Demo pinned version, compose, expected **201** path.

## Tooling

- Same as 1a + **WireMock** optional for future client adapters (not 1b default).
