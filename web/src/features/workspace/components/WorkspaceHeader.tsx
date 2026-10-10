import "./WorkspaceHeader.css";

interface WorkspaceHeaderProps {
    channel: WorkspaceChannel;
    onBackToChannels: () => void;
}

export default function WorkspaceHeader({
    channel,
    onBackToChannels,
}: WorkspaceHeaderProps) {
    return (
        <header className="workspace-header">
            <button
                type="button"
                className="workspace-header__back"
                onClick={onBackToChannels}
            >
                <span aria-hidden="true">←</span>
                Channels
            </button>

            <div className="workspace-header__channel">
                {channel.thumbnailUrl ? (
                    <img
                        className="workspace-header__thumbnail"
                        src={channel.thumbnailUrl}
                        alt=""
                    />
                ) : (
                    <div
                        className="workspace-header__placeholder"
                        aria-hidden="true"
                    >
                        {channel.title.charAt(0).toUpperCase() || "Y"}
                    </div>
                )}

                <div className="workspace-header__details">
                    <span className="workspace-header__eyebrow">
                        ASSISTUBE WORKSPACE
                    </span>
                    <h1>{channel.title}</h1>
                </div>
            </div>
        </header>
    );
}
