---
name: decompose-phase-stories
description: >-
  Breaks an approved roadmap phase into small, independently reviewable
  implementation story markdown files under specs/phases/<phase>/stories/,
  using the write-implementation-story canonical template for each file. Use
  when the user asks to decompose a phase into stories, create story files, or
  align backlog work with phase specs and acceptance criteria—without writing
  application code.
---

# Phase → implementation stories

## When to use

- User requests story breakdown for a phase (e.g. “break Phase 1 into stories”).
- Specs exist; work is **planning-only** (no Spring/Java/code in this workflow unless the user explicitly asks).

## Story file format (mandatory)

Before writing any `story-*.md` file, **read and follow** [write-implementation-story/SKILL.md](../write-implementation-story/SKILL.md). Phase breakdown **uses** that skill’s verbatim template: title `# Story <n>: …`, then the **fifteen** numbered `##` headings in **write-implementation-story → Canonical section headings** (`## 1. Status` through `## 15. Completion Notes`, same order, exact titles), Status lifecycle, Spec References bullets, and the checkbox blocks under **Acceptance Criteria**, **Test Requirements**, and **Human Review Checklist**. **Do not** use unnumbered section titles (e.g. `## Status` without the digit) for new or rewritten stories.

## Inputs (read in order)

1. **Phase hub:** `specs/phases/<phase-folder>/README.md` (or `spec.md` if the hub only redirects).
2. **Definition of done:** `specs/03-acceptance-criteria.md` — sections for each **sub-phase** included in the breakdown (e.g. 1a and 1b).
3. **Authoritative phase specs:** every `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md`, `implementation-plan.md` linked from the phase hub.
4. **Roadmap context:** `specs/02-roadmap.md` for ordering and dependencies between sub-phases.

## Output location

- Create **`stories/`** under the phase folder (e.g. `specs/phases/phase-1-monolith-mvp/stories/`).
- One file per story: **`story-<n>-<short-kebab-name>.md`** with sequential `n` starting at **1**.

## Fifteen traceability areas (content checklist)

Each story must include **every** heading in **write-implementation-story → Canonical section headings**, in order, with no omissions. For each section, fill as follows:

1. **`## 1. Status`** — one value from the lifecycle in write-implementation-story (e.g. `Draft` for new files).
2. **`## 2. Goal`** — one concrete outcome for this story alone.
3. **`## 3. User Value`** — who benefits and how (operators, reviewers, integrators).
4. **`## 4. Spec References`** — bullets for `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md` as applicable; add `03-acceptance-criteria.md`, OpenAPI, ADRs when relevant.
5. **`## 5. In Scope`** — deliverables for **this** story only.
6. **`## 6. Out of Scope`** — explicit exclusions (next story / future phase).
7. **`## 7. API Changes`** — routes, headers, status codes; **None** if not applicable.
8. **`## 8. Data Model Changes`** — entities, migrations, constraints; **None** if not applicable.
9. **`## 9. Business Rules`** — state, auth, validation, concurrency this story touches.
10. **`## 10. Acceptance Criteria`** — checkboxes; objectively verifiable.
11. **`## 11. Test Requirements`** — use write-implementation-story’s checkbox lines; tie substance to `test-plan.md` where it applies.
12. **`## 12. Files Expected to Change`** — globs or concrete paths.
13. **`## 13. Implementation Notes`** — ordering, dependencies, edge cases; **no code blocks** unless illustrating a contract snippet already in specs.
14. **`## 14. Human Review Checklist`** — that skill’s checkbox block verbatim.
15. **`## 15. Completion Notes`** — placeholder until implemented.

## Rules

- **Small stories:** each story should be reviewable in one PR-sized chunk; split by vertical slice or by clear seam (schema vs HTTP vs OpenAPI vs tests).
- **Traceability:** every **In Scope** item must map to at least one **Spec References** row; acceptance criteria must map to **03-acceptance-criteria.md** or phase spec bullets.
- **Independence:** prefer stories that can be implemented and tested without starting the *next* story; when impossible, state **hard dependencies** in **Implementation Notes** and in the chat summary (dependency graph).
- **Future phases:** do not scope Phase 2+ product behavior. If a spec mentions later work, list it under **Out of Scope** or a one-line **design note** in **Implementation Notes**.
- **Acceptance-driven exceptions:** when `03-acceptance-criteria.md` mandates a deliverable that uses a specific technology (e.g. container image, demo repro), put that work in **dedicated** story(ies) whose **Goal** cites the acceptance section by path—do not hide mandatory DoD inside unrelated stories and do not mark it “out of scope” without a documented product decision.

## After creating or updating stories (in chat)

Provide for the human:

1. **Recommended implementation order** — ordered list of `story-n-*.md` titles.
2. **Dependencies** — which stories block which (DAG or bullet list).
3. **Risks** — spec gaps, ordering risks, test/env brittleness.
4. **Human approval questions** — explicit decisions needed before implementation (e.g. deferrals vs acceptance criteria).

## Anti-patterns

- One giant “implement Phase 1” story.
- Stories with no **Out of Scope** (scope creep).
- Acceptance criteria that cannot be tested or verified.
- Implementing or generating Spring Boot sources as part of this skill (out of scope unless the user asks).
