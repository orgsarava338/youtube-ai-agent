import { api } from "@/lib/api";

class AgentApi {
    async sendMessage(request: AgentChatRequest): Promise<AgentChatResponse> {
        const response = await api.post<AgentChatResponse>(
            "/api/v1/agent/chat",
            request,
        );

        return response.data;
    }
}

const agentApi = new AgentApi();
export default agentApi;
