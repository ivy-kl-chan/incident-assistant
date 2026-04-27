# API contract — Phase 1b (signal ingest + incident extensions)

**Depends on:** `../phase-1a-monolith-core/api-contract.md` for all **incident** resources not listed here. **1b** **adds** the ingest route and **extends** list/get for signal-backed rows.

**Shared conventions** (versioning, errors, correlation, problem+json): same as **1a** base doc.

---

## Feature flag

- Config **`signals.enabled`**. If **`false`**: **`POST /api/v1/signal-ingest/evaluations` → `404`**. (Phase **1a** does not register this route at all; **1b** registers it when the ingest feature is included, and may return **`404`** when disabled.)

---

## Extensions to incident API (1b)

Applies when **`signals.enabled=true`** and DB rows may have **`source = SIGNAL`**.

**Database:** **PostgreSQL** is **normative** for **1b** (advisory locks, partial indexes, optional idempotency store). **H2** (or other engines) may be used locally only with an explicit **divergence** note in README/ADR—**default CI** for **1b** ingest, dedup, and idempotency tests uses **PostgreSQL** (e.g. Testcontainers).

### `GET /api/v1/incidents` (extension)

- **Unknown query keys** and bad **`page`/`size`** → **`400`**, same as **1a** (`../phase-1a-monolith-core/api-contract.md` § Pagination).
- Query **`source`** (optional): single token only.
  - **Omitted** → return rows with **`source = MANUAL`** only (default; same effective filter as **1a** list).
  - **`source=MANUAL`** → same as omitted (**`MANUAL`** only).
  - **`source=SIGNAL`** → **`SIGNAL`** rows only.
  - **`source=ALL`** → **`MANUAL`** and **`SIGNAL`** rows.
  - **Unknown** token, **empty** value, **comma-separated** multiple values, or **duplicate** query keys → **`400`**.
- `IncidentSummary`: **`source`** may be **`SIGNAL`** when filter allows it.

### `GET /api/v1/incidents/{id}` (extension)

**Additional fields** (when present in DB; **`null`/`omitted` for** pure manual rows):

| Field | Type | Notes |
|-------|------|--------|
| `telemetryContext` | object \| null | See `data-model.md` |
| `createdByRuleId` | string \| null | e.g. `demo.otel.signal_v1` |
| `signalFingerprint` | string \| null | 64-char hex |

**Security:** do not put secrets or pre-signed tokens inside `deepLinks` values; treat as **untrusted** display URLs.

---

## `POST /api/v1/signal-ingest/evaluations` (1b)

**`Content-Type`:** must be **`application/json`**. Otherwise **`415`**.

**Auth:** header **`X-Integration-Token`** = env **`SIGNAL_INGEST_TOKEN`**; compare with **constant-time** equality.

**Registry vs. code:** at **application startup**, every **`id`** in **`rules/registry.yaml`** **must** have a **bound evaluator** in code. If any entry lacks an evaluator, the process **must fail to start** (fail-fast). Adding a rule to YAML without code is a **deployment error**.

**Rule registry:** `ruleId` on the request **must** exist in **`rules/registry.yaml`**. Unknown id → **`400`** (problem+json; not **`404`**).

**Normative match behavior:** for the shipped rule ids below, the **bullet definitions** in this section are **normative**. The **`matchSemantics`** field in **`registry.yaml`** is **informative** documentation only and **must not** contradict these bullets.

**Idempotency (optional header):** clients MAY send **`Idempotency-Key`** (HTTP header), max **128** chars, pattern **`^[a-zA-Z0-9._-]+$`**. When **absent**, behavior is unchanged (fingerprint dedup only, per `data-model.md`). When **present**:
- Compute **`body_hash`** = **SHA-256** hex over **UTF-8** bytes of the **canonical JSON** of the **entire** request object (all top-level keys sorted lexicographically; recurse into nested objects the same way so byte-for-byte replays hash identically).
- Compute **`key_hash`** = **SHA-256** hex over UTF-8 bytes of the header value.
- **Before** rule evaluation and mutating work, look up **`key_hash`** in the idempotency store (`data-model.md`). If a row exists, is **not** expired, and **`body_hash`** **equals** the stored **`body_hash`** → return the **stored** HTTP **status** and **body** (**no** duplicate side effects).
- If a row exists for **`key_hash`**, is **not** expired, and **`body_hash`** **≠** stored **`body_hash`** → **`409`** (problem+json; same key, different body).
- On **successful** completion of an ingest that produced a final HTTP response (**200** / **201** with bodies defined here, including **`{ "matched": false }`**), **upsert** the row for **`key_hash`** (store **`body_hash`**, **`http_status`**, **`response_body`**, **`created_at`**).
- Default **`INGEST_IDEMPOTENCY_TTL`**: **24 hours** (configurable). Expired rows **may** be deleted lazily.
- **Replay:** when returning a cached response, **still require** valid **`X-Integration-Token`** and **`signals.enabled=true`**; do **not** return a cached **200**/**201** if auth fails (**401**) or ingest is disabled (**404**). Do **not** cache **401**/**404**/**409**/**5xx** responses.

