CREATE SCHEMA IF NOT EXISTS chat;

CREATE TABLE chat.conversations (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_conversations_user_updated
    ON chat.conversations (user_id, updated_at DESC);

CREATE TABLE chat.conversation_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversation_id UUID NOT NULL
        REFERENCES chat.conversations(id) ON DELETE CASCADE,
    sequence_no BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    content TEXT,
    tool_calls_json TEXT,
    tool_call_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_conversation_message_sequence 
        UNIQUE (conversation_id, sequence_no),

    CONSTRAINT chk_conversation_message_role
        CHECK (role IN ('system', 'user', 'assistant', 'tool'))
);

CREATE INDEX idx_conversation_messages_order
    ON chat.conversation_messages (conversation_id, sequence_no);