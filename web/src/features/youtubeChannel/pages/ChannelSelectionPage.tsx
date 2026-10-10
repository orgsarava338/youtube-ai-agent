import { useEffect } from "react";
import { useNavigate } from "react-router";

import ChannelCard from "@/features/youtubeChannel/components/ChannelCard";
import { useYouTubeChannels } from "@/features/youtubeChannel/hooks/useYouTubeChannels";

import "./ChannelSelectionPage.css";

export default function ChannelSelectionPage() {
    const { channels, loading, error, refetch } = useYouTubeChannels();
    const navigate = useNavigate();

    useEffect(() => {
        if (loading || error || channels.length !== 1) {
            return;
        }

        const channel = channels[0];
        const channelIdentifier = channel.customUrl || channel.id;

        if (!channelIdentifier) {
            return;
        }

        navigate(`/workspace/${channelIdentifier}`, { replace: true });
    }, [channels, loading, error, navigate]);

    function handleSelectChannel(channel: YouTubeChannel) {
        const channelIdentifier = channel.customUrl || channel.id;

        if (!channelIdentifier) {
            console.error(
                "Cannot open workspace: channel identifier is missing.",
            );
            return;
        }

        navigate(`/workspace/${channelIdentifier}`);
    }

    if (loading) {
        return (
            <main className="channel-selection">
                <p role="status">Loading your YouTube channels...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="channel-selection">
                <h1>Unable to load your channels</h1>
                <p role="alert">{error}</p>
                <button type="button" onClick={() => void refetch()}>
                    Try again
                </button>
            </main>
        );
    }

    if (channels.length === 1) {
        return (
            <main className="channel-selection">
                <p role="status">Opening your YouTube workspace...</p>
            </main>
        );
    }

    return (
        <main className="channel-selection">
            <header className="channel-selection__header">
                <p className="channel-selection__eyebrow">
                    ASSISTUBE WORKSPACE
                </p>

                <h1>Select your YouTube channel</h1>

                <p>
                    Choose a connected channel to continue to your personalized
                    YouTube workspace.
                </p>

                <p className="channel-selection__count">
                    {channels.length}{" "}
                    {channels.length === 1 ? "channel" : "channels"} connected
                </p>
            </header>

            {channels.length === 0 ? (
                <section className="channel-selection__empty">
                    <h2>No YouTube channels found</h2>
                    <p>
                        Connect a YouTube account with a channel to get started.
                    </p>

                    <button type="button" onClick={() => void refetch()}>
                        Refresh channels
                    </button>
                </section>
            ) : (
                <section
                    className="channel-selection__grid"
                    aria-label="Your YouTube channels"
                >
                    {channels.map((channel) => (
                        <ChannelCard
                            key={channel.id}
                            channel={channel}
                            onSelect={handleSelectChannel}
                        />
                    ))}
                </section>
            )}
        </main>
    );
}
