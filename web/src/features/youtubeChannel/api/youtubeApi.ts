import { api } from "@/lib/api";

class YouTubeChannelsApi {
    async getMyChannels(): Promise<YouTubeChannel[]> {
        const response = await api.get<YouTubeChannel[]>(
            "/api/v1/youtube/channels",
        );
        return response.data;
    }

    async getMyChannel(): Promise<YouTubeChannel> {
        const response = await api.get<YouTubeChannel>(
            "/api/v1/youtube/channel",
        );
        return response.data;
    }
}

const youtubeChannelsApi = new YouTubeChannelsApi();
export default youtubeChannelsApi;
