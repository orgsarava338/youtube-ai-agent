import { useNavigate } from "react-router";

import ChannelCard from "@/features/youtubeChannel/components/ChannelCard";
import { useYouTubeChannels } from "@/features/youtubeChannel/hooks/useYouTubeChannels";

import "./ChannelSelectionPage.css";

export default function ChannelSelectionPage() {
    const { channels, loading, error, refetch } = useYouTubeChannels();
    const navigate = useNavigate();

    function handleSelectChannel(channel: YouTubeChannel) {
        const identifier = channel.customUrl || channel.id;

        if (!identifier) {
            return;
        }

        navigate(`/workspace/${identifier}`);
    }

    if (loading) {
        return (
            <main className="channel-selection channel-selection--center">
                <div
                    className="channel-selection__loading-icon"
                    aria-hidden="true"
                >
                    <span />
                </div>
                <h1>Finding your channels</h1>
                <p role="status">Loading your connected YouTube channels...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="channel-selection">
                <section className="channel-selection__empty">
                    <div
                        className="channel-selection__empty-icon"
                        aria-hidden="true"
                    >
                        !
                    </div>
                    <h1>We couldn't load your channels</h1>
                    <p role="alert">{error}</p>
                    <button
                        type="button"
                        className="channel-selection__primary-button"
                        onClick={() => void refetch()}
                    >
                        Try again
                    </button>
                </section>
            </main>
        );
    }

    if (channels.length === 0) {
        return (
            <main className="channel-selection">
                <header className="channel-selection__header">
                    <p className="channel-selection__eyebrow">
                        YOUR CREATOR WORKSPACE
                    </p>
                    <h1>Let's get your channel ready.</h1>
                    <p>
                        You’re signed in! Create a YouTube channel to start
                        exploring your content with AssisTube.
                    </p>
                </header>

                <section className="channel-selection__empty">
                    <div
                        className="channel-selection__empty-icon"
                        aria-hidden="true"
                    >
                        ▶
                    </div>
                    <h2>No YouTube channels yet</h2>
                    <p>
                        Create a channel using your Google account. Once it's
                        ready, come back here and refresh to get started.
                    </p>

                    <a
                        className="channel-selection__primary-button"
                        href="https://www.youtube.com/create_channel"
                        target="_blank"
                        rel="noreferrer"
                    >
                        Create a YouTube channel
                        <span aria-hidden="true"> ↗</span>
                    </a>

                    <button
                        type="button"
                        className="channel-selection__secondary-button"
                        onClick={() => void refetch()}
                    >
                        I've created my channel · Refresh
                    </button>
                </section>
            </main>
        );
    }

    return (
        <main className="channel-selection">
            <header className="channel-selection__header">
                <p className="channel-selection__eyebrow">
                    YOUR CREATOR WORKSPACE
                </p>
                <h1>Which channel are we working on?</h1>
                <p>
                    Choose a channel to open its dedicated AssisTube workspace.
                    Your conversations stay organized by channel.
                </p>

                <div className="channel-selection__meta">
                    <span className="channel-selection__status-dot" />
                    {channels.length}{" "}
                    {channels.length === 1 ? "channel" : "channels"} available
                </div>
            </header>

            <section
                className="channel-selection__grid"
                aria-label="Choose a YouTube channel"
            >
                {channels.map((channel) => (
                    <ChannelCard
                        key={channel.id}
                        channel={channel}
                        onSelect={handleSelectChannel}
                    />
                ))}
            </section>

            <p className="channel-selection__footer-note">
                <span aria-hidden="true">🔒</span> Your channels are private to
                your connected account.
            </p>
        </main>
    );
}
