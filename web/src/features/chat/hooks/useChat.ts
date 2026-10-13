import { useCallback, useEffect, useRef, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

import chatApi from "@/features/chat/api/chatApi";
import {
    conversationsQuery,
    conversationDetailQuery,
} from "@/features/chat/queries/chatQueries";
import { chatQueryKeys } from "@/features/chat/queries/chatQueryKeys";

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

export function useChat(routeConversationId?: string) {
    const queryClient = useQueryClient();

    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [conversationId, setConversationId] = useState<string | null>(null);
    const [sending, setSending] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const hydratedConversationRef = useRef<string | null>(null);

    // Fetch and cache the sidebar conversation list.
    const conversationsQueryResult = useQuery(conversationsQuery());

    // Fetch the selected conversation only when its URL contains an ID.
    const conversationDetailResult = useQuery({
        ...conversationDetailQuery(routeConversationId ?? ""),
        enabled: Boolean(routeConversationId),
    });

    const conversations = conversationsQueryResult.data ?? [];

    // Restore messages once when opening a conversation.
    // A background refetch must not overwrite messages being edited or sent.

    useEffect(() => {
        if (!routeConversationId) {
            hydratedConversationRef.current = null;
            setConversationId(null);
            setMessages([]);
            setError(null);
            return;
        }

        const detail = conversationDetailResult.data;

        if (
            !detail ||
            hydratedConversationRef.current === routeConversationId ||
            detail.conversationId !== routeConversationId
        ) {
            return;
        }

        const restoredMessages: ChatMessage[] = detail.messages
            .filter(
                (message) =>
                    message.role === "user" || message.role === "assistant",
            )
            .map((message) => ({
                id: crypto.randomUUID(),
                role: message.role as ChatMessage["role"],
                content: message.content,
                createdAt: new Date().toISOString(),
                status: "sent",
            }));

        hydratedConversationRef.current = routeConversationId;
        setConversationId(detail.conversationId);
        setMessages(restoredMessages);
        setError(null);
    }, [routeConversationId, conversationDetailResult.data]);

    // Surface errors from the conversation-detail query.
    useEffect(() => {
        if (conversationDetailResult.error) {
            setError(getErrorMessage(conversationDetailResult.error));
        }
    }, [conversationDetailResult.error]);

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
                const response = conversationId
                    ? await chatApi.sendMessage(conversationId, { message })
                    : await chatApi.createConversation({ message });

                const activeConversationId =
                    response.conversationId ?? conversationId;

                if (activeConversationId) {
                    setConversationId(activeConversationId);
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

                // Refresh sidebar data after sending a message.
                await queryClient.invalidateQueries({
                    queryKey: chatQueryKeys.conversations(),
                });

                return activeConversationId;
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
        [conversationId, sending, queryClient],
    );

    const clearMessages = useCallback(() => {
        if (sending) {
            return;
        }

        hydratedConversationRef.current = null;
        setConversationId(null);
        setMessages([]);
        setError(null);
    }, [sending]);

    const deleteConversation = useCallback(
        async (id: string): Promise<void> => {
            // Stop any in-flight request for this conversation.
            await queryClient.cancelQueries({
                queryKey: chatQueryKeys.detail(id),
                exact: true,
            });

            await chatApi.deleteConversation(id);

            // Update the sidebar cache immediately.
            queryClient.setQueryData<ConversationSummary[]>(
                chatQueryKeys.conversations(),
                (current) =>
                    current?.filter((item) => item.conversationId !== id),
            );

            if (conversationId === id) {
                setConversationId(null);
                setMessages([]);
                setError(null);
            }
        },
        [conversationId, queryClient],
    );

    return {
        messages,
        conversationId,
        conversations,
        sending,

        loadingConversation:
            Boolean(routeConversationId) && conversationDetailResult.isPending,

        loadingConversations: conversationsQueryResult.isPending,

        error:
            error ??
            (conversationsQueryResult.error
                ? getErrorMessage(conversationsQueryResult.error)
                : null),

        sendMessage,
        clearMessages,
        deleteConversation,
    };
}
