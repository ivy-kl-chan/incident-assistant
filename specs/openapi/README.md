# OpenAPI

| File | Phase | Normative docs |
|------|--------|----------------|
| `openapi-1a.yaml` | **1a** | `phases/phase-1a-monolith-core/api-contract.md` |
| `openapi-1b.yaml` | **1b** (ingest) | `phases/phase-1b-signal-ingest/api-contract.md` |

**Full API (after 1b):** merge **1a + 1b** in your tooling, or a future **`openapi-v1-combined.yaml`** if you add a generator. Do **not** treat a single file as authoritative unless it explicitly lists both phase docs in its description.

**Phase 1a gate:** `openapi-1a.yaml` is complete and matches controllers. **Phase 1b gate:** add/update `openapi-1b.yaml` and extend incident schemas per `phase-1b` contract.
