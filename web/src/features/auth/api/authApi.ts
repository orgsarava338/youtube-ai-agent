import config from "@/config";
import { api } from "@/lib/api";

export async function getCurrentUser(): Promise<CurrentUser> {
  const response = await api.get<CurrentUser>("/api/v1/auth/me");
  return response.data;
}

export async function logoutRequest(): Promise<void> {
    const csrfResponse = await api.get<CsrfResponse>("/api/v1/auth/csrf");
    const { token, headerName } = csrfResponse.data;
  await api.post("/logout", {}, {
    headers: { [headerName]: token }
  });
}

export function startGoogleLogin(): void {
    if (!config?.api?.url) {
        throw new Error("API base URL is not configured");
    }

  window.location.assign(`${config.api.url}/oauth2/authorization/google`);
}