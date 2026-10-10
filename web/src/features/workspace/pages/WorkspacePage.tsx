import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";

import { useAgentChat } from "@/features/agent/hooks/useAgentChat";
import { useYouTubeChannels } from "@/features/youtubeChannel/hooks/useYouTubeChannels";
import WorkspaceLayout from "@/layouts/WorkspaceLayout";
import WorkspaceSidebar from "@/features/workspace/components/WorkspaceSidebar";

import "./WorkspacePage.css";
import SmartResponse from "@/features/agent/components/SmartResponse";

export default function WorkspacePage() {
    const { channelId } = useParams<{ channelId: string }>();
    const navigate = useNavigate();

    const { channels, loading, error, refetch } = useYouTubeChannels();

    const {
        messages,
        sending,
        error: chatError,
        sendMessage,
        clearMessages,
    } = useAgentChat();

    const [input, setInput] = useState("");
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, sending]);

    if (loading) {
        return (
            <main className="workspace-page workspace-page--status">
                <p role="status">Loading your workspace...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="workspace-page workspace-page--status">
                <h1>Unable to load your workspace</h1>
                <p role="alert">{error}</p>
                <button type="button" onClick={() => void refetch()}>
                    Try again
                </button>
            </main>
        );
    }

    const channel = channels.find(
        (item) => item.customUrl === channelId || item.id === channelId,
    );

    if (!channel) {
        return (
            <main className="workspace-page workspace-page--status">
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

    async function handleSendMessage() {
        const message = input.trim();

        if (!message || sending) {
            return;
        }

        setInput("");
        await sendMessage(message);
        textareaRef.current?.focus();
    }

    function handleComposerKeyDown(
        event: React.KeyboardEvent<HTMLTextAreaElement>,
    ) {
        if (
            event.key === "Enter" &&
            !event.shiftKey &&
            !event.nativeEvent.isComposing
        ) {
            event.preventDefault();
            void handleSendMessage();
        }
    }

    function handleNewChat() {
        if (sending) {
            return;
        }

        clearMessages();
        setInput("");
        textareaRef.current?.focus();
    }

    return (
        <WorkspaceLayout
            sidebar={
                <WorkspaceSidebar
                    channel={channel}
                    onSignOut={() => navigate("/", { replace: true })}
                />
            }
        >
            <div className="workspace-page">
                <header className="workspace-page__header">
                    <div>
                        <span className="workspace-page__eyebrow">
                            YOUR YOUTUBE COPILOT
                        </span>
                        <h1>Chat</h1>
                    </div>

                    <button
                        type="button"
                        className="workspace-page__channel-button"
                        onClick={() => navigate("/")}
                    >
                        <span className="workspace-page__status-dot" />
                        {channel.title}
                        <span aria-hidden="true">⌄</span>
                    </button>
                </header>

                <section
                    className="workspace-page__content"
                    aria-label="Agent conversation"
                >
                    {messages.length === 0 ? (
                        <div className="workspace-page__welcome">
                            <div className="workspace-page__welcome-icon">
                                <span aria-hidden="true">✳</span>
                            </div>

                            <p className="workspace-page__eyebrow">
                                YOUR CREATOR WORKSPACE
                            </p>

                            <h2>
                                What can we work on
                                <br />
                                <span>today?</span>
                            </h2>

                            <p className="workspace-page__description">
                                Your YouTube channel, all in one place. Explore
                                ideas, understand your content, and plan your
                                next move.
                            </p>

                            <div className="workspace-page__suggestions">
                                <p className="workspace-page__suggestions-label">
                                    TRY ASKING
                                </p>

                                <div className="workspace-page__suggestion-grid">
                                    <button
                                        type="button"
                                        className="workspace-page__suggestion-card"
                                        onClick={() =>
                                            setInput(
                                                "What can you tell me about my YouTube channel?",
                                            )
                                        }
                                    >
                                        <span className="workspace-page__suggestion-icon workspace-page__suggestion-icon--purple">
                                            ◉
                                        </span>
                                        <span className="workspace-page__suggestion-title">
                                            Analyze my channel
                                        </span>
                                        <span className="workspace-page__suggestion-description">
                                            Explore your channel and
                                            performance.
                                        </span>
                                        <span className="workspace-page__suggestion-arrow">
                                            ↗
                                        </span>
                                    </button>

                                    <button
                                        type="button"
                                        className="workspace-page__suggestion-card"
                                        onClick={() =>
                                            setInput(
                                                "Help me review my YouTube videos.",
                                            )
                                        }
                                    >
                                        <span className="workspace-page__suggestion-icon workspace-page__suggestion-icon--orange">
                                            ▷
                                        </span>
                                        <span className="workspace-page__suggestion-title">
                                            Review my videos
                                        </span>
                                        <span className="workspace-page__suggestion-description">
                                            Find content patterns and ideas.
                                        </span>
                                        <span className="workspace-page__suggestion-arrow">
                                            ↗
                                        </span>
                                    </button>

                                    <button
                                        type="button"
                                        className="workspace-page__suggestion-card"
                                        onClick={() =>
                                            setInput(
                                                "Give me five video ideas for my YouTube channel.",
                                            )
                                        }
                                    >
                                        <span className="workspace-page__suggestion-icon workspace-page__suggestion-icon--blue">
                                            ✎
                                        </span>
                                        <span className="workspace-page__suggestion-title">
                                            Brainstorm video ideas
                                        </span>
                                        <span className="workspace-page__suggestion-description">
                                            Discover fresh topics for your
                                            audience.
                                        </span>
                                        <span className="workspace-page__suggestion-arrow">
                                            ↗
                                        </span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="workspace-page__conversation">
                            {messages.map((message) => (
                                <article
                                    key={message.id}
                                    className={`workspace-page__message workspace-page__message--${message.role}`}
                                >
                                    <div className="workspace-page__message-label">
                                        {message.role === "user"
                                            ? "You"
                                            : "AssisTube"}
                                    </div>

                                    <div className="workspace-page__message-bubble">
                                        {message.role === "assistant" ? (
                                            <SmartResponse
                                                content={message.content}
                                            />
                                        ) : (
                                            message.content
                                        )}
                                        {message.status === "error" && (
                                            <span className="workspace-page__message-status">
                                                Message failed
                                            </span>
                                        )}
                                    </div>
                                </article>
                            ))}

                            {sending && (
                                <div
                                    className="workspace-page__typing"
                                    role="status"
                                    aria-label="AssisTube is thinking"
                                >
                                    <span />
                                    <span />
                                    <span />
                                    <span className="workspace-page__typing-label">
                                        AssisTube is thinking...
                                    </span>
                                </div>
                            )}

                            <div ref={messagesEndRef} />
                        </div>
                    )}

                    {chatError && (
                        <p className="workspace-page__chat-error" role="alert">
                            {chatError}
                        </p>
                    )}

                    <div className="workspace-page__composer">
                        <textarea
                            ref={textareaRef}
                            aria-label="Ask about your YouTube channel"
                            placeholder="Ask anything about your YouTube channel..."
                            rows={2}
                            value={input}
                            maxLength={20000}
                            disabled={sending}
                            onChange={(event) => setInput(event.target.value)}
                            onKeyDown={handleComposerKeyDown}
                        />

                        <div className="workspace-page__composer-footer">
                            <span>
                                Enter to send · Shift + Enter for a new line
                            </span>

                            <button
                                type="button"
                                disabled={!input.trim() || sending}
                                onClick={() => void handleSendMessage()}
                                aria-label="Send message"
                            >
                                {sending ? "…" : "↑"}
                            </button>
                        </div>
                    </div>

                    {messages.length > 0 && (
                        <button
                            type="button"
                            className="workspace-page__new-chat"
                            onClick={handleNewChat}
                            disabled={sending}
                        >
                            + New chat
                        </button>
                    )}

                    <p className="workspace-page__disclaimer">
                        AssisTube can make mistakes. Verify important
                        information.
                    </p>
                </section>
            </div>
        </WorkspaceLayout>
    );
}
