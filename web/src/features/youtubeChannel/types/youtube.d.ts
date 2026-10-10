interface YouTubeChannel {
    id: string;
    title: string;
    description?: string;
    customUrl?: string;
    thumbnailUrl?: string;
    subscriberCount?: number;
    videoCount?: number;
    viewCount?: number;
    uploadsPlaylistId?: string;
}
