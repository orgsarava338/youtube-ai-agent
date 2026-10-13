export const chatQueryKeys = {
    all: ["chat"] as const,

    conversations: () => [...chatQueryKeys.all, "conversations"] as const,

    detail: (conversationId: string) =>
        [...chatQueryKeys.all, "conversation", conversationId] as const,
};
