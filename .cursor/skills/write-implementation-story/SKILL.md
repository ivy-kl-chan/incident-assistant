---
name: write-implementation-story
description: >-
  Authors a single phase implementation story markdown file using the Incident
  Assistant canonical story template (title, status lifecycle, goal, spec
  references, prerequisites and dependencies, scope, API/data rules, acceptance
  and test checklists, files, review, completion). Use when the user asks to
  write, draft, or rewrite one story file; to align a story with phase specs; or when executing
  decompose-phase-stories (every `story-*.md` produced there must follow this
  template).
---

# Write implementation story

## When to use

- User wants **one** new or updated story under `specs/phases/<phase>/stories/`.
- User asks for the **full story template** (all sections below).
- **decompose-phase-stories** uses this template for **all** stories it creates under `stories/`; this skill also applies when drafting or rewriting **one** story in isolation.

## Before writing

1. Read the phase hub (`README.md` / `spec.md`) and the sections of `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md` that this story touches.
2. Read `specs/03-acceptance-criteria.md` for the relevant sub-phase.
3. Name the file `story-<n>-<short-kebab-name>.md` and match story number to sequence in `stories/`.

## Sixteen traceability areas

Stories use **sixteen** `##` sections in a fixed order, each titled with a **section number and name** (e.g. `## 1. Status`). **decompose-phase-stories**, **review-phase-story-breakdown**, and **review-story-pre-implementation** all expect this same set—**do not omit a section** or rename headings. If a section is empty, write `None.` or a single honest placeholder (e.g. “N/A—no HTTP surface in this story.”).

## Canonical section headings (all `story-*.md` files)

The story **title** is one line: `# Story <n>: [Story Name]`. After that, these headings must appear **in this order**, with **exact** titles (number, spelling, spacing, and the period after the digit):

1. `## 1. Status`
2. `## 2. Goal`
3. `## 3. User Value`
4. `## 4. Spec References`
5. `## 5. Prerequisites, dependencies, and blocked by`
6. `## 6. In Scope`
7. `## 7. Out of Scope`
8. `## 8. API Changes`
9. `## 9. Data Model Changes`
10. `## 10. Business Rules`
11. `## 11. Acceptance Criteria`
12. `## 12. Test Requirements`
13. `## 13. Files Expected to Change`
14. `## 14. Implementation Notes`
15. `## 15. Human Review Checklist`
16. `## 16. Completion Notes`

The **Required output template** block below is the expanded form of this list (same headings and order).

## Required output template (use verbatim structure)

Replace bracketed placeholders with real content. Keep **all headings** and the **Acceptance Criteria** / **Test Requirements** / **Human Review Checklist** structure as written. For **`## 1. Status`**, substitute **one** value from **Status lifecycle** (the template example uses **`Draft`** for new stories).

```markdown
# Story X: [Story Name]

## 1. Status

Draft

## 2. Goal

[One clear outcome.]

## 3. User Value

[Why this story matters.]

## 4. Spec References

- spec.md: [section name]
- api-contract.md: [section name]
- data-model.md: [section name]
- test-plan.md: [section name]

## 5. Prerequisites, dependencies, and blocked by

- [Prior `story-*.md` files, merged migrations, or feature flags this story assumes; or `None` if this is the first story or has no hard blockers.]
- [What blocks starting this story (human approval, env, or another team).]

## 6. In Scope

- [specific item]
- [specific item]
- [specific item]

## 7. Out of Scope

- [explicit exclusions]
- [future story items]
- [future phase items]

## 8. API Changes

[Endpoint, request, response, errors, or "None".]

## 9. Data Model Changes

[Entities, fields, constraints, or "None".]

## 10. Business Rules

- [rule]
- [rule]

## 11. Acceptance Criteria

- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

## 12. Test Requirements

- [ ] Unit tests
- [ ] API/controller tests
- [ ] Repository tests, if applicable
- [ ] Error case tests

## 13. Files Expected to Change

- [path]
- [path]

## 14. Implementation Notes

[Guidance for Cursor.]

## 15. Human Review Checklist

- [ ] Scope matches story
- [ ] No future story implemented
- [ ] Tests are meaningful
- [ ] Public API matches spec
- [ ] README/spec updates called out in the story ship **with** implementation when the story says so (not orphaned spec-only README edits for runtime behavior)

## 16. Completion Notes

[Filled after implementation.]
```

