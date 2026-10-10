import "./ChatSidebar.css";

interface ChatSidebarProps {
    channel: WorkspaceChannel;
    onSignOut?: () => void;
    conversations: ConversationSummary[];
    activeConversationId: string | null;
    loadingConversations: boolean;
    onNewChat: () => void;
    onSelectConversation: (id: string) => void;
}

export default function ChatSidebar({
    channel,
    onSignOut,
    conversations,
    activeConversationId,
    loadingConversations,
    onNewChat,
    onSelectConversation,
}: ChatSidebarProps) {
    return (
        <div className="chat-sidebar">
            <div className="chat-sidebar__brand">
                <div className="chat-sidebar__brand-icon">A</div>
                <span>AssisTube</span>
            </div>

            <button
                type="button"
                className="chat-sidebar__new-chat"
                onClick={onNewChat}
            >
                <span aria-hidden="true">+</span>
                <span>New chat</span>
            </button>

            <nav
                className="chat-sidebar__navigation"
                aria-label="chat navigation"
            >
                <p className="chat-sidebar__section-label">CHAT</p>

                <button
                    type="button"
                    className="chat-sidebar__nav-item chat-sidebar__nav-item--active"
                >
                    <span aria-hidden="true">◉</span>
                    <span>Chat</span>
                </button>

                <button
                    type="button"
                    className="chat-sidebar__nav-item"
                    disabled
                    title="Coming soon"
                >
                    <span aria-hidden="true">▥</span>
                    <span>Analytics</span>
                    <span className="chat-sidebar__coming-soon">Soon</span>
                </button>

                <button
                    type="button"
                    className="chat-sidebar__nav-item"
                    disabled
                    title="Coming soon"
                >
                    <span aria-hidden="true">▷</span>
                    <span>Videos</span>
                    <span className="chat-sidebar__coming-soon">Soon</span>
                </button>
            </nav>

            <section
                className="chat-sidebar__history"
                aria-label="Chat history"
            >
                <p className="chat-sidebar__section-label">RECENT CHATS</p>

                {loadingConversations ? (
                    <p className="chat-sidebar__history-status">
                        Loading conversations...
                    </p>
                ) : conversations.length === 0 ? (
                    <p className="chat-sidebar__history-status">
                        Your conversations will appear here.
                    </p>
                ) : (
                    <div className="chat-sidebar__history-list">
                        {conversations.map((conversation) => (
                            <button
                                key={conversation.conversationId}
                                type="button"
                                className={[
                                    "chat-sidebar__history-item",
                                    activeConversationId ===
                                    conversation.conversationId
                                        ? "chat-sidebar__history-item--active"
                                        : "",
                                ]
                                    .filter(Boolean)
                                    .join(" ")}
                                onClick={() =>
                                    onSelectConversation(
                                        conversation.conversationId,
                                    )
                                }
                                title={conversation.title}
                                aria-current={
                                    activeConversationId ===
                                    conversation.conversationId
                                        ? "page"
                                        : undefined
                                }
                            >
                                <span aria-hidden="true">◷</span>
                                <span className="chat-sidebar__history-title">
                                    {conversation.title || "New conversation"}
                                </span>
                            </button>
                        ))}
                    </div>
                )}
            </section>

            <div className="chat-sidebar__bottom">
                <div className="chat-sidebar__channel">
                    <p className="chat-sidebar__section-label">
                        CONNECTED CHANNEL
                    </p>

                    <div className="chat-sidebar__channel-info">
                        {channel.thumbnailUrl ? (
                            <img
                                className="chat-sidebar__channel-avatar"
                                src={channel.thumbnailUrl}
                                alt=""
                                referrerPolicy="no-referrer"
                            />
                        ) : (
                            <div className="chat-sidebar__channel-avatar chat-sidebar__channel-avatar--placeholder">
                                {channel.title.charAt(0).toUpperCase()}
                            </div>
                        )}

                        <div className="chat-sidebar__channel-details">
                            <p className="chat-sidebar__channel-name">
                                {channel.title}
                            </p>
                            <span> YouTube channel </span>
                        </div>

                        <span
                            className="chat-sidebar__connected-indicator"
                            title="Channel connected"
                        />
                    </div>
                </div>

                <div className="chat-sidebar__footer">
                    <div className="chat-sidebar__account-icon">S</div>

                    <div className="chat-sidebar__account-details">
                        <p>My account</p>
                        <span>AssisTube chat</span>
                    </div>

                    {onSignOut && (
                        <button
                            type="button"
                            className="chat-sidebar__sign-out"
                            onClick={onSignOut}
                            aria-label="Sign out"
                            title="Sign out"
                        >
                            ↗
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}
