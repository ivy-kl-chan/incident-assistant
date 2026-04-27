# Data model — Phase 1b (additive to 1a)

**Prerequisites:** `../phase-1a-monolith-core/data-model.md` base `incidents` table.

1b **adds** (migration `V2` or equivalent) — either new columns on `incidents` + optional `signal_ingest_audit`, or same in one step if 1a used “reserved nulls”.

## Additive columns: `incidents`

| Column | Type | Notes |
|--------|------|--------|
| `created_by_rule_id` | VARCHAR(128) | nullable; set when `source=SIGNAL` |
| `signal_fingerprint` | VARCHAR(64) | nullable; **64** lowercase hex; **no** global `UNIQUE` (Option A) |
| `telemetry_context` | JSONB or TEXT | nullable; max **8 KiB** stored |

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

**Concurrency:** `pg_advisory_xact_lock` (PostgreSQL) around read-then-insert; integration test: parallel ingests same fingerprint → **one** new `DRAFT` when expected.

**Dedup lookup:** rows with same `signal_fingerprint` and `created_at` within `now() - cooldown` (see full algorithm in team ADR; behavior matches table).

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
| `title` | `titlePrefix` from `rules/demo-rule-v1.yaml` + `serviceName` / fallback `[Signal] {ruleId}`; max **200** |
| `description` | `summary` or null |
| `severity` | `severityOnCreate` from registry (default `SEV3` for `demo.otel.signal_v1`) |
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

## Indexes (add to 1a)

- `(signal_fingerprint, created_at DESC)` partial where fingerprint not null.

## Microservice note

- **`SignalIngestPort`**: keep behind interface; future service hosts same contract.
