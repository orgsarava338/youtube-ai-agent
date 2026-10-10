import { useNavigate } from "react-router";
import "./WorkspaceSidebar.css";

interface WorkspaceSidebarProps {
    channel: WorkspaceChannel;
    onSignOut?: () => void;
}

export default function WorkspaceSidebar({
    channel,
    onSignOut,
}: WorkspaceSidebarProps) {
    const navigate = useNavigate();

    return (
        <div className="workspace-sidebar">
            <div className="workspace-sidebar__brand">
                <div className="workspace-sidebar__brand-icon">A</div>
                <span>AssisTube</span>
            </div>

            <button
                type="button"
                className="workspace-sidebar__new-chat"
                onClick={() => navigate(`/workspace/${channel.customUrl || channel.id}`)}
            >
                <span aria-hidden="true">+</span>
                <span>New chat</span>
            </button>

            <nav className="workspace-sidebar__navigation" aria-label="Workspace navigation">
                <p className="workspace-sidebar__section-label">WORKSPACE</p>

                <button
                    type="button"
                    className="workspace-sidebar__nav-item workspace-sidebar__nav-item--active"
                >
                    <span aria-hidden="true">◉</span>
                    <span>Chat</span>
                </button>

                <button
                    type="button"
                    className="workspace-sidebar__nav-item"
                    disabled
                    title="Coming soon"
                >
                    <span aria-hidden="true">▥</span>
                    <span>Analytics</span>
                    <span className="workspace-sidebar__coming-soon">Soon</span>
                </button>

                <button
                    type="button"
                    className="workspace-sidebar__nav-item"
                    disabled
                    title="Coming soon"
                >
                    <span aria-hidden="true">▷</span>
                    <span>Videos</span>
                    <span className="workspace-sidebar__coming-soon">Soon</span>
                </button>
            </nav>

            <div className="workspace-sidebar__bottom">
                <div className="workspace-sidebar__channel">
                    <p className="workspace-sidebar__section-label">CONNECTED CHANNEL</p>

                    <div className="workspace-sidebar__channel-info">
                        {channel.thumbnailUrl ? (
                            <img
                                className="workspace-sidebar__channel-avatar"
                                src={channel.thumbnailUrl}
                                alt=""
                                referrerPolicy="no-referrer"
                            />
                        ) : (
                            <div className="workspace-sidebar__channel-avatar workspace-sidebar__channel-avatar--placeholder">
                                {channel.title.charAt(0).toUpperCase()}
                            </div>
                        )}

                        <div className="workspace-sidebar__channel-details">
                            <p className="workspace-sidebar__channel-name">
                                {channel.title}
                            </p>
                            <span> YouTube channel </span>
                        </div>

                        <span
                            className="workspace-sidebar__connected-indicator"
                            title="Channel connected"
                        />
                    </div>
                </div>

                <div className="workspace-sidebar__footer">
                    <div className="workspace-sidebar__account-icon">S</div>

                    <div className="workspace-sidebar__account-details">
                        <p>My account</p>
                        <span>AssisTube workspace</span>
                    </div>

                    {onSignOut && (
                        <button
                            type="button"
                            className="workspace-sidebar__sign-out"
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
