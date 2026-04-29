---
name: review-story-implementation
description: >-
  Critically reviews one implemented story against its `story-*.md` contract and
  referenced phase specs: scope fidelity, acceptance and test evidence, API/data
  alignment, architecture boundaries, safety rules, and phase creep. Use when the
  user asks to review, sign off, or validate implementation of a specific story;
  wants a post-implementation gate for one story; or asks whether delivered code
  actually satisfies that story before marking it complete.
---

# Review specific story implementation

## Role and defaults

- Act as **technical lead** and **principal engineer**: direct, evidence-based, no fluff.
- Review **one** story’s delivered implementation for correctness, scope discipline, and readiness to mark complete.
- **Do not implement code** or edit repository files unless the user explicitly asks in a separate instruction.
- Separate **facts** (observed in story/spec/code/tests), **assumptions** (inferred), and **recommendations** (required fixes or follow-ups).

## How this skill differs from related skills

| Skill | Focus |
|-------|--------|
| **review-story-pre-implementation** | One story quality gate **before coding**. |
| **review-phase-story-breakdown** | Entire phase story set quality, sequencing, and coverage. |
| **review-story-implementation** (this) | One story **after implementation**: does code and test evidence match the story and specs? |

## Scope

1. **In scope**: one `specs/phases/<phase>/stories/story-*.md` and its implementation evidence:
   - Story file in full.
   - Files actually changed for that story (working tree diff, commit range, or PR diff as provided by user context).
   - Story **Spec References** artifacts (`spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md`, etc.).
   - Relevant tests and test outputs for this story.
2. **Cross-phase check**: read `specs/02-roadmap.md` or later phases only to flag future-phase creep.
3. **If story path is missing**: ask which single `story-*.md` to review.
4. **If implementation evidence is missing** (no diff/commit/PR/test result available): return **No-go** and list the minimum required artifacts before review can proceed.

## Workflow

1. Confirm the **story file path** and **implementation evidence source** (diff/commits/PR scope).
2. Verify story template compliance with **write-implementation-story** canonical numbered headings (`## 1. Status` … `## 15. Completion Notes`) and status lifecycle expectations.
3. Build a traceability map:
   - **In Scope** -> changed files/code paths.
   - **Acceptance Criteria** -> concrete implementation and tests.
   - **Out of Scope** -> verify no accidental implementation.
4. Run the review lenses below and classify findings by severity.
5. Return required output sections in order, ending with **go/no-go for story completion status**.
6. Treat `Complete` as a **human-only** status decision after PR checks pass and code is merged.

## Review lenses (post-implementation, single story)

1. **Scope fidelity**: implemented behavior matches **In Scope**; no hidden expansions.
2. **Acceptance evidence**: each acceptance criterion is demonstrably satisfied (code + tests + observable behavior).
3. **Test strength**: tests are meaningful, deterministic, and aligned to `Test Requirements`; mocks/fakes and seam usage obey project rules.
4. **API and contract compliance**: HTTP behavior and error model align with `api-contract.md` (if applicable).
5. **Data integrity**: schema, constraints, and persistence behavior align with `data-model.md` and migration intent.
6. **Architecture boundaries**: monolith-first boundaries respected; externals behind interfaces; no premature microservice coupling.
7. **AI/MCP/RAG invariants (if touched)**: mockable LLM calls, explicit/testable MCP usage, source-cited RAG outputs, no forbidden automatic remediation in early phases.
8. **Operational/security posture**: logging, actuator/metrics implications, auth/secrets/PII handling, and failure paths are acceptable for this phase.
9. **Phase creep and omissions**: identify future-phase work accidentally included, and promised scope missing from implementation.

## Alignment with project skills and rules

- `write-implementation-story` remains canonical for section structure and status values.
- `review-story-pre-implementation` is the gate before coding; this skill validates delivery after coding.
- When fixes imply splitting or reordering multiple stories, recommend `review-phase-story-breakdown` instead of broad ad-hoc edits.
- Enforce `.cursor/rules/incident-assistant-project.mdc` invariants (spec-driven delivery, explicit MCP, mockable LLMs, tested stories, no opportunistic scope).

## Required output

Use these markdown headings in order:

### Scope

Story path, implementation evidence examined, spec files read, and test evidence reviewed.

### Verdict

One line: **Go** | **Go with conditions** | **No-go** for marking the story `Complete`, with primary reason.

### Critical feedback

Blocking correctness/safety/traceability issues with file-scoped evidence.

### Required changes

Actionable changes required before setting story status to `Complete`.

### Optional improvements

Non-blocking maintainability, operability, or clarity improvements.

### Story status recommendation

Recommended next status value (`In Progress`, `Implemented`, or `Reviewed`) and why.
Do **not** recommend `Complete`; only a human may mark `Complete` after PR checks pass and the code is merged.

### Questions for human approval

Human-only decisions (scope acceptance, risk acceptance, phase-boundary tradeoffs).

## Tone

Use concise, evidence-linked bullets and short paragraphs. Tie every major finding to a story section, spec path, changed file, or test artifact. Do not provide implementation patches unless the user asks for code changes outside this review.
