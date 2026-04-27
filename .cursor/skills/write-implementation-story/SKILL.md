---
name: write-implementation-story
description: >-
  Authors a single phase implementation story markdown file using the Incident
  Assistant canonical story template (title, status lifecycle, goal, spec
  references, scope, API/data rules, acceptance and test checklists, files,
  review, completion). Use when the user asks to write, draft, or rewrite one
  story file; to align a story with phase specs; or when executing
  phase-story-breakdown (every `story-*.md` produced there must follow this
  template).
---

# Write implementation story

## When to use

- User wants **one** new or updated story under `specs/phases/<phase>/stories/`.
- User asks for the **full story template** (all sections below).
- **phase-story-breakdown** uses this template for **all** stories it creates under `stories/`; this skill also applies when drafting or rewriting **one** story in isolation.

## Before writing

1. Read the phase hub (`README.md` / `spec.md`) and the sections of `spec.md`, `api-contract.md`, `data-model.md`, `test-plan.md` that this story touches.
2. Read `specs/03-acceptance-criteria.md` for the relevant sub-phase.
3. Name the file `story-<n>-<short-kebab-name>.md` and match story number to sequence in `stories/`.

## Fifteen traceability areas

The template below includes **all fifteen** planning areas used in this repo (see **phase-story-breakdown** “Fifteen traceability areas” checklist). Headings use `##` and the title uses `# Story X:` as shown—**do not omit a section**. If a section is empty, write `None.` or a single honest placeholder (e.g. “N/A—no HTTP surface in this story.”).

## Required output template (use verbatim structure)

Replace bracketed placeholders with real content. Keep **all headings** and the **Status** / **Acceptance Criteria** / **Test Requirements** / **Human Review Checklist** option lines as written.

```markdown
# Story X: [Story Name]

## Status

Draft | Approved | In Progress | Implemented | Reviewed | Complete

## Goal

[One clear outcome.]

## User Value

[Why this story matters.]

## Spec References

- spec.md: [section name]
- api-contract.md: [section name]
- data-model.md: [section name]
- test-plan.md: [section name]

## In Scope

- [specific item]
- [specific item]
- [specific item]

## Out of Scope

- [explicit exclusions]
- [future story items]
- [future phase items]

## API Changes

[Endpoint, request, response, errors, or "None".]

## Data Model Changes

[Entities, fields, constraints, or "None".]

## Business Rules

- [rule]
- [rule]

## Acceptance Criteria

- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

## Test Requirements

- [ ] Unit tests
- [ ] API/controller tests
- [ ] Repository tests, if applicable
- [ ] Error case tests

## Files Expected to Change

- [path]
- [path]

## Implementation Notes

[Guidance for Cursor.]

## Human Review Checklist

- [ ] Scope matches story
- [ ] No future story implemented
- [ ] Tests are meaningful
- [ ] Public API matches spec
- [ ] README/spec updated if needed

## Completion Notes

[Filled after implementation.]
```

## Rules

- **Status:** set a single state (e.g. `Draft` for new files); do not leave the pipe-separated list as the literal value unless the user asked for a reminder line—normally pick one status.
- **Spec References:** use real relative links or `path: heading` as in existing phase stories; add rows for `03-acceptance-criteria.md` or ADRs when relevant.
- **Traceability:** every **In Scope** bullet should be justified by **Spec References**; acceptance criteria must be objectively verifiable.
- **Out of Scope:** always include explicit exclusions (avoids scope creep).
- After the file is written, give a short chat summary: dependencies, risks, and any spec gaps found.

## Relation to phase-story-breakdown

**phase-story-breakdown** owns phase inputs, story count, ordering, dependencies, and `stories/` naming; it **requires** this file’s template for every story body. Use **write-implementation-story** alone when the user only wants one story written or normalized to this template without a full phase pass.
