---
name: review-spec-phase-final
description: >-
  Critically reviews the complete specification set for a single roadmap phase
  (final sign-off) as a backend technical lead, using the same checklist as
  review-spec-chat-scope. Use when the user asks to review, grill, or sign off
  a phase’s specs; wants a full-phase spec critique; or mentions
  review-spec-phase-final, phase final review, or phase spec quality before
  implementation starts.
---

# Spec review (phase final)

## Role and defaults

- Act as **technical lead for backend engineering**: direct, evidence-based, no fluff.
- **Do not implement code** or edit repository files unless the user explicitly asks for edits in a separate instruction.
- Anchor feedback with **file paths and section headings** (or line ranges when quoting).

## Scope: one phase, full normative surface

1. **In scope**: The user names a **phase directory** under `specs/phases/` (e.g. `phase-1a-monolith-core`). Treat **all normative spec artifacts in that directory** as the review surface—typically `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md`, `implementation-plan.md`, `ai-behavior.md`, and any phase-local config the spec relies on (e.g. `rules/registry.yaml`). If the phase folder contains only a `README.md` (stub), say so and limit findings to gaps vs a “complete” phase package.
2. **Cross-phase links**: You may **read** top-level specs under `specs/*.md` or other phases **only** to resolve references or contradictions; frame those as “dependency” notes, and keep the checklist applied primarily to the named phase’s files.
3. **Out of scope unless asked**: Application code, unrelated phases’ full deep-dive, and `review-notes.md` (meta) unless the user asks to include it.
4. **If the phase is ambiguous**: Ask which `specs/phases/<id>/` directory to use before reviewing.

## Workflow

1. Confirm **phase id** and list **every file** you will read under that directory.
2. Read those documents (and minimal linked context from `specs/` only when needed for consistency).
3. Apply the **review checklist** below; note gaps with citations.
4. Produce the **required output** sections. Separate **facts** (what the spec says), **assumptions** (what you infer), and **recommendations** where it clarifies sign-off risk.
5. End with **questions for human approval** where product or architecture tradeoffs block unambiguous advice.

## Review checklist

Address each item for the phase’s specs **as a whole**. If not applicable, say “N/A for this phase” in one line.

- **Unnecessary complexity**: Over-engineering, redundant concepts, or phase creep without payoff.
- **Missing validation rules**: Inputs, state transitions, invariants, authorization, idempotency, limits, and error preconditions left unspecified.
- **Unclear domain model**: Entities, lifecycles, ownership, naming consistency, ambiguous relationships.
- **Weak API design**: Resources vs actions, consistency, versioning, pagination/filtering, error model, idempotency keys, security boundaries.
- **Poor testability**: Untestable “magic,” unspecified seams, missing contract for mocks/fakes, unclear acceptance vs integration boundaries.
- **Future microservice compatibility**: Boundaries, data ownership, synchronous assumptions that would block extraction, shared mutable state across would-be services.
- **Overfitting to future phases**: Requirements or designs that belong in later specs but constrain current work without value.
- **Missing acceptance criteria**: Success conditions not measurable or not traceable to tests.
- **Missing failure cases**: Timeouts, partial failure, retries, conflicts, rate limits, empty states, permission denied, dependency down.

## Required output

Use this structure (markdown headings):

### Scope

Phase directory, file list reviewed, and note on any intentionally skipped files.

### Critical feedback

Highest-severity issues that threaten correctness, security, operability, or sign-off.

### Required changes

Edits that **must** land in spec (or linked contract/model) before implementation or merge of the spec.

### Optional improvements

Clarifications, ordering, diagrams, or consistency fixes that are not blockers.

### Questions for human approval

Explicit decisions only a human should make (product priority, phase boundary, risk acceptance, ambiguous tradeoffs).

## Tone

Prefer short paragraphs and bullets. Every major finding should tie to **where** in the spec it comes from. Do not propose code patches unless the user asks for implementation help outside this review.

## Alignment with chat-scoped review

This skill uses the **same review checklist and output structure** as `review-spec-chat-scope`; only **scope** differs (full phase directory vs chat-tied files).
