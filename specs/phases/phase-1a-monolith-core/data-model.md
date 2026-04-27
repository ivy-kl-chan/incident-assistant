# Data model — Phase 1a (manual incidents only)

**Scope:** Persists **manually created** incidents. **Signal-specific columns** and the **`signal_ingest_audit`** table are **Phase 1b** — see `../phase-1b-signal-ingest/data-model.md` for the additive model.

**Deployment note:** A single repo may add **nullable** 1b columns in one migration for less churn; the **1a** behavior and API **must not** read or write them until 1b is delivered.

## Table: `incidents` (1a logical model)

| Column | Type | Notes |
|--------|------|--------|
| `id` | UUID | PK |
| `version` | BIGINT | **Optimistic lock**; maps to `ETag` / `If-Match` |
| `status` | VARCHAR(32) | `DRAFT`, `OPEN`, `CLOSED`, `CANCELLED` |
| `title` | VARCHAR(200) | |
| `description` | TEXT | nullable |
| `severity` | VARCHAR(8) | SEV1…SEV4 |
| `source` | VARCHAR(16) | In **1a**, default **`MANUAL`**; API never creates `SIGNAL` (1b) |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

**Semantics:** Same state meanings as the former combined spec: **no** `OPEN` from `CLOSED` in v1.

## Indexes (1a)

- `(status, created_at DESC)` for list.

## Out of scope (1a)

- `signal_fingerprint`, `created_by_rule_id`, `telemetry_context` JSON, `signal_ingest_audit` — **1b** only.

## Future (1b+)

- **1b** document adds columns + optional audit table. **Extraction** note: same repository port pattern; **no** change to 1a domain invariants for manual flows.
