# AI behavior — Phase 1b

- **No** large language model, RAG, or MCP **tool execution** in 1b.
- **Abnormality** is **deterministic** rule code + fixtures (`rules/registry.yaml`), not ML.

**Forward (Phase 2+):** LLM may **add** fields or related records; must not overwrite `telemetry_context` without versioning. **No** automatic remediation at 1b→2 boundary.
