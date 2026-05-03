# Data model — Phase 1b (additive to 1a)

**Prerequisites:** `../phase-1a-monolith-core/data-model.md` — **same** physical **`incidents`** table (baseline **`V1`** migration already includes nullable signal columns and optional **`signal_ingest_audit`** / **`signal_ingest_idempotency`** per this doc). **1b** turns on use of those columns; **no second versioned migration** is required for greenfield if **`V1`** was written to the combined shape.

**PostgreSQL** is **normative** for **1b** behavior in this document (locks, partial indexes, JSONB). Other databases require an explicit **ADR** / README divergence note.

## Columns used by 1b on `incidents`

Physical columns are created in **`V1`** (`../phase-1a-monolith-core/data-model.md`). This table is the **1b** semantics on those fields.

| Column | Type | Notes |
|--------|------|--------|
| `created_by_rule_id` | VARCHAR(128) | nullable; set when `source=SIGNAL` |
| `signal_fingerprint` | VARCHAR(64) | nullable; **64** lowercase hex; **no** global `UNIQUE` (Option A) |
| `telemetry_context` | JSONB | nullable; max **8 KiB** stored (normative DDL: **JSONB**; **TEXT** only with ADR) |

`source` may be **`SIGNAL`** for signal-created rows.

## `signal_fingerprint` (normative)

```
canonical = stable_sorted_json({ "ruleId", "fingerprintInputs" })
signal_fingerprint = lowercase_hex(SHA-256(canonical))  // 64 hex chars
```

## Telemetry JSON (stored in `telemetry_context`)

Same shape and validation as the prior monolithic spec:

- `traceId`, `spanId`, `serviceName`, `windowStart`, `windowEnd`, `deepLinks.jaegerQuery`, `deepLinks.grafanaDashboard`
- **No secrets** in `deepLinks` (see `api-contract.md` security note in 1a — same discipline).

## Dedup policy, matrix, races

**Option A (default):** see matrix below. **Option B** only via ADR.

**Cooldown** `SIGNAL_DEDUP_COOLDOWN` default **15 minutes**.

**Concurrency:** within the transaction that applies the matrix below, take **`pg_advisory_xact_lock`** on a **64-bit key** derived from **`signal_fingerprint`**. **Normative mapping:** parse the **64**-char lowercase **hex** `signal_fingerprint` as **32** bytes; take the first **8** bytes as a **big-endian unsigned int64** advisory key (or equivalently XOR-split the 32-byte digest into four uint64s and **XOR** fold to one uint64—document one approach in code comments). **Collision note:** different fingerprints **could** map to the same 64-bit key in theory; risk is negligible for this demo; use the **full** fingerprint in SQL predicates so correctness does not rely on the advisory key being injective.

### Normative ingest algorithm (Option A, PostgreSQL)

Let **`W`** = cooldown window from `SIGNAL_DEDUP_COOLDOWN`, **`fp`** = computed `signal_fingerprint`, **`t0`** = `observedAt` (for logging only unless ADR says otherwise), **`now()`** = DB transaction time.

1. **Begin** transaction.
2. **`pg_advisory_xact_lock`(derive_key(`fp`))`**.
3. **Select** from `incidents` where `signal_fingerprint` = **`fp`** and `created_at` ≥ **`now()` − `W`**, order by `created_at` **desc**, limit **1** → row **`R`** (may be null).
4. If **`R`** is null → **insert** new `DRAFT` with `source=SIGNAL`, set fingerprint + telemetry + rule id → **`201`** (body per `api-contract.md`).
5. Else apply **matrix** (same `fp`, within **`W`**) using **`R.status`**:

| `R.status` | Action |
|------------|--------|
| `DRAFT` | **`200`** duplicate → same `incidentId`, `reason: DUPLICATE_SIGNAL` |
| `OPEN` | **`201`** new `DRAFT` (new episode) |
| `CLOSED` | **`201`** new `DRAFT` |
| `CANCELLED` | **`201`** new `DRAFT` |

6. **Commit** transaction.

**Integration test:** two parallel ingests (same **`fp`**, rule matched, empty pre-state) → **exactly one** new `DRAFT` row.

**Dedup lookup (summary):** “in-window” means **`created_at` ≥ `now()` − `W`** for the same **`signal_fingerprint`**; “most recent” drives the matrix when multiple rows exist (step 3).

### `200` vs `201` matrix (second ingest, same `signal_fingerprint` within `W`, rule would match)

| Most recent in-window row | Option A (default) | Option B (ADR) |
|--------------------------|--------------------|----------------|
| `DRAFT` | `200` → same `incidentId` | same |
| `OPEN` | `201` → new `DRAFT` (new episode) | `200` → that `incidentId` |
| `CLOSED` | `201` new `DRAFT` | `200` that id |
| `CANCELLED` | `201` new `DRAFT` | `200` that id |

## Signal-created defaults

| Field | Source |
|-------|--------|
| `title` | `titlePrefix` from **`rules/registry.yaml`** entry for `created_by_rule_id` + `serviceName` / fallback **`[Signal] {ruleId}`**; max **200** |
| `description` | `summary` or null |
| `severity` | `severityOnCreate` from **`rules/registry.yaml`** for the rule in use |
| `source` | `SIGNAL` |

## Table: `signal_ingest_audit` (optional)

| Column | Type |
|--------|------|
| `id` | BIGSERIAL |
| `received_at` | TIMESTAMPTZ |
| `rule_id` | VARCHAR(128) |
| `matched` | BOOLEAN |
| `incident_id` | UUID nullable |
| `dedup_hit` | BOOLEAN |
| `payload_hash` | VARCHAR(64) |

Store **hash** of payload, not raw body, if PII risk.

## Table: `signal_ingest_idempotency` (optional, recommended if `Idempotency-Key` supported)

Supports **`Idempotency-Key`** replay per `api-contract.md`. Create in **`V1`** if ingest idempotency is implemented.

| Column | Type | Notes |
|--------|------|--------|
| `idempotency_key_hash` | CHAR(64) | lowercase hex SHA-256 of header value (**PK**; one active replay slot per key for this demo) |
| `body_hash` | CHAR(64) | lowercase hex SHA-256 of canonical request body bytes |
| `http_status` | SMALLINT | stored replay status (**200** or **201** for cached successes) |
| `response_body` | JSONB or TEXT | exact JSON body to replay |
| `created_at` | TIMESTAMPTZ | for **`INGEST_IDEMPOTENCY_TTL`** expiry |

**Primary key:** **`idempotency_key_hash`**. **Conflict (HTTP 409):** on ingest, if a row exists for this **`idempotency_key_hash`**, is **not** expired, and **`body_hash`** **≠** stored **`body_hash`**, reject (same key, different body). If **`body_hash`** matches, replay **`http_status`** + **`response_body`** per `api-contract.md`.

### Request handling order (informative)

1. Auth, **`Content-Type`**, parse JSON.  
2. **`Idempotency-Key`** lookup / conflict (**`api-contract.md`**).  
3. Validate **`ruleId`** and payload.  
4. Evaluate rule match; if not matched, respond **`200 { "matched": false }`** (and persist idempotency row if applicable).  
5. If matched, run **fingerprint + transaction + advisory lock + dedup matrix** (below).

## Indexes

- **`(signal_fingerprint, created_at DESC)`** partial where fingerprint is not null — same index referenced from **1a** `data-model.md` (**`V1`** DDL).

## Microservice note

- **`SignalIngestPort`**: keep behind interface; future service hosts same contract.
