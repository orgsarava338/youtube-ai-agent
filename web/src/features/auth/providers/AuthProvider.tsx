import {
    useCallback,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";

import authApi from "@/features/auth/api/authApi";
import { AuthContext } from "@/features/auth/providers/AuthContext";
import { authQueryKeys } from "@/features/auth/queries/authQueryKeys";

interface AuthProviderProps {
    children: ReactNode;
}

type SessionStatus = "initializing" | "authenticated" | "anonymous";

export function AuthProvider({ children }: AuthProviderProps) {
    const queryClient = useQueryClient();

    const [sessionStatus, setSessionStatus] =
        useState<SessionStatus>("initializing");

    // Restore the session before allowing the current-user query to run.
    useEffect(() => {
        let active = true;

        async function initializeAuth() {
            try {
                const authenticated = await authApi.initializeAuth();

                if (active) {
                    setSessionStatus(
                        authenticated ? "authenticated" : "anonymous",
                    );
                }
            } catch (error) {
                console.error("Authentication initialization failed:", error);

                if (active) {
                    setSessionStatus("anonymous");
                }
            }
        }

        void initializeAuth();

        return () => {
            active = false;
        };
    }, []);

    // TanStack Query now owns the current-user server state.
    const currentUserQuery = useQuery({
        queryKey: authQueryKeys.me(),
        queryFn: () => authApi.getCurrentUser(),
        enabled: sessionStatus === "authenticated",
        retry: false,
    });

    const user =
        sessionStatus === "authenticated"
            ? (currentUserQuery.data ?? null)
            : null;

    const loading =
        sessionStatus === "initializing" ||
        (sessionStatus === "authenticated" && currentUserQuery.isPending);

    const refreshUser = useCallback(async (): Promise<void> => {
        if (sessionStatus !== "authenticated") {
            return;
        }

        const result = await currentUserQuery.refetch();

        if (result.error) {
            console.error("Unable to refresh current user:", result.error);
        }
    }, [sessionStatus, currentUserQuery.refetch]);

    const login = useCallback(() => {
        authApi.startGoogleLogin();
    }, []);

    const logout = useCallback(async (): Promise<void> => {
        try {
            await authApi.logout();
        } finally {
            // Clear private cached data even if the logout request fails.
            await queryClient.cancelQueries();
            queryClient.clear();

            setSessionStatus("anonymous");
        }
    }, [queryClient]);

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            loading,
            login,
            logout,
            refreshUser,
        }),
        [user, loading, login, logout, refreshUser],
    );

    return (
        <AuthContext.Provider value={value}>
            {loading ? (
                <div className="flex min-h-screen items-center justify-center">
                    Loading your session...
                </div>
            ) : (
                children
            )}
        </AuthContext.Provider>
    );
}
