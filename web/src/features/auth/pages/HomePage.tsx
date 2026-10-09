import { useAuth } from "@/features/auth/hook/useAuth";

export default function HomePage() {
  const { user, loading, login, logout } = useAuth();

  if (loading) {
    return <main className="home-page">Loading AssistTube...</main>;
  }

  if (!user) {
    return (
      <main className="home-page">
        <h1>Your YouTube channel. One smarter workspace.</h1>
        <button type="button" onClick={login}>
          Continue with Google
        </button>
      </main>
    );
  }

  return (
    <main className="home-page">
      <h1>Welcome back, {user.name}</h1>
      <p>{user.email}</p>

      {user.picture && (
        <img src={user.picture} alt="Your Google profile" width={48} />
      )}

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
    </main>
  );
}
