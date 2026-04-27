# Review notes — Phase 1b

| Topic | Spec |
|-------|------|
| Dedup Option A, matrix, advisory lock | `data-model.md` |
| Ingest token + constant-time | `api-contract.md` |
| `200` not matched | `api-contract.md` |
| Shipped rules + startup bind | `rules/registry.yaml` + `api-contract.md` |
| Idempotency + list `source` | `api-contract.md` |
| PG normative / H2 divergence | `api-contract.md` § Extensions |
| **No** LLM on ingest path | `ai-behavior.md` |

**Split from former monolith pack** — 1a-only items in `../phase-1a-monolith-core/review-notes.md`.

## Changelog

| Date | Note |
|------|------|
| 2026-04-26 | Split: 1b folder only |
| 2026-04-26 | `openapi-1b.yaml`: list + get-by-id + ingest; merge rules in `openapi/README.md` |
| 2026-04-26 | Kickoff: webhook ingest, minimal OTel profile, metrics→traces→logs stories (`docs/adr/0002-…`) |
