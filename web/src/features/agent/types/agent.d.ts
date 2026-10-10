interface AgentChatRequest {
    message: string;
}

interface AgentChatResponse {
    type: string;
    content: string | null;
}

interface AgentMessage {
    id: string;
    role: "user" | "assistant";
    content: string;
    createdAt: string;
    status?: "sending" | "sent" | "error";
}
