import { useCallback, useEffect, useState } from "react";
import youtubeChannelsApi from "@/features/youtubeChannel/api/youtubeApi";

interface UseYouTubeChannelsResult {
    channels: YouTubeChannel[];
    loading: boolean;
    error: string | null;
    refetch: () => Promise<void>;
}

export function useYouTubeChannels(): UseYouTubeChannelsResult {
    const [channels, setChannels] = useState<YouTubeChannel[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchChannels = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const result = await youtubeChannelsApi.getMyChannels();
            setChannels(result);
        } catch (err: unknown) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to load your YouTube channels.",
            );
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        void fetchChannels();
    }, [fetchChannels]);

    return {
        channels,
        loading,
        error,
        refetch: fetchChannels,
    };
}
