import axios, { type InternalAxiosRequestConfig } from "axios";

import config from "@/config";
import tokenStore from "@/features/auth/store/tokenStore";

interface TokenResponse {
    accessToken: string;
    tokenType: "Bearer";
    expiresIn: number;
    refreshToken?: string;
}

type RetryConfig = InternalAxiosRequestConfig & {
    _retry?: boolean;
};

export const api = axios.create({
    baseURL: config.api.url,
    withCredentials: false,
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use((request) => {
    const token = tokenStore.getAccessToken();

    if (token) {
        request.headers.set("Authorization", `Bearer ${token}`);
    }

    return request;
});

let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
    const refreshToken = tokenStore.getRefreshToken();

    if (!refreshToken) {
        throw new Error("No refresh token available");
    }

    if (!refreshPromise) {
        refreshPromise = axios
            .post<TokenResponse>(
                `${config.api.url}/api/v1/auth/token/refresh`,
                { refreshToken },
                {
                    withCredentials: false,
                    headers: {
                        "Content-Type": "application/json",
                    },
                },
            )
            .then(({ data }) => {
                if (data.tokenType !== "Bearer" || !data.accessToken) {
                    throw new Error("Invalid refresh response");
                }

                tokenStore.setAccessToken(data.accessToken);

                // The backend should rotate refresh tokens.
                if (data.refreshToken) {
                    tokenStore.setRefreshToken(data.refreshToken);
                }

                return data.accessToken;
            })
            .catch((error: unknown) => {
                tokenStore.clearTokens();
                throw error;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }

    return refreshPromise;
}

api.interceptors.response.use(
    (response) => response,
    async (error: unknown) => {
        if (!axios.isAxiosError(error) || !error.config) {
            return Promise.reject(error);
        }

        const request = error.config as RetryConfig;

        if (
            error.response?.status !== 401 ||
            request._retry ||
            request.url?.includes("/api/v1/auth/token/")
        ) {
            return Promise.reject(error);
        }

        request._retry = true;

        try {
            const token = await refreshAccessToken();
            request.headers.set("Authorization", `Bearer ${token}`);
            return api(request);
        } catch (refreshError) {
            return Promise.reject(refreshError);
        }
    },
);