**Request body** (`SignalEvaluationRequest`):

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `ruleId` | string | yes | Max **128**; pattern **`^[a-z0-9._-]+$`**; must be a **registered** id (see **`rules/registry.yaml`**) |
| `observedAt` | string (instant) | yes | **`now() - 7d` ≤ t ≤ `now() + 5m`** |
| `fingerprintInputs` | object | yes | Max **4 KiB** serialized, depth **≤ 4**, **≤ 32** keys, key length **≤ 64**; string values **≤ 512** chars; **arrays** at most **32** elements (each element obeys same depth/size limits recursively) |
| `telemetryPointers` | object | yes | Same limits as `fingerprintInputs` |
| `summary` | string | no | max **2_000** chars — maps to `description` |

**Max body: 64 KiB** → **413** / **400** as server supports.

**Match semantics (per rule):** evaluation is **pluggable** by `ruleId`. **Normative** behavior for shipped ids is the bullet list below (see also **Normative match behavior** above re: YAML).

- **`demo.otel.signal_v1`:** matches if **`fingerprintInputs.match` is boolean `true`** (CI) **or** **`fingerprintInputs.metric` equals** **`demo.synthetic`**. Else treat as not matched → **`{ "matched": false }`** without creating an incident.
- **`demo.stub.always_false_v1`:** reference implementation **never** matches → **`{ "matched": false }`**.

**Transactions:** for a **matched** evaluation that may create or dedupe an incident, the server SHOULD run **fingerprint lookup + insert/update + advisory lock** inside **one** database transaction (see `data-model.md`) so concurrent ingests do not create duplicate drafts incorrectly.

**Responses:**

| Case | HTTP | Body |
|------|------|------|
| New draft | `201` | `{ "created": true, "incidentId", "status": "DRAFT" }` |
| Dedup | `200` | `{ "created": false, "incidentId", "reason": "DUPLICATE_SIGNAL" }` |
| No match | `200` | `{ "matched": false }` |
| Unknown `ruleId` / validation | `400` / `422` | problem+json |
| Idempotency key reuse with different body (within TTL) | `409` | problem+json |
| Wrong `Content-Type` | `415` | problem+json optional |
| Auth | `401` | |
| Rate limit | `429` | if implemented |
| Rule internal error | `500` | no stack in body |
| DB down | `503` preferred or `500` (document) |

**Failure test matrix:** `test-plan.md`.

---

## OpenAPI (1b)

- **Artifact:** `specs/openapi/openapi-1b.yaml` — at **1b** gate includes: **`GET /api/v1/incidents`** (optional **`source`**: `MANUAL` \| `SIGNAL` \| `ALL`; default **MANUAL-only** when omitted), **`GET /api/v1/incidents/{id}`** (extended **`IncidentDetail1b`** + **`ETag`**), **`POST /api/v1/signal-ingest/evaluations`** (**`Idempotency-Key`**, **`409`**, **`SignalEvaluationRequest`** schema). **Merge** with **`openapi-1a.yaml`** for **`POST/PATCH`** incidents, **`transitions`**, and **`/actuator/health`** (see `specs/openapi/README.md`).

---

## Shipped rules (registry)

| Artifact | Role |
|----------|------|
| `rules/registry.yaml` | **Normative** list of shipped **`ruleId`** values + metadata (`severityOnCreate`, `titlePrefix`). **`matchSemantics`** in YAML is **informative** only (see **Normative match behavior** in this file). |

Add new shipped rules by extending **`registry.yaml`** and registering an evaluator in code; **startup** **must** fail if a new YAML **`id`** is not bound (or ADR for dynamic plugins).
