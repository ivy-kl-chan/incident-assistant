# Implementation plan — Phase 1a (no 1b)

**No code** — order of work only.

## Preconditions

- README “Before Phase 1a” answered where relevant (build tool, DB story).

## Milestones (1a)

| Step | Deliverable |
|------|-------------|
| M1 | Scaffold, Actuator (restricted), Flyway `V1__incidents_1a.sql` matching `data-model.md` |
| M2 | Domain + persistence (manual incidents only) |
| M3 | HTTP: incidents API per `api-contract.md`; **no** `POST /signal-ingest/*` (omit controller or 404) |
| M4 | `specs/openapi/openapi-1a.yaml` complete for 1a paths; Problem Details |
| M5 | Dockerfile + `docker-compose` app + PostgreSQL, README |
| M6 | **1a gate:** `specs/03-acceptance-criteria.md` Phase **1a** + `test-plan.md` green |

## Optional: DB forward-compat

- One migration can add **nullable** 1b columns if an ADR says so; feature flags must keep **1a** API from returning **SIGNAL** or ingest paths until 1b.

## Does not start until 1a done

- Any work under `specs/phases/phase-1b-signal-ingest/`.
