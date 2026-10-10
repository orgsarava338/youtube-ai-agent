import { useNavigate, useParams } from "react-router";
import { useYouTubeChannels } from "@/features/youtubeChannel/hooks/useYouTubeChannels";
import "./WorkspacePage.css";

export default function WorkspacePage() {
    const { customUrl } = useParams<{ customUrl: string }>();
    const navigate = useNavigate();

    const { channels, loading, error, refetch } = useYouTubeChannels();

    if (loading) {
        return (
            <main className="channel-home channel-home--status">
                <p role="status">Loading your channels...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="channel-home channel-home--status">
                <h1>Unable to load your channels</h1>
                <p role="alert">{error}</p>
                <button type="button" onClick={() => void refetch()}>
                    Try again
                </button>
            </main>
        );
    }

    const channel = channels.find(
        (item) => item.customUrl === customUrl || item.id === customUrl,
    );

    if (!channel) {
        return (
            <main className="channel-home channel-home--status">
                <h1>Channel not found</h1>
                <p>
                    This channel isn't available in your connected YouTube
                    channels.
                </p>
                <button
                    type="button"
                    onClick={() => navigate("/", { replace: true })}
                >
                    Back to channels
                </button>
            </main>
        );
    }

    const chatUrl = `/workspace/${channel.customUrl}/chat`;

    return (
        <main className="channel-home">
            <header className="channel-home__header">
                <div>
                    <p className="channel-home__eyebrow">
                        YOUR CREATOR WORKSPACE
                    </p>
                    <h1>Channel Home</h1>
                    <p>Your YouTube channel, with AssisTube by your side.</p>
                </div>

                <button
                    type="button"
                    className="channel-home__chat-button"
                    onClick={() => navigate(chatUrl)}
                >
                    <span aria-hidden="true">✳</span>
                    Open AI Chat
                    <span aria-hidden="true">→</span>
                </button>
            </header>

            <section className="channel-home__hero">
                <div className="channel-home__hero-content">
                    <span className="channel-home__hero-label">
                        YOUR CHANNEL WORKSPACE
                    </span>
                    <h2>{channel.title}</h2>
                    <p>
                        Understand your channel, discover content opportunities,
                        and plan what to create next.
                    </p>

                    <button type="button" onClick={() => navigate(chatUrl)}>
                        Ask AssisTube
                        <span aria-hidden="true"> ↗</span>
                    </button>
                </div>

                <div
                    className="channel-home__hero-decoration"
                    aria-hidden="true"
                >
                    ✳
                </div>
            </section>

            <section className="channel-home__section">
                <div className="channel-home__section-heading">
                    <div>
                        <h2>What would you like to do?</h2>
                        <p>Start with a task and let your AI copilot help.</p>
                    </div>
                </div>

                <div className="channel-home__cards">
                    <button
                        type="button"
                        className="channel-home__card"
                        onClick={() =>
                            navigate(chatUrl, {
                                state: {
                                    prompt: "Analyze my YouTube channel and suggest improvements.",
                                },
                            })
                        }
                    >
                        <span className="channel-home__card-icon channel-home__card-icon--purple">
                            ◉
                        </span>
                        <h3>Analyze my channel</h3>
                        <p>
                            Explore your channel and identify opportunities to
                            improve.
                        </p>
                        <span className="channel-home__card-arrow">↗</span>
                    </button>

                    <button
                        type="button"
                        className="channel-home__card"
                        onClick={() =>
                            navigate(chatUrl, {
                                state: {
                                    prompt: "Review my YouTube videos and identify content patterns.",
                                },
                            })
                        }
                    >
                        <span className="channel-home__card-icon channel-home__card-icon--orange">
                            ▷
                        </span>
                        <h3>Review my videos</h3>
                        <p>
                            Discover content patterns and ideas for future
                            videos.
                        </p>
                        <span className="channel-home__card-arrow">↗</span>
                    </button>

                    <button
                        type="button"
                        className="channel-home__card"
                        onClick={() =>
                            navigate(chatUrl, {
                                state: {
                                    prompt: "Give me five video ideas for my YouTube channel.",
                                },
                            })
                        }
                    >
                        <span className="channel-home__card-icon channel-home__card-icon--blue">
                            ✎
                        </span>
                        <h3>Brainstorm video ideas</h3>
                        <p>Find fresh topics and ideas for your audience.</p>
                        <span className="channel-home__card-arrow">↗</span>
                    </button>
                </div>
            </section>
        </main>
    );
}
