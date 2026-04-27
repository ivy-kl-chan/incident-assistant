# API contract — Phase 1b (signal ingest + incident extensions)

**Depends on:** `../phase-1a-monolith-core/api-contract.md` for all **incident** resources not listed here. **1b** **adds** the ingest route and **extends** list/get for signal-backed rows.

**Shared conventions** (versioning, errors, correlation, problem+json): same as **1a** base doc.

---

## Feature flag

- Config **`signals.enabled`**. If **`false`**: **`POST /api/v1/signal-ingest/evaluations` → `404`**. (1a may ship with controller absent; 1b registers route when enabled.)

---

## Extensions to incident API (1b)

Applies when **`signals.enabled=true`** and DB rows may have **`source = SIGNAL`**.

### `GET /api/v1/incidents` (extension)

- Query **`source`**: `MANUAL` or **`SIGNAL`** (1b). Invalid combo → **400** (unchanged from enum rules).
- `IncidentSummary`: **`source`** may be **`SIGNAL`**.

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

**Auth:** header **`X-Integration-Token`** = env **`SIGNAL_INGEST_TOKEN`**; compare with **constant-time** equality.

**Request body** (`SignalEvaluationRequest`):

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `ruleId` | string | yes | Max **128**; pattern **`^[a-z0-9._-]+$`**; shipped: **`demo.otel.signal_v1`** |
| `observedAt` | string (instant) | yes | **`now() - 7d` ≤ t ≤ `now() + 5m`** |
| `fingerprintInputs` | object | yes | Max **4 KiB** serialized, depth **≤ 4**, **≤ 32** keys, key length **≤ 64**; string values **≤ 512** chars |
| `telemetryPointers` | object | yes | Same limits; **`windowEnd` ≥ `windowStart`** when both set |
| `summary` | string | no | max **2_000** chars — maps to `description` |

**Max body: 64 KiB** → **413** / **400** as server supports.

**Match semantics (shipped rule):** rule **`demo.otel.signal_v1`** matches if **`fingerprintInputs.match` is boolean `true`** (CI) **or** **`fingerprintInputs.metric` equals** **`demo.synthetic`**. Else **`{ "matched": false }`**.

**Responses:**

| Case | HTTP | Body |
|------|------|------|
| New draft | `201` | `{ "created": true, "incidentId", "status": "DRAFT" }` |
| Dedup | `200` | `{ "created": false, "incidentId", "reason": "DUPLICATE_SIGNAL" }` |
| No match | `200` | `{ "matched": false }` |
| Bad rule / validation | `400` / `422` | problem+json |
| Auth | `401` | |
| Rate limit | `429` | if implemented |
| Rule internal error | `500` | no stack in body |
| DB down | `503` preferred or `500` (document) |

**Failure test matrix:** `test-plan.md`.

---

## OpenAPI (1b)

- **Artifact:** `specs/openapi/openapi-1b.yaml` (ingest path + 1b field schemas where applicable). Keep aligned with 1a file or merge in tooling (see `specs/openapi/README.md`).

---

## Shipped rule

| `ruleId` | Registry |
|----------|----------|
| `demo.otel.signal_v1` | `rules/demo-rule-v1.yaml` |
