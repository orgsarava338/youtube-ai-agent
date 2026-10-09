
import config from "@/config";
import "./GoogleLoginButton.css";

export default function GoogleLoginButton() {
  const handleLogin = () => {
    window.location.assign(
      `${config.api.url}/oauth2/authorization/google`
    );
  };

  return (
    <button
      type="button"
      className="google-login-button"
      onClick={handleLogin}
    >
      <span className="google-g" aria-hidden="true">
        G
      </span>
      <span>Continue with Google</span>
    </button>
  );
}
