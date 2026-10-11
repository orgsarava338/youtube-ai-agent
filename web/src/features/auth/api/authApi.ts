import config from "@/config";
import { api } from "@/lib/api";

class AuthApi {
    async getCurrentUser(): Promise<CurrentUser> {
        const response = await api.get<CurrentUser>("/api/v1/auth/me");

        return response.data;
    }

    async logoutRequest(): Promise<void> {
        await api.post("/api/v1/auth/logout");
    }

    startGoogleLogin(): void {
        if (!config?.api?.url) {
            throw new Error("API base URL is not configured");
        }

        const redirectUrl = window.location.origin;

        const loginUrl = new URL("/api/v1/auth/login", config.api.url);
        loginUrl.searchParams.set("redirectUrl", redirectUrl);

        window.location.assign(loginUrl.toString());
    }
}

const authApi = new AuthApi();

export default authApi;
