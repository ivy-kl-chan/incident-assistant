# Story 11 — Ingest idempotency key, replay, and optional audit

## 1. Status

Planned

## 2. Goal

Implement optional **`Idempotency-Key`** handling per **1b** `api-contract.md` with persistence in **`signal_ingest_idempotency`** (if **`V1`** created it), including **canonical JSON** body hashing, **409** on key conflict, successful replay rules, and optional **`signal_ingest_audit`** rows (hash only, no raw PII body).

## 3. User Value

Safe retries from webhook clients do not create duplicate incidents; operators can audit ingest outcomes without storing full payloads.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | **`Idempotency-Key`** algorithm, **409**, replay, TTL, no cache on **401**/**404** |
| [`../../phase-1b-signal-ingest/data-model.md`](../../phase-1b-signal-ingest/data-model.md) | **`signal_ingest_idempotency`** columns; optional **`signal_ingest_audit`** |
| [`../../phase-1b-signal-ingest/test-plan.md`](../../phase-1b-signal-ingest/test-plan.md) | **409** conflict; replay same key+body; replay still requires token |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase **1b** DoD: idempotency behavior |

## 5. In Scope

- Header **`Idempotency-Key`** optional; pattern and max length per **`api-contract.md`**.
- **`body_hash`**, **`key_hash`** computation and lookup **before** rule evaluation / mutations as ordered in **`data-model.md`** “Request handling order”.
- **Upsert** idempotency row on successful **200**/**201** responses that cache per contract (including **`{ "matched": false }`** if applicable—confirm **`api-contract.md`** “successful completion” wording).
- **409** when same key, different body, non-expired row.
- Replay returns stored **HTTP** status + body **after** auth + **`signals.enabled`** checks succeed.
- **`INGEST_IDEMPOTENCY_TTL`** default **24h** (configurable); lazy expiry acceptable.
- Optional **`signal_ingest_audit`**: store **`payload_hash`**, **`matched`**, **`dedup_hit`**, **`incident_id`**, timestamps—if implemented, document fields.

## 6. Out of Scope

- Changing **dedup** matrix (**Story 10**).
- **`GET`** incident extensions (**Story 12**).
- **Rate limiting** (**429**) unless team pulls it in here—default **out** unless spec mandates “if implemented”.
- **Docker**, **OpenTelemetry Demo**, **Kubernetes**, **microservices**, **AI**, **RAG**, **MCP**.

## 7. API Changes

- **Extend:** `POST /api/v1/signal-ingest/evaluations` to honor **`Idempotency-Key`** per contract.

## 8. Data Model Changes

None if **V1** already created **`signal_ingest_idempotency`** / audit tables; otherwise amend **Flyway** with team decision (**ADR** if **V2**—prefer **V1** completeness from Story **2**).

## 9. Business Rules

- Do **not** cache **401**/**404**/**409**/**5xx** per **`api-contract.md`**.
- **Replay** must still validate token and feature flag.

## 10. Acceptance Criteria

- [ ] Same **`Idempotency-Key`** + same body → identical **200**/**201** response without duplicate side effects on incidents/dedup.
- [ ] Same key + different body (within TTL) → **409**.
- [ ] Replay with invalid token → **401** even if cached success exists.
- [ ] Replay with **`signals.enabled=false`** → **404** (not cached success body).

## 11. Test Requirements

- Integration tests per **1b** `test-plan.md` idempotency section.
- Unit test for canonical JSON hashing stability (sorted keys at all nesting levels).

## 12. Files Expected to Change

- **`src/main/java/**`** ingest pipeline ordering, idempotency repository; **`src/test/java/**`**; **`application*.yml`** TTL.

## 13. Implementation Notes

- Ordering must match **`api-contract.md`** / **`data-model.md`**: auth and flag before returning cached success.

## 14. Human Review Checklist

- [ ] **TTL** and cleanup strategy documented.
- [ ] Audit hashing approach avoids storing sensitive payloads.

## 15. Completion Notes

*(Fill when implemented.)*
