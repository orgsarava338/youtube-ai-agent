import config from "@/config";
import tokenStore from "@/features/auth/store/tokenStore";
import { api } from "@/lib/api";

class AuthApi {
    initializeAuthPromise: Promise<boolean> | null = null;

    startGoogleLogin(): void {
        if (!config?.api?.url) {
            throw new Error("API base URL is not configured");
        }

        const redirectUrl = window.location.origin;

        const loginUrl = new URL("/api/v1/auth/login", config.api.url);
        loginUrl.searchParams.set("redirectUrl", redirectUrl);

        window.location.assign(loginUrl.toString());
    }

    async initializeAuth(): Promise<boolean> {
        if (this.initializeAuthPromise) {
            return this.initializeAuthPromise;
        }

        this.initializeAuthPromise = this.performInitializeAuth();

        try {
            const res = await this.initializeAuthPromise;
            return res == true;
        } finally {
            this.initializeAuthPromise = null;
        }
    }

    async performInitializeAuth(): Promise<boolean> {
        try {
            // 1. Handle a new Google OAuth login if a code is present.
            const completedLogin = await this.completeLoginFromRedirect();

            if (completedLogin) {
                return true;
            }

            // 2. No OAuth code means this may be a returning user.
            // Restore the session using the persisted refresh token.
            const refreshToken = tokenStore.getRefreshToken();

            if (!refreshToken) {
                tokenStore.clearTokens();
                return false;
            }

            // 3. Request new tokens using the refresh token.
            // IMPORTANT: The URL must match your backend's @PostMapping.
            const response = await api.post<{
                accessToken: string;
                refreshToken: string;
                tokenType: "Bearer";
                expiresIn: number;
            }>("/api/v1/auth/token/refresh", {
                refreshToken,
            });

            const data = response.data;

            if (
                !data ||
                data.tokenType !== "Bearer" ||
                !data.accessToken ||
                !data.refreshToken
            ) {
                throw new Error("Invalid token refresh response");
            }

            // 4. Save both tokens because the backend rotates refresh tokens.
            tokenStore.setAccessToken(data.accessToken);
            tokenStore.setRefreshToken(data.refreshToken);

            return true;
        } catch (error) {
            console.error(error);

            // Don't clear tokens for every transient network/server error.
            // A temporary failure should not destroy a potentially valid session.
            return false;
        }
    }

    async completeLoginFromRedirect(): Promise<boolean> {
        const url = new URL(window.location.href);
        const code = url.searchParams.get("code");

        if (!code) {
            return false;
        }

        // Remove the one-time code before making any network request.
        url.searchParams.delete("code");

        window.history.replaceState(
            window.history.state,
            "",
            url.pathname + url.search + url.hash,
        );

        const response = await api.post<TokenExchangeResponse>(
            "/api/v1/auth/token/exchange",
            { code },
        );

        const data = response.data;
        if (
            !data ||
            data.tokenType !== "Bearer" ||
            !data.accessToken ||
            !data.refreshToken
        ) {
            throw new Error("Invalid token exchange response");
        }

        tokenStore.setAccessToken(data.accessToken);
        tokenStore.setRefreshToken(data.refreshToken);

        return true;
    }

    async getCurrentUser(): Promise<CurrentUser> {
        const accessToken = tokenStore.getAccessToken();

        if (!accessToken) {
            throw new Error("No access token available");
        }

        const response = await api.get<CurrentUser>("/api/v1/auth/me", {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        });
        return response.data;
    }

    async logout(): Promise<void> {
        const refreshToken = tokenStore.getRefreshToken();

        try {
            await api.post("/api/v1/auth/logout", { refreshToken });
        } finally {
            tokenStore.clearTokens();
        }
    }
}
const authApi = new AuthApi();
export default authApi;
