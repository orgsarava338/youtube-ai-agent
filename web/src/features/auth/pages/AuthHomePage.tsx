import { useAuth } from "@/features/auth/hook/useAuth";
import ChannelSelectionPage from "@/features/youtubeChannel/pages/ChannelSelectionPage";

import "./HomePage.scss";

export default function HomePage() {
    const { user, loading, login, logout } = useAuth();

    if (loading) {
        return (
            <main className="home-page home-page--loading">
                <div className="home-page__brand">
                    <span className="home-page__brand-mark">A</span>
                    <span>AssisTube</span>
                </div>
                <p role="status">Getting your workspace ready...</p>
            </main>
        );
    }

    if (!user) {
        return (
            <main className="home-page home-page--guest">
                <header className="home-page__topbar">
                    <a className="home-page__brand" href="/">
                        <span className="home-page__brand-mark">A</span>
                        <span>AssisTube</span>
                    </a>
                    <span className="home-page__topbar-label">
                        Your YouTube copilot
                    </span>
                </header>

                <section className="home-page__hero">
                    <div className="home-page__hero-badge">
                        <span aria-hidden="true">✦</span>A smarter way to create
                    </div>

                    <h1>
                        Your YouTube channel.
                        <br />
                        <span>One smarter workspace.</span>
                    </h1>

                    <p className="home-page__hero-description">
                        Understand your channel, explore your content, and make
                        your next move with an AI assistant built for creators.
                    </p>

                    <button
                        type="button"
                        className="home-page__google-button"
                        onClick={login}
                    >
                        <svg
                            aria-hidden="true"
                            viewBox="0 0 48 48"
                            width="21"
                            height="21"
                        >
                            <path
                                fill="#EA4335"
                                d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5Z"
                                transform="translate(2 2)"
                            />
                            <path
                                fill="#4285F4"
                                d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.73 7.18l7.64 5.93c4.46-4.12 7.13-10.18 7.13-17.58Z"
                            />
                            <path
                                fill="#FBBC05"
                                d="M10.53 28.59A14.4 14.4 0 0 1 9.75 24c0-1.59.27-3.13.76-4.59l-7.98-6.19A23.8 23.8 0 0 0 0 24c0 3.87.93 7.52 2.56 10.78l7.97-6.19Z"
                                transform="translate(2 2)"
                            />
                            <path
                                fill="#34A853"
                                d="M24 48c6.48 0 11.93-2.13 15.9-5.8l-7.64-5.93c-2.12 1.42-4.84 2.27-8.26 2.27-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48Z"
                                transform="translate(2 -2)"
                            />
                        </svg>
                        Continue with Google
                        <span aria-hidden="true">→</span>
                    </button>

                    <p className="home-page__privacy-note">
                        Sign in securely with Google to connect your YouTube
                        channel.
                    </p>

                    <div className="home-page__feature-list">
                        <div>
                            <span aria-hidden="true">✳</span>
                            <strong>Ask questions</strong>
                            <p>Get insights about your channel.</p>
                        </div>
                        <div>
                            <span aria-hidden="true">▤</span>
                            <strong>Explore your content</strong>
                            <p>Find ideas in your videos and analytics.</p>
                        </div>
                        <div>
                            <span aria-hidden="true">↗</span>
                            <strong>Plan what’s next</strong>
                            <p>Turn ideas into your next steps.</p>
                        </div>
                    </div>
                </section>

                <footer className="home-page__guest-footer">
                    Made for creators, powered by AI.
                </footer>
            </main>
        );
    }

    return (
        <div className="home-page home-page--authenticated">
            <header className="home-page__user-header">
                <a className="home-page__brand" href="/">
                    <span className="home-page__brand-mark">A</span>
                    <span>AssisTube</span>
                </a>

                <div className="home-page__user-actions">
                    <div className="home-page__user-info">
                        {user.picture ? (
                            <img
                                src={user.picture}
                                alt=""
                                width={40}
                                height={40}
                                referrerPolicy="no-referrer"
                            />
                        ) : (
                            <div className="home-page__avatar-placeholder">
                                {(user.name || user.email || "U")
                                    .charAt(0)
                                    .toUpperCase()}
                            </div>
                        )}

                        <div className="home-page__user-copy">
                            <p>{user.name || "Creator"}</p>
                            <span>{user.email}</span>
                        </div>
                    </div>

                    <button
                        type="button"
                        className="home-page__sign-out"
                        onClick={() => {
                            void logout().catch((error: unknown) => {
                                console.error("Logout failed", error);
                            });
                        }}
                    >
                        Sign out
                    </button>
                </div>
            </header>

            <ChannelSelectionPage />
        </div>
    );
}
