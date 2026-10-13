import { queryOptions } from "@tanstack/react-query";
import chatApi from "@/features/chat/api/chatApi";
import { chatQueryKeys } from "./chatQueryKeys";

export const conversationsQuery = () =>
    queryOptions({
        queryKey: chatQueryKeys.conversations(),
        queryFn: () => chatApi.listConversations(),
    });

export const conversationDetailQuery = (conversationId: string) =>
    queryOptions({
        queryKey: chatQueryKeys.detail(conversationId),
        queryFn: ({ signal }) =>
            chatApi.getConversation(conversationId, signal),
    });
