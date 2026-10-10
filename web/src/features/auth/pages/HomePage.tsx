import { useAuth } from "@/features/auth/hook/useAuth";
import ChannelSelectionPage from "@/features/youtubeChannel/pages/ChannelSelectionPage";

export default function HomePage() {
    const { user, loading, login, logout } = useAuth();

    if (loading) {
        return (
            <main className="home-page">
                <p role="status">Loading AssisTube...</p>
            </main>
        );
    }

    if (!user) {
        return (
            <main className="home-page">
                <h1>Your YouTube channel. One smarter workspace.</h1>
                <p>
                    Connect your Google account to get started with AssisTube.
                </p>

                <button type="button" onClick={login}>
                    Continue with Google
                </button>
            </main>
        );
    }

    return (
        <>
            <header className="home-page__user-header">
                <div className="home-page__user-info">
                    {user.picture && (
                        <img
                            src={user.picture}
                            alt=""
                            width={40}
                            height={40}
                            referrerPolicy="no-referrer"
                        />
                    )}

                    <div>
                        <p>{user.name}</p>
                        <span>{user.email}</span>
                    </div>
                </div>

                <button
                    type="button"
                    onClick={() => {
                        void logout().catch((error: unknown) => {
                            console.error("Logout failed", error);
                        });
                    }}
                >
                    Sign out
                </button>
            </header>

            <ChannelSelectionPage />
        </>
    );
}
