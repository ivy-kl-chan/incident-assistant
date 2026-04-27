---
name: review-story-pre-implementation
description: >-
  Critically reviews one implementation story file (`story-*.md`) before coding
  as a technical lead and principal engineer: traceability to phase specs,
  implementation risk, architecture and interfaces, testability, security and
  operability, and alignment with Incident Assistant project rules (AI safety,
  mockable LLMs, explicit MCP, no premature remediation). Use when the user
  asks to review, grill, or sign off a single story before implementation; wants
  a pre-implementation gate or go/no-go on one `stories/story-*.md`; or mentions
  principal-engineer or tech-lead review of the active story only (not the whole
  phase breakdown).
---

# Review story before implementation (tech lead + principal engineer)

## Role and defaults

- Act as **technical lead** and **principal engineer**: direct, evidence-based, no fluff; weigh **delivery risk**, **maintainability**, **operability**, and **correctness** together.
- **Do not implement application code** or edit repository files unless the user explicitly asks for edits in a separate instruction. (Updating the story markdown after review is allowed only when the user asks for it.)
- Anchor feedback with **story file path**, **phase spec paths**, **section headings**, and short quotes only when needed.
- Separate **facts** (what the story or spec says), **assumptions** (what you infer), and **recommendations** (what to change before coding).

## How this skill differs from others

| Skill | Focus |
|-------|--------|
| **review-phase-story-breakdown** | Entire `stories/` set for a phase: coverage, ordering, independence across stories. |
| **review-spec-chat-scope** | Spec documents tied to the chat; not necessarily one story file. |
| **review-story-pre-implementation** (this) | **One** active `story-*.md` as a **pre-coding gate**: is this story ready to implement safely and traceably? |

## Scope

1. **In scope**: The user names **one** `specs/phases/<phase>/stories/story-*.md` (or the agent infers the **active** story from context). Read:
   - That story file in full.
   - Every artifact listed under **Spec References** (open and read those sections or files).
   - Phase normative specs when the story touches behavior not fully specified in the cited lines: typically `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md`, and the phase hub (`README.md` / hub `spec.md`).
   - `specs/03-acceptance-criteria.md` for the sub-phase when the story claims phase acceptance alignment.
   - `.cursor/rules/incident-assistant-project.mdc` when checking **project invariants** (interfaces for externals, mockable LLMs, explicit MCP, RAG citations, no automatic remediation in early phases, spec-driven workflow).
2. **Cross-phase**: Read `specs/02-roadmap.md` or later phase folders **only** to flag **future-phase creep** in **In Scope**; cite paths when flagging.
3. **If the story path is missing**: Ask which `story-*.md` to review before proceeding.

## Workflow

1. Confirm **story file path** and **phase id**; list all spec files and sections read.
2. Verify the story matches **write-implementation-story**: all **fifteen** sections in **Canonical section headings** are present **in order** with exact `##` titles; **Status** appropriate for pre-implementation (e.g. expect `Approved` or explicit user waiver if still `Draft`).
3. **Traceability pass**: each **In Scope** bullet should map to **Spec References**; each **Acceptance Criteria** item should be objectively verifiable; **Out of Scope** should deflect known adjacent work.
4. **Principal-engineer pass** (single story): apply the **nine lenses** below; flag blockers vs nice-to-haves.
5. Produce **required output** in order. End with **go / no-go** and **questions for human approval**.

## Nine lenses (single story)

Answer each for **this story only**. If not applicable: one line, “N/A — [reason].”

1. **Spec and phase alignment**: Do cited specs actually require what **In Scope** promises? Any contradiction with `api-contract.md` / `data-model.md` / `test-plan.md`?
2. **Scope discipline**: Vague **In Scope** bullets; missing **Out of Scope**; hidden work bundled (docs, migrations, unrelated refactors).
3. **Architecture and boundaries**: Fits monolith-first; clear module seams; abstractions for **external dependencies** per project rules; microservice boundaries not violated prematurely.
4. **AI / MCP / RAG (if touched)**: Safe, testable behavior; **LLM calls mockable**; MCP explicit and testable; RAG answers cite sources; no disallowed automatic remediation for this phase.
5. **API and data**: **API Changes** and **Data Model Changes** complete and consistent with contracts; error and empty states implied by acceptance criteria where relevant.
6. **Testability**: **Test Requirements** are specific enough to implement; acceptance criteria map to **unit / integration / contract** tests as appropriate; test data and seams (interfaces, fakes) identified in **Implementation Notes** or flag the gap.
7. **Operational and security posture**: Logging/metrics/actuator implications if applicable; secrets and auth; PII or sensitive data handling called out when relevant.
8. **Dependencies and ordering**: Relies on prior stories, migrations, or flags? If yes, are they **done** or explicitly documented? Risk of **implementing before prerequisites**?
9. **Files and churn**: **Files Expected to Change** plausible; blast radius acceptable; no silent cross-cutting edits outside listed files unless justified in story.

## Alignment with authoring skills

- Story body is expected to follow **write-implementation-story** (fifteen **Canonical section headings** in order, plus checklist blocks from the template).
- If findings imply **splitting** or **renumbering** the whole phase backlog, call that out and suggest running **review-phase-story-breakdown**—do not silently rewrite other stories unless the user asks.

## Required output

Use these **markdown headings** in order:

### Scope

Story file path, phase directory, list of spec files (and sections) read.

### Verdict

One line: **Go** | **Go with conditions** | **No-go**, with the primary reason in the same paragraph.

### Critical feedback

Blockers for safe implementation: missing traceability, spec contradictions, untestable acceptance criteria, unsafe ordering, or invariant violations (project rules).

### Required changes before coding

Concrete edits to **this story** and/or **phase specs** (file-scoped bullets; each item actionable).

### Optional improvements

Clarifications, splits, or engineering hygiene that are not strict blockers.

### Principal-engineer notes

Short bullets on **risk**, **maintainability**, or **operations** the implementer should keep in mind during coding (even if the story is a Go).

### Questions for human approval

Explicit decisions only a human should make (phase boundary, risk acceptance, product ambiguity, controversial tradeoffs).

## Tone

Short paragraphs and bullets. Every major finding ties to **the story file** or a **spec path/section**. Do not write Java/Spring implementation or code patches unless the user asks for implementation help outside this review.
