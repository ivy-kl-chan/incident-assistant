# Implementation plan — Phase 1a (no 1b)

**No code** — order of work only.

## Preconditions

- README “Before Phase 1a” answered where relevant (build tool, DB story).

## Milestones (1a)

| Step | Deliverable |
|------|-------------|
| M1 | Scaffold, Actuator (restricted), Flyway **`V1` only** — single baseline SQL per `data-model.md` (1a columns + nullable 1b columns + optional audit + indexes from 1b doc) |
| M2 | Domain + persistence (manual incidents only) |
| M3 | HTTP: incidents API per `api-contract.md`; **no** `POST /api/v1/signal-ingest/*` route registered (ingest **absent** in 1a) |
| M4 | `specs/openapi/openapi-1a.yaml` complete for 1a paths; Problem Details |
| M5 | Dockerfile + `docker-compose` app + PostgreSQL, README |
| M6 | **1a gate:** `specs/03-acceptance-criteria.md` Phase **1a** + `test-plan.md` green |

## Does not start until 1a done

- Any work under `specs/phases/phase-1b-signal-ingest/`.
