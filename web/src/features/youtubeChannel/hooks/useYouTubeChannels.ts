import { useQuery } from "@tanstack/react-query";
import youtubeChannelsApi from "@/features/youtubeChannel/api/youtubeApi";

const YOUTUBE_CHANNELS_QUERY_KEY = ["youtube", "channels"] as const;

export function useYouTubeChannels() {
    const query = useQuery({
        queryKey: YOUTUBE_CHANNELS_QUERY_KEY,
        queryFn: () => youtubeChannelsApi.getMyChannels(),
    });

    return {
        channels: query.data ?? [],
        loading: query.isPending,
        error: query.error
            ? query.error instanceof Error
                ? query.error.message
                : "Unable to load your YouTube channels."
            : null,
        refetch: async () => {
            await query.refetch();
        },
    };
}
