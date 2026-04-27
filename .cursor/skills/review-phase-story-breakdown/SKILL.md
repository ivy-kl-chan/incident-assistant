---
name: review-phase-story-breakdown
description: >-
  Critically reviews a phase’s implementation story set (all `story-*.md`
  under `specs/phases/<phase>/stories/`) against the phase specs and roadmap:
  story size, independence, scope, dependencies, breadth, acceptance and test
  strength, spec coverage, future-phase creep, and order. Use when the user asks
  to grill, sign off, or improve a story breakdown; to review Phase 1 (or any
  phase) stories before implementation; or mentions story order, story scope,
  hidden dependencies, or weak acceptance criteria for `stories/`.
---

# Review phase story breakdown (“grill stories”)

## Role and defaults

- Act as **technical lead for backend engineering**: direct, evidence-based, no fluff.
- **Do not implement application code** or edit repository files unless the user explicitly asks for edits in a separate instruction. (Updating story markdown after review is allowed only when the user asks for it.)
- Anchor feedback with **story file paths** (`story-*.md`), **phase spec paths**, and **section headings** (or short quotes when needed).
- When helpful, separate **facts** (what a story or spec says), **assumptions** (what you infer), and **recommendations** (what to change).

## Scope

1. **In scope**: The user names a **phase folder** under `specs/phases/` (e.g. `phase-1-monolith-mvp`). Read:
   - **Every** `specs/phases/<phase>/stories/story-*.md` file in that phase.
   - The phase’s normative specs: typically `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md`, `implementation-plan.md`, and the phase hub (`README.md` or hub `spec.md`).
   - `specs/03-acceptance-criteria.md` for the sub-phase(s) this breakdown claims to satisfy.
   - `specs/02-roadmap.md` for declared phase boundaries and ordering between sub-phases.
2. **Cross-phase**: Read other phases or top-level `specs/*.md` **only** to check **future-phase creep** or resolve contradictions; cite those paths when flagging leakage.
3. **If `stories/` is missing or empty**: Say so; list what must exist before this review is meaningful; optionally review whether the **phase specs alone** imply a sensible story split (still no code).
4. **If the phase is ambiguous**: Ask which `specs/phases/<id>/` directory to use before reviewing.

## Workflow

1. Confirm **phase id** and list **all** `story-*.md` files and **all** phase spec files you read.
2. Skim `02-roadmap.md` and `03-acceptance-criteria.md` for the intended **phase boundary** and **definition of done**.
3. For each story file, check template completeness against **write-implementation-story** (fifteen traceability areas: Status through Completion Notes; no omitted `##` sections).
4. Apply the **ten evaluation questions** below across the **whole story set** (not only per file): coverage and ordering are **set-level** concerns.
5. Produce the **required output** sections in order. End with **questions for human approval** where product or architecture choices block unambiguous advice.

## Ten evaluation questions

Answer each for the **story set as a whole**, with per-story examples where useful. If not applicable: one line, “N/A — [reason].”

1. **Are the stories small enough?** Prefer PR-sized, reviewable slices; flag stories that bundle multiple vertical slices, unrelated endpoints, or schema + behavior + docs in one lump without a seam.
2. **Is each story independently testable?** Can acceptance criteria be verified without implementing a **later** story? Flag coupling to unpublished APIs, missing test seams (interfaces/fakes), or “manual only” verification.
3. **Does each story have clear scope?** **In Scope** vs **Out of Scope** should be concrete; vague bullets (“improve resilience”) need sharpening.
4. **Are there hidden dependencies?** Order, data prerequisites, feature flags, migrations, or shared files touched by multiple stories; dependencies should appear in **Implementation Notes** or an explicit note—flag implicit ones.
5. **Are any stories too broad?** Single story spanning multiple unrelated user outcomes or entire subsystems; recommend splits with clear boundaries.
6. **Are any acceptance criteria vague?** Non-measurable adjectives (“fast,” “robust,” “good UX”); missing error/empty states; criteria that cannot map to a test or observable outcome.
7. **Are test requirements strong enough?** Checkboxes present but **substance** weak (e.g. “add tests” without type/level); alignment with `test-plan.md` and **03-acceptance-criteria.md**; missing integration/contract boundaries when the story exposes HTTP or persistence.
8. **Does the story set fully cover the Phase spec?** Trace **must-have** behaviors and contracts from phase `spec.md` / `api-contract.md` / `data-model.md` / `test-plan.md` to at least one story (**Spec References** + **In Scope**); list **gaps** (spec requirements with no story owner).
9. **Is anything from future phases accidentally included?** Compare to `02-roadmap.md` and later phase folders; flag **Out of Scope** omissions or **In Scope** items that belong to Phase 2+.
10. **Is the order correct?** Migrations before code that assumes schema; contracts before clients; foundational config before features; flag **cycles** or **test-the-last-story-first** anti-patterns.

## Alignment with authoring skills

- Story files are expected to follow **write-implementation-story** (canonical headings and checklists).
- **decompose-phase-stories** defines where stories live and independence/traceability expectations; this review **validates** that breakdown against specs—does not replace it.

## Required output

Use these **markdown headings** in order:

### Scope

Phase directory, list of `story-*.md` files, list of phase spec files read, and any skipped files (with reason).

### Critical feedback

Highest-severity issues: missing coverage, unsafe ordering, non-testable stories, or vague acceptance that would block implementation or review.

### Required changes

Concrete edits needed in **story markdown** and/or **phase specs** before treating the breakdown as approved (file-scoped bullets; each item actionable).

### Optional improvements

Splits, clarifications, diagram requests, or consistency fixes that are not strict blockers.

### Recommended story order

Numbered list of `story-*.md` filenames (or story titles with file names in parentheses). Brief **one-line rationale per position**; call out **parallelizable** pairs only if justified.

### Questions for human approval

Explicit decisions only a human should make (priorities, phase boundary, risk acceptance, controversial splits, deferrals).

## Tone

Short paragraphs and bullets. Every major finding should tie to **which story file** or **which spec file/section**. Do not propose Java/Spring implementation or code patches unless the user asks for implementation help outside this review.