## Status lifecycle

Stories use a **single** status word on the line(s) immediately under **`## 1. Status`**. Allowed values and meaning:

| Status | When to use |
|--------|----------------|
| **Draft** | Story text is in progress or not yet approved for implementation. |
| **Planned** | Story content is substantially drafted but **held for another review round** before **Approved**; not yet the active implementation story. |
| **Approved** | Pre-implementation review passed (e.g. **review-story-pre-implementation**); safe to start coding as the **active** story. |
| **In Progress** | Implementation underway. |
| **Implemented** | Code changes merged or otherwise delivered for this story’s scope. |
| **Reviewed** | Human or peer review of the implementation completed. |
| **Complete** | Story scope and acceptance criteria verified; artifact closed. |

**Legacy:** Some repositories use **`Completed`** (past tense) like **`Complete`** for **`## 1. Status`**. Treat **`Complete`** and **`Completed`** as **closed** for [**.cursor/rules/incident-assistant-project.mdc**](../../rules/incident-assistant-project.mdc) **Closed story files**—agents must **not** edit those files for template churn unless the user explicitly targets that file.

**Pre-implementation gate:** **`review-story-pre-implementation`** expects **`Approved`** (or an explicit human waiver if the story remains **`Draft`** or **`Planned`**).

## Rules

- **Closed story files:** If the target `story-*.md` already has **`## 1. Status`** **`Complete`** or **`Completed`**, **stop**—do not normalize template sections, add **§5**, or renumber headings unless the user **explicitly** asked to edit **that** file. See **Closed story files** in `.cursor/rules/incident-assistant-project.mdc`.
- **Status:** set exactly **one** value from the table above in **`## 1. Status`**; never leave the template’s pipe-separated reminder as the literal file content unless the user explicitly wants a placeholder line.
- **Spec References:** use real relative links or `path: heading` as in existing phase stories; add rows for `03-acceptance-criteria.md` or ADRs when relevant.
- **Traceability:** every **In Scope** bullet should be justified by **Spec References**; acceptance criteria must be objectively verifiable.
- **Out of Scope:** always include explicit exclusions (avoids scope creep).
- **Prerequisites, dependencies, and blocked by (§5):** for every **new** or **open** (non-**Complete** / non-**Completed**) story, always include this section. List **prior** `story-*.md` deliverables, **migrations** (e.g. `V1` before domain code), **flags**, or **external** blockers. Use `None` only when there are no ordering or human dependencies. Do not hide hard sequencing in **Implementation Notes** alone when it is a true prerequisite. Do not retro-edit **closed** files to add **§5** without explicit human request (see **Closed story files** above).
- **README and user-facing docs:** If **Acceptance Criteria** or **Files Expected to Change** lists **repository `README.md`** (or similar), treat that as part of **implementation delivery**—the same change set / PR as the application behavior—unless the story explicitly allows a **spec-only** doc edit first. Do not instruct agents to fully author normative product README sections ahead of code when the story ties those docs to shipped endpoints or commands.
- After the file is written, give a short chat summary: dependencies, risks, and any spec gaps found.

## Relation to decompose-phase-stories

**decompose-phase-stories** owns phase inputs, story count, ordering, dependencies, and `stories/` naming; it **requires** this file’s template and **Canonical section headings** for every story body. Use **write-implementation-story** alone when the user only wants one story written or normalized to this template without a full phase pass.

## Relation to implement-story

**implement-story** drives coding against **one** `story-*.md` that follows this template and lifecycle (**Approved** → **In Progress** → **Implemented**). Authoring and status semantics here; execution and project invariants there.
