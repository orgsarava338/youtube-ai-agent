interface ChatRequest {
    message: string;
}

interface ChatResponse {
    conversationId: string;
    type: string;
    content: string | null;
}

interface ConversationSummary {
    conversationId: string;
    title: string;
    createdAt: string;
    updatedAt: string;
}

interface PersistedMessage {
    role: string;
    content: string;
    toolCallId: string | null;
}

interface ConversationDetail {
    conversationId: string;
    title: string;
    createdAt: string;
    updatedAt: string;
    messages: PersistedMessage[];
}

interface ChatMessage {
    id: string;
    role: "user" | "assistant";
    content: string;
    createdAt: string;
    status?: "sending" | "sent" | "error";
}
