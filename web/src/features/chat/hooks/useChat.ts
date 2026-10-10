import { useCallback, useState } from "react";
import axios from "axios";
import chatApi from "@/features/chat/api/chatApi";

function createMessage(
    role: ChatMessage["role"],
    content: string,
    status: ChatMessage["status"] = "sent",
): ChatMessage {
    return {
        id: crypto.randomUUID(),
        role,
        content,
        createdAt: new Date().toISOString(),
        status,
    };
}

function getErrorMessage(err: unknown): string {
    if (axios.isAxiosError(err)) {
        const data = err.response?.data;

        if (typeof data?.message === "string") {
            return data.message;
        }

        if (typeof data?.content === "string") {
            return data.content;
        }

        if (err.response?.status === 401) {
            return "Your session may have expired. Please sign in again.";
        }

        return "Unable to reach the chat service. Please try again.";
    }

    return "Something went wrong. Please try again.";
}

export function useChat() {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [conversationId, setConversationId] = useState<string | null>(null);
    const [conversations, setConversations] = useState<ConversationSummary[]>(
        [],
    );
    const [sending, setSending] = useState(false);
    const [loadingConversation, setLoadingConversation] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const sendMessage = useCallback(
        async (value: string): Promise<string | null> => {
            const message = value.trim();

            if (!message || sending) {
                return null;
            }

            setError(null);
            setSending(true);

            const userMessage = createMessage("user", message, "sending");

            setMessages((current) => [...current, userMessage]);

            try {
                // Start a new conversation or continue the active one.
                const response = conversationId
                    ? await chatApi.sendMessage(conversationId, {
                          message,
                      })
                    : await chatApi.createConversation({ message });

                if (response.conversationId) {
                    setConversationId(response.conversationId);
                }

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

                // Refresh sidebar data after a successful response.
                const updatedConversations = await chatApi.listConversations();

                setConversations(updatedConversations);

                return response.conversationId ?? conversationId;
            } catch (err: unknown) {
                const errorMessage = getErrorMessage(err);

                setMessages((current) =>
                    current.map((item) =>
                        item.id === userMessage.id
                            ? { ...item, status: "error" }
                            : item,
                    ),
                );

                setError(errorMessage);
                return null;
            } finally {
                setSending(false);
            }
        },
        [conversationId, sending],
    );

    const loadConversations = useCallback(async () => {
        try {
            const result = await chatApi.listConversations();
            setConversations(result);
        } catch (err: unknown) {
            setError(getErrorMessage(err));
        }
    }, []);

    const loadConversation = useCallback(
        async (id: string) => {
            if (sending || loadingConversation) {
                return;
            }

            setError(null);
            setLoadingConversation(true);

            try {
                const conversation = await chatApi.getConversation(id);

                const restoredMessages: ChatMessage[] = conversation.messages
                    .filter(
                        (message) =>
                            message.role === "user" ||
                            message.role === "assistant",
                    )
                    .map((message) => ({
                        id: crypto.randomUUID(),
                        role: message.role as ChatMessage["role"],
                        content: message.content,
                        createdAt: new Date().toISOString(),
                        status: "sent",
                    }));

                setConversationId(conversation.conversationId);
                setMessages(restoredMessages);
            } catch (err: unknown) {
                setError(getErrorMessage(err));
            } finally {
                setLoadingConversation(false);
            }
        },
        [sending, loadingConversation],
    );

    const clearMessages = useCallback(() => {
        if (sending || loadingConversation) {
            return;
        }

        // The next message will create a new conversation.
        setConversationId(null);
        setMessages([]);
        setError(null);
    }, [sending, loadingConversation]);

    return {
        messages,
        conversationId,
        conversations,
        sending,
        loadingConversation,
        error,
        sendMessage,
        loadConversations,
        loadConversation,
        clearMessages,
    };
}
