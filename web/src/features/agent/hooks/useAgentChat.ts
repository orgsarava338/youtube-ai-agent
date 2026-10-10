import { useCallback, useState } from "react";
import axios from "axios";

import agentApi from "@/features/agent/api/agentApi";

function createMessage(
    role: AgentMessage["role"],
    content: string,
    status: AgentMessage["status"] = "sent",
): AgentMessage {
    return {
        id: crypto.randomUUID(),
        role,
        content,
        createdAt: new Date().toISOString(),
        status,
    };
}

export function useAgentChat() {
    const [messages, setMessages] = useState<AgentMessage[]>([]);
    const [sending, setSending] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const sendMessage = useCallback(
        async (value: string) => {
            const message = value.trim();

            if (!message || sending) {
                return;
            }

            setError(null);
            setSending(true);

            const userMessage = createMessage("user", message, "sending");

            setMessages((current) => [...current, userMessage]);

            try {
                const response = await agentApi.sendMessage({ message });

                setMessages((current) =>
                    current.map((item) =>
                        item.id === userMessage.id
                            ? { ...item, status: "sent" }
                            : item,
                    ),
                );

                const content =
                    response.content?.trim() ||
                    (response.type === "agent_error"
                        ? "The agent encountered an error."
                        : "The agent returned an empty response.");

                setMessages((current) => [
                    ...current,
                    createMessage("assistant", content),
                ]);

                if (response.type === "agent_error") {
                    setError(content);
                }
            } catch (err: unknown) {
                const errorMessage = axios.isAxiosError(err)
                    ? typeof err.response?.data?.message === "string"
                        ? err.response.data.message
                        : typeof err.response?.data?.content === "string"
                          ? err.response.data.content
                          : err.response?.status === 401
                            ? "Your session may have expired. Please sign in again."
                            : "Unable to reach the agent. Please try again."
                    : "Something went wrong. Please try again.";

                setMessages((current) =>
                    current.map((item) =>
                        item.id === userMessage.id
                            ? { ...item, status: "error" }
                            : item,
                    ),
                );

                setError(errorMessage);
            } finally {
                setSending(false);
            }
        },
        [sending],
    );

    const clearMessages = useCallback(() => {
        if (sending) {
            return;
        }

        setMessages([]);
        setError(null);
    }, [sending]);

    return {
        messages,
        sending,
        error,
        sendMessage,
        clearMessages,
    };
}
