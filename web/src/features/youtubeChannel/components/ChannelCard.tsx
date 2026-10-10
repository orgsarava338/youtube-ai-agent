import "./ChannelCard.css";

interface ChannelCardProps {
    channel: YouTubeChannel;
    onSelect: (channel: YouTubeChannel) => void;
}

export default function ChannelCard({ channel, onSelect }: ChannelCardProps) {
    return (
        <article className="channel-card">
            <div className="channel-card__thumbnail">
                {channel.thumbnailUrl ? (
                    <img
                        src={channel.thumbnailUrl}
                        alt={`${channel.title} channel`}
                    />
                ) : (
                    <div
                        className="channel-card__placeholder"
                        aria-hidden="true"
                    >
                        {channel.title.charAt(0).toUpperCase() || "Y"}
                    </div>
                )}
            </div>

            <div className="channel-card__content">
                <h2>{channel.title}</h2>

                {channel.customUrl && (
                    <p className="channel-card__handle">{channel.customUrl}</p>
                )}

                {channel.description && (
                    <p className="channel-card__description">
                        {channel.description}
                    </p>
                )}

                <button
                    type="button"
                    className="channel-card__select"
                    onClick={() => onSelect(channel)}
                >
                    Open in AssisTube
                </button>
            </div>
        </article>
    );
}
