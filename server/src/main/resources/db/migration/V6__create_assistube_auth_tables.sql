CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.assistube_access_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_access_token_expiry
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_assistube_access_tokens_user_id
    ON auth.assistube_access_tokens (user_id);

CREATE INDEX idx_assistube_access_tokens_expires_at
    ON auth.assistube_access_tokens (expires_at);


CREATE TABLE auth.assistube_login_codes (
    code_hash VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_login_codes_expiry
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_assistube_login_codes_expires_at
    ON auth.assistube_login_codes (expires_at);


CREATE TABLE auth.assistube_refresh_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assistube_refresh_token_expiry
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_assistube_refresh_tokens_user_id
    ON auth.assistube_refresh_tokens (user_id);

CREATE INDEX idx_assistube_refresh_tokens_expires_at
    ON auth.assistube_refresh_tokens (expires_at);