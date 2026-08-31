ALTER TABLE call_sessions
    ADD COLUMN flow_public_id VARCHAR(100) NULL;

CREATE INDEX idx_call_session_flow
    ON call_sessions (flow_public_id);