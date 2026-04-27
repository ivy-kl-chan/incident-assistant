# Data model — Phase 1a (manual incidents only)

**Scope:** Persists **manually created** incidents in **1a**. **Signal-backed rows** and ingest are **Phase 1b** — see `../phase-1b-signal-ingest/data-model.md` for semantics on shared columns.

**Migrations (normative for this repo):** use **one** Flyway versioned script **`V1__...sql`** (single “baseline” migration) that creates the **`incidents`** table in its **final** shape for **1a+1b**: all **1a** columns plus **nullable** `created_by_rule_id`, `signal_fingerprint`, `telemetry_context`, and optional **`signal_ingest_audit`** and **`signal_ingest_idempotency`** (see **`../phase-1b-signal-ingest/data-model.md`**), with indexes from that doc. **1a** application code **must not** read or write 1b-only columns or ingest until **1b** is delivered; unused columns stay **null**.

## Table: `incidents` (physical `V1` — 1a + reserved nulls)

The **`V1`** script creates the full row shape below; **1a** code uses only the subset documented in **Out of scope (1a) — application**.

| Column | Type | Notes |
|--------|------|--------|
| `id` | UUID | PK |
| `version` | BIGINT | **Optimistic lock**; maps to `ETag` / `If-Match` |
| `status` | VARCHAR(32) | `DRAFT`, `OPEN`, `CLOSED`, `CANCELLED` |
| `title` | VARCHAR(200) | |
| `description` | TEXT | nullable |
| `severity` | VARCHAR(8) | SEV1…SEV4 |
| `source` | VARCHAR(16) | Default **`MANUAL`**; **1a** never writes **`SIGNAL`** |
| `created_by_rule_id` | VARCHAR(128) | nullable; **unused** in **1a** |
| `signal_fingerprint` | VARCHAR(64) | nullable; **unused** in **1a** |
| `telemetry_context` | JSONB or TEXT | nullable; **unused** in **1a** |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

**Semantics:** Same state meanings as the former combined spec: **no** `OPEN` from `CLOSED` in v1.

## Indexes

- **`(status, created_at DESC)`** — list (1a).
- **Partial index on `(signal_fingerprint, created_at DESC)`** where fingerprint is not null — required for **1b** dedup lookup (`../phase-1b-signal-ingest/data-model.md`); safe no-op for **1a** workloads.

## Out of scope (1a) — application

- Using **`source = SIGNAL`**, ingest, fingerprinting, and **`signal_ingest_audit`** writes — **1b** only (DB columns may already exist as null).

## Future (1b+)

- **`signal_ingest_audit`** (optional) and ingest semantics: **`../phase-1b-signal-ingest/data-model.md`**. **Extraction** note: same repository port pattern; **no** change to **1a** domain invariants for manual flows.
