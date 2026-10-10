import { api } from "@/lib/api";

class ChatApi {
    async createConversation(request: ChatRequest): Promise<ChatResponse> {
        const response = await api.post<ChatResponse>("/api/v1/chat", request);

        return response.data;
    }

    async sendMessage(
        conversationId: string,
        request: ChatRequest,
    ): Promise<ChatResponse> {
        const response = await api.post<ChatResponse>(
            `/api/v1/chat/${conversationId}`,
            request,
        );
        return response.data;
    }

    async listConversations(): Promise<ConversationSummary[]> {
        const response = await api.get<ConversationSummary[]>("/api/v1/chat");
        return response.data;
    }

    async getConversation(conversationId: string): Promise<ConversationDetail> {
        const response = await api.get<ConversationDetail>(
            `/api/v1/chat/${conversationId}`,
        );
        return response.data;
    }
}

const chatApi = new ChatApi();
export default chatApi;
