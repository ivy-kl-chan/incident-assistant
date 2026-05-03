-- Baseline schema: Phase 1a incidents + reserved 1b columns and auxiliary 1b tables.
-- Normative: specs/phases/phase-1a-monolith-core/data-model.md,
--             specs/phases/phase-1b-signal-ingest/data-model.md

CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    severity VARCHAR(8) NOT NULL,
    source VARCHAR(16) NOT NULL DEFAULT 'MANUAL',
    created_by_rule_id VARCHAR(128),
    signal_fingerprint VARCHAR(64),
    telemetry_context JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_incidents_status_created_at ON incidents (status, created_at DESC);

CREATE INDEX idx_incidents_signal_fingerprint_created_at_partial
    ON incidents (signal_fingerprint, created_at DESC)
    WHERE signal_fingerprint IS NOT NULL;

CREATE TABLE signal_ingest_audit (
    id BIGSERIAL PRIMARY KEY,
    received_at TIMESTAMPTZ NOT NULL,
    rule_id VARCHAR(128) NOT NULL,
    matched BOOLEAN NOT NULL,
    incident_id UUID,
    dedup_hit BOOLEAN NOT NULL,
    payload_hash VARCHAR(64) NOT NULL
);

CREATE TABLE signal_ingest_idempotency (
    idempotency_key_hash CHAR(64) PRIMARY KEY,
    body_hash CHAR(64) NOT NULL,
    http_status SMALLINT NOT NULL,
    response_body JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
