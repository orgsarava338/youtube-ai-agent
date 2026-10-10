import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";

import ChatLayout from "@/layouts/ChatLayout";
import { useYouTubeChannels } from "@/features/youtubeChannel/hooks/useYouTubeChannels";
import { useChat } from "@/features/chat/hooks/useChat";
import SmartResponse from "@/features/chat/components/SmartResponse";

import "./ChatPage.css";
import ChatSidebar from "@/features/chat/components/ChatSidebar";

export default function ChatPage() {
    const { customUrl, conversationId: routeConversationId } = useParams<{
        customUrl: string;
        conversationId?: string;
    }>();
    const navigate = useNavigate();

    const { channels, loading, error, refetch } = useYouTubeChannels();

    const {
        messages,
        conversationId,
        conversations,
        sending,
        loadingConversation,
        error: chatError,
        sendMessage,
        loadConversations,
        loadConversation,
        clearMessages,
    } = useChat();

    const [input, setInput] = useState("");
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const textareaRef = useRef<HTMLTextAreaElement>(null);
    const processedRouteRef = useRef<string | null>(null);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, sending]);

    useEffect(() => {
        void loadConversations();
    }, [loadConversations]);

    useEffect(() => {
        const routeKey = routeConversationId ?? "new-chat";

        if (processedRouteRef.current === routeKey) {
            return;
        }

        processedRouteRef.current = routeKey;

        if (routeConversationId) {
            void loadConversation(routeConversationId);
        } else {
            clearMessages();
        }
    }, [routeConversationId, loadConversation, clearMessages]);

    if (loading) {
        return (
            <main className="chat-page chat-page--status">
                <p role="status">Loading your chat...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="chat-page chat-page--status">
                <h1>Unable to load your chat</h1>
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
            <main className="chat-page chat-page--status">
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

        if (!message || sending || !customUrl) {
            return;
        }

        setInput("");

        const newConversationId = await sendMessage(message);

        if (!routeConversationId && newConversationId) {
            navigate(`/chat/${customUrl}/chat/${newConversationId}`, {
                replace: true,
            });
        }

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

    function handleSelectConversation(id: string) {
        if (sending || loadingConversation || !customUrl) {
            return;
        }

        setInput("");
        navigate(`/chat/${customUrl}/chat/${id}`);
    }

    function handleNewChat() {
        if (sending || loadingConversation || !customUrl) {
            return;
        }

        setInput("");
        navigate(`/chat/${customUrl}/chat`);
        textareaRef.current?.focus();
    }

    return (
        <ChatLayout
            sidebar={
                <ChatSidebar
                    channel={channel}
                    onSignOut={() => navigate("/", { replace: true })}
                    conversations={conversations}
                    activeConversationId={conversationId}
                    loadingConversations={loadingConversation} // indicates that a conversation is being opened, not that the conversation list is loading.
                    onNewChat={handleNewChat}
                    onSelectConversation={(id) => {
                        void handleSelectConversation(id);
                    }}
                />
            }
        >
            <div className="chat-page">
                <header className="chat-page__header">
                    <div>
                        <span className="chat-page__eyebrow">
                            YOUR YOUTUBE COPILOT
                        </span>
                        <h1>Chat</h1>
                    </div>

                    <button
                        type="button"
                        className="chat-page__channel-button"
                        onClick={() => navigate("/")}
                    >
                        <span className="chat-page__status-dot" />
                        {channel.title}
                        <span aria-hidden="true">⌄</span>
                    </button>
                </header>

                <section
                    className="chat-page__content"
                    aria-label="Agent conversation"
                >
                    {messages.length === 0 ? (
                        <div className="chat-page__welcome">
                            <div className="chat-page__welcome-icon">
                                <span aria-hidden="true">✳</span>
                            </div>

                            <p className="chat-page__eyebrow">
                                YOUR CREATOR chat
                            </p>

                            <h2>
                                What can we work on
                                <br />
                                <span>today?</span>
                            </h2>

                            <p className="chat-page__description">
                                Your YouTube channel, all in one place. Explore
                                ideas, understand your content, and plan your
                                next move.
                            </p>

                            <div className="chat-page__suggestions">
                                <p className="chat-page__suggestions-label">
                                    TRY ASKING
                                </p>

                                <div className="chat-page__suggestion-grid">
                                    <button
                                        type="button"
                                        className="chat-page__suggestion-card"
                                        onClick={() =>
                                            setInput(
                                                "What can you tell me about my YouTube channel?",
                                            )
                                        }
                                    >
                                        <span className="chat-page__suggestion-icon chat-page__suggestion-icon--purple">
                                            ◉
                                        </span>
                                        <span className="chat-page__suggestion-title">
                                            Analyze my channel
                                        </span>
                                        <span className="chat-page__suggestion-description">
                                            Explore your channel and
                                            performance.
                                        </span>
                                        <span className="chat-page__suggestion-arrow">
                                            ↗
                                        </span>
                                    </button>

                                    <button
                                        type="button"
                                        className="chat-page__suggestion-card"
                                        onClick={() =>
                                            setInput(
                                                "Help me review my YouTube videos.",
                                            )
                                        }
                                    >
                                        <span className="chat-page__suggestion-icon chat-page__suggestion-icon--orange">
                                            ▷
                                        </span>
                                        <span className="chat-page__suggestion-title">
                                            Review my videos
                                        </span>
                                        <span className="chat-page__suggestion-description">
                                            Find content patterns and ideas.
                                        </span>
                                        <span className="chat-page__suggestion-arrow">
                                            ↗
                                        </span>
                                    </button>

                                    <button
                                        type="button"
                                        className="chat-page__suggestion-card"
                                        onClick={() =>
                                            setInput(
                                                "Give me five video ideas for my YouTube channel.",
                                            )
                                        }
                                    >
                                        <span className="chat-page__suggestion-icon chat-page__suggestion-icon--blue">
                                            ✎
                                        </span>
                                        <span className="chat-page__suggestion-title">
                                            Brainstorm video ideas
                                        </span>
                                        <span className="chat-page__suggestion-description">
                                            Discover fresh topics for your
                                            audience.
                                        </span>
                                        <span className="chat-page__suggestion-arrow">
                                            ↗
                                        </span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="chat-page__conversation">
                            {messages.map((message) => (
                                <article
                                    key={message.id}
                                    className={`chat-page__message chat-page__message--${message.role}`}
                                >
                                    <div className="chat-page__message-label">
                                        {message.role === "user"
                                            ? "You"
                                            : "AssisTube"}
                                    </div>

                                    <div className="chat-page__message-bubble">
                                        {message.role === "assistant" ? (
                                            <SmartResponse
                                                content={message.content}
                                            />
                                        ) : (
                                            message.content
                                        )}
                                        {message.status === "error" && (
                                            <span className="chat-page__message-status">
                                                Message failed
                                            </span>
                                        )}
                                    </div>
                                </article>
                            ))}

                            {sending && (
                                <div
                                    className="chat-page__typing"
                                    role="status"
                                    aria-label="AssisTube is thinking"
                                >
                                    <span />
                                    <span />
                                    <span />
                                    <span className="chat-page__typing-label">
                                        AssisTube is thinking...
                                    </span>
                                </div>
                            )}

                            <div ref={messagesEndRef} />
                        </div>
                    )}

                    {chatError && (
                        <p className="chat-page__chat-error" role="alert">
                            {chatError}
                        </p>
                    )}

                    <div className="chat-page__composer">
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

                        <div className="chat-page__composer-footer">
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
                            className="chat-page__new-chat"
                            onClick={handleNewChat}
                            disabled={sending}
                        >
                            + New chat
                        </button>
                    )}

                    <p className="chat-page__disclaimer">
                        AssisTube can make mistakes. Verify important
                        information.
                    </p>
                </section>
            </div>
        </ChatLayout>
    );
}
