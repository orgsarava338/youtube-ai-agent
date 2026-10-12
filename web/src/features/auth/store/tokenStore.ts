import storageUtils from "@/shared/utils/StorageUtil";

const REFRESH_TOKEN_KEY = "assistube_refresh_token";

class TokenStore {
    private accessToken: string | null = null;

    getAccessToken(): string | null {
        return this.accessToken;
    }

    setAccessToken(token: string): void {
        this.accessToken = token;
    }

    clearAccessToken(): void {
        this.accessToken = null;
    }

    getRefreshToken(): string | null {
        return storageUtils.get<string>(REFRESH_TOKEN_KEY);
    }

    setRefreshToken(token: string): boolean {
        return storageUtils.set(REFRESH_TOKEN_KEY, token);
    }

    clearRefreshToken(): void {
        storageUtils.remove(REFRESH_TOKEN_KEY);
    }

    clearTokens(): void {
        this.clearAccessToken();
        this.clearRefreshToken();
    }
}

const tokenStore = new TokenStore();

export default tokenStore;
