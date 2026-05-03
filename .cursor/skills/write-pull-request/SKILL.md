---
name: write-pull-request
description: >-
  Drafts a concise GitHub pull request title and body for Incident Assistant
  work using the repo’s standard PR structure (summary, why, out of scope,
  implementation pointers table, verification steps, story/spec traceability,
  reviewer checklist). Delivers the PR description as Markdown inside one
  fenced markdown code block for reliable copy-paste, plus a labeled PR title line.
  Use when the user asks for a PR
  description, PR body, merge request text, or to write or polish a pull request
  for a story or branch.
---

# Write pull request

## When to use

- User asks for a **PR description**, **PR body**, **MR text**, or to **write/polish a PR**.

## Deliverable format (mandatory)

The user must get **copy-paste-ready Markdown**: valid **GFM source** they can paste into GitHub’s description field **or** save as a `.md` file.

### Layout (use every time)

1. **`**PR title:**`** — One line of plain text (GitHub **title** field).
2. Blank line.
3. **`**PR description (Markdown):**`** — Immediately below, put the **entire** PR body inside **one** fenced code block with language tag **`markdown`**:

   - The fence must be **only** around the description (not around the title).
   - **Inside** the fence: **GitHub Flavored Markdown** only—headings, lists, tables, task lists (`- [ ]`). **Start at `### Summary`**; run through **`### Reviewer checklist`**. **Do not** add meta headings like `## Description` inside the fence.
   - **After** the closing fence, add two short lines (plain text, outside any fence): tell the user to copy **only** what is **between** the opening and closing fence lines into GitHub’s PR description (omit the triple-backtick lines themselves); optionally save that text as `pr-description.md`.

### Why a `markdown` code block

Unfenced Markdown in chat often **renders** in the UI; copying from the preview can drop tables or task-list syntax. Fenced \`\`\`markdown … \`\`\` preserves **literal** `.md` source so “copy code” / select-all inside the fence yields paste-ready GFM.

### If the user insists on unfenced output only

Repeat the same sections **once** as unfenced GFM below the fence (rare); default remains **fenced** for copy-paste reliability.

## Before writing

1. Identify the **active story** (`specs/phases/<phase>/stories/story-*.md`) if the work is story-scoped; read its goal, scope, out-of-scope, acceptance criteria, and **Files Expected to Change**.
2. If available, skim **`git diff`** / branch changes so **Implementation pointers** and **verify** steps match real paths and tests.
3. Keep prose **concise** and **factual**; separate **facts** vs **assumptions** when anything is uncertain (mark assumptions briefly).

## PR body template (mandatory sections — paste-ready GFM)

Fill this structure for the **description** field. Replace bracketed placeholders with real content. **Reference only** (fence below). The **user-facing answer** must place this exact structure **inside** the deliverable’s outer \`\`\`markdown … \`\`\` block (see **Deliverable format**).

```markdown
### Summary

[2–4 sentences: what shipped and the main user/engineering outcome.]

### Why this shape

- [Bullet: key design or scope decision #1]
- [Bullet: key design or scope decision #2 — optional third bullet]

### Out of scope (explicit)

- [What reviewers should not expect or what was intentionally deferred.]

### Implementation pointers

| Area | Location |
|------|----------|
| [e.g. Migration / API / config] | `path` |
| [Tests] | `path` |

### How to verify

- Local: [command(s) or steps — e.g. `mvn clean verify`]
- CI: [what to check in workflow logs if relevant]

### Spec / story traceability

- **Story:** `path/to/story-*.md` (optional: note status from story)

### Reviewer checklist

- [ ] [Concrete check tied to specs or AC — e.g. DDL vs data-model]
- [ ] [Concrete check — e.g. single migration version, no scope creep]
- [ ] [Tests / separation of concerns if applicable]
```

## Style rules (Incident Assistant)

- **Title**: Prefer **`Story <n> — <short outcome>`** when the PR maps to one numbered story; otherwise a clear outcome-focused title.
- **No filler**: Avoid generic phrases (“this PR improves…”) without specifics.
- **Traceability**: Always include the **`story-*.md`** path when the change implements or advances a phase story.
- **Reviewer checklist**: Short, **actionable** bullets—what to open and compare—not vague “code looks good.”
- **Human tone**: Complete sentences; match project preference for precise, blog-quality prose without engagement filler.

## Optional: tech-lead polish pass

If the user asks to **review as tech lead** or **polish**:

1. Tighten the **Summary** to the contract (what changed and why it matters).
2. Ensure **Out of scope** prevents false expectations.
3. Add or sharpen **Implementation pointers** so reviewers know exact files/classes.
4. Expand **How to verify** with commands that actually exercise the change.
5. Make **Reviewer checklist** match acceptance criteria and specs, not generic coding standards.
