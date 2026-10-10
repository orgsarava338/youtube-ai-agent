import axios, { AxiosError } from "axios";
import type { InternalAxiosRequestConfig } from "axios";

import config from "@/config";

interface CsrfResponse {
    token: string;
    headerName: string;
}

export const api = axios.create({
    baseURL: config.api.url,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
});

const csrfApi = axios.create({
    baseURL: config.api.url,
    withCredentials: true,
});

let csrfToken: CsrfResponse | null = null;
let csrfRequest: Promise<CsrfResponse> | null = null;

export async function refreshCsrfToken(): Promise<CsrfResponse> {
    csrfToken = null;
    csrfRequest = null;
    return getCsrfToken();
}

async function getCsrfToken(): Promise<CsrfResponse> {
    if (csrfToken) {
        return csrfToken;
    }

    if (!csrfRequest) {
        csrfRequest = csrfApi
            .get<CsrfResponse>("/api/v1/auth/csrf")
            .then((response) => {
                csrfToken = response.data;
                return response.data;
            })
            .finally(() => {
                csrfRequest = null;
            });
    }

    return csrfRequest;
}

api.interceptors.request.use(
    async (request: InternalAxiosRequestConfig) => {
        const method = request.method?.toLowerCase();

        if (["post", "put", "patch", "delete"].includes(method ?? "")) {
            const csrf = await getCsrfToken();
            request.headers.set(csrf.headerName, csrf.token);
        }

        return request;
    },
    (error: AxiosError) => Promise.reject(error),
);

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        if (error.response?.status === 403) {
            const request = error.config as
                | (InternalAxiosRequestConfig & { _csrfRetried?: boolean })
                | undefined;

            if (
                request &&
                !request._csrfRetried &&
                ["post", "put", "patch", "delete"].includes(
                    request.method?.toLowerCase() ?? "",
                )
            ) {
                request._csrfRetried = true;
                await refreshCsrfToken();
                return api.request(request);
            }
        }

        return Promise.reject(error);
    },
);
