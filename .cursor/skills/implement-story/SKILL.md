---
name: implement-story
description: >-
  Implements exactly one Approved phase story (`story-*.md`) for Incident
  Assistant: confirms preconditions and spec traceability, produces the
  mandated pre-code plan and test plan, implements scoped Java/Spring changes
  with tests and project invariants (interfaces for externals, mockable LLMs,
  explicit MCP, no premature remediation), updates the story artifact status,
  and delivers the post-code summary required by project rules. Use when the
  user asks to implement, code, or ship a story; to work on the active
  `stories/story-*.md`; or when execution should follow the single-story
  workflow after pre-implementation review.
---

# Implement story

## Role

- Execute **one** active implementation story end-to-end: planning, code, tests, story artifact updates, and chat deliverables defined in `.cursor/rules/incident-assistant-project.mdc`.
- Act as **implementer** aligned with the same bar **review-story-pre-implementation** (pre-code) and **review-story-implementation** (post-code) use for tech lead / principal-engineer review—not as a substitute for human sign-off on **`Complete`**.

## When to use

- User asks to **implement**, **build**, **code**, or **deliver** a specific `specs/phases/<phase>/stories/story-*.md`, or names the **active** story in context.
- Work should follow **single-story** scope: no parallel stories, no opportunistic phase expansion.

## How this skill differs from related skills

| Skill | Focus |
|-------|--------|
| **write-implementation-story** | Author or normalize **one** story file (template, fifteen sections); **no** application code. |
| **decompose-phase-stories** | Split a **phase** into many `story-*.md` files; planning-only. |
| **review-phase-story-breakdown** | Quality of the **whole** `stories/` set (ordering, independence). |
| **review-story-pre-implementation** | **Go/no-go** on the story **before** coding (traceability, risk, invariants). |
| **implement-story** (this) | **Write code and tests** for **one** Approved story; ship scoped changes and update the story per lifecycle. |
| **review-story-implementation** | Validate delivered work **after** coding against the story and specs; does not replace implementation. |

## Preconditions (do not skip)

1. **Normative specs exist** for the behavior being built (project rule: no implementation before spec).
2. The phase is decomposed into stories (**decompose-phase-stories** / **write-implementation-story**); the target file follows **write-implementation-story** canonical headings (`## 1. Status` … `## 15. Completion Notes`).
3. **Single active story:** confirm which `story-*.md` is in scope; do not implement multiple stories in one pass.
4. **Status gate:** **`## 1. Status`** should be **`Approved`** before coding. If it is `Draft` or `Planned`, stop unless the user explicitly waives the gate (same convention as **review-story-pre-implementation**).
5. Read the story in full and every artifact under **Spec References** (`spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md`, `specs/03-acceptance-criteria.md` as cited).

Optional but recommended: user has run or accepts **review-story-pre-implementation** on this story before **`Approved`**.

## Before coding (mandatory chat output)

Produce **before** editing application code (matches project rules **Before coding, always produce**):

- **Active story:** path to the single `story-*.md`.
- **Summary of requested work:** tie bullets to **In Scope** only.
- **Affected files:** align with **Files Expected to Change**; justify any addition with story scope.
- **Implementation plan:** ordered steps mapped to acceptance criteria; call out interfaces and test seams.
- **Test plan:** maps to **Test Requirements** and phase `test-plan.md`.
- **Risks and assumptions:** separate **facts** / **assumptions** / **recommendations** when material.

Then set **`## 1. Status`** to **`In Progress`** when implementation starts (per **write-implementation-story** lifecycle).

## Implementation rules (Incident Assistant invariants)

Enforce `.cursor/rules/incident-assistant-project.mdc` while coding:

- **Scope:** implement **In Scope** only; respect **Out of Scope**; no future-story or future-phase features.
- **Architecture:** monolith-first; external systems behind **interfaces**; no premature microservice splits.
- **AI / MCP / RAG (if touched):** safe, testable behavior; **all LLM calls mockable**; MCP **explicit** and testable; RAG cites sources; **no automatic remediation** in early phases.
- **Quality:** meaningful **unit and integration** tests for this story’s scope; deterministic tests.
- **Docs:** update **README** and specs when the story or project rules require behavior documentation.

## After coding (mandatory chat output)

Provide **after** implementation (matches project rules **After coding, always provide**):

- **Story status update:** set **`## 1. Status`** to **`Implemented`** when the scoped deliverable is merged or ready for review; fill **`## 15. Completion Notes`** with dates, PR/commit refs, and deviations (if any). Do **not** set **`Complete`** unless the user explicitly marks human verification done—**`Complete`** is human-only after checks pass (see **review-story-implementation**).
- **What changed:** file-scoped summary linked to acceptance criteria.
- **Tests added:** locations and what they prove.
- **Commands to run:** e.g. `./mvnw test`, integration commands per README/test-plan.
- **Known limitations:** honest gaps or follow-ups **outside** this story’s scope.
- **Acceptance criteria:** confirm each **## 10. Acceptance Criteria** item with evidence (code/test/behavior).
- **Phase acceptance:** note whether phase-level criteria apply **if** this story finishes the phase.

## Workflow checklist

Copy and track:

```
- [ ] Preconditions satisfied (specs, single Approved story, spec refs read)
- [ ] Before-coding block delivered in chat
- [ ] Status → In Progress when coding starts
- [ ] Code + tests scoped to story; invariants enforced
- [ ] Story §10 / §11 satisfied with evidence
- [ ] After-coding block delivered; Status → Implemented; §15 Completion Notes updated
- [ ] Offer or run review-story-implementation when user wants post-implementation gate
```

## Relation to post-implementation review

- For a formal **post-code** gate against the story contract, use **review-story-implementation** (diff, tests, traceability).
- If findings require backlog or ordering changes across stories, recommend **review-phase-story-breakdown** rather than ad hoc multi-story edits.

## Tone

Prefer concise, evidence-linked updates. Every change should trace to the active **`story-*.md`** and cited specs.
