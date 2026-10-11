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

        window.location.assign(`${config.api.url}/api/v1/auth/login`);
    }
}

const authApi = new AuthApi();

export default authApi;
