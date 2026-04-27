# API contract — Phase 1b (signal ingest + incident extensions)

**Depends on:** `../phase-1a-monolith-core/api-contract.md` for all **incident** resources not listed here. **1b** **adds** the ingest route and **extends** list/get for signal-backed rows.

**Shared conventions** (versioning, errors, correlation, problem+json): same as **1a** base doc.

---

## Feature flag

- Config **`signals.enabled`**. If **`false`**: **`POST /api/v1/signal-ingest/evaluations` → `404`**. (Phase **1a** does not register this route at all; **1b** registers it when the ingest feature is included, and may return **`404`** when disabled.)

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

**`Content-Type`:** must be **`application/json`**. Otherwise **`415`**.

**Auth:** header **`X-Integration-Token`** = env **`SIGNAL_INGEST_TOKEN`**; compare with **constant-time** equality.

**Rule registry:** `ruleId` **must** exist in **`rules/registry.yaml`**. Unknown id → **`400`** (problem+json; treat as client error, not **`404`**).

**Request body** (`SignalEvaluationRequest`):

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `ruleId` | string | yes | Max **128**; pattern **`^[a-z0-9._-]+$`**; must be a **registered** id (see **`rules/registry.yaml`**) |
| `observedAt` | string (instant) | yes | **`now() - 7d` ≤ t ≤ `now() + 5m`** |
| `fingerprintInputs` | object | yes | Max **4 KiB** serialized, depth **≤ 4**, **≤ 32** keys, key length **≤ 64**; string values **≤ 512** chars; **arrays** at most **32** elements (each element obeys same depth/size limits recursively) |
| `telemetryPointers` | object | yes | Same limits as `fingerprintInputs` |
| `summary` | string | no | max **2_000** chars — maps to `description` |

**Max body: 64 KiB** → **413** / **400** as server supports.

**Match semantics (per rule):** evaluation is **pluggable** by `ruleId`. Reference behavior is defined in **`rules/registry.yaml`** (`matchSemantics` per rule), summarized here:

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
| Wrong `Content-Type` | `415` | problem+json optional |
| Auth | `401` | |
| Rate limit | `429` | if implemented |
| Rule internal error | `500` | no stack in body |
| DB down | `503` preferred or `500` (document) |

**Failure test matrix:** `test-plan.md`.

---

## OpenAPI (1b)

- **Artifact:** `specs/openapi/openapi-1b.yaml` (ingest path + 1b field schemas where applicable). Keep aligned with 1a file or merge in tooling (see `specs/openapi/README.md`).

---

## Shipped rules (registry)

| Artifact | Role |
|----------|------|
| `rules/registry.yaml` | **Normative** list of shipped `ruleId` values + metadata (`severityOnCreate`, `titlePrefix`, `matchSemantics`). |

Add new shipped rules by extending **`registry.yaml`** and registering an evaluator in code (or ADR for dynamic plugins).
