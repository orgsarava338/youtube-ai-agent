
import authApi from "@/features/auth/api/authApi";

import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";


export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined
);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(async (): Promise<void> => {
    try {
      const currentUser = await authApi.getCurrentUser();
      setUser(currentUser);
    } catch {
      setUser(null);
    }
  }, []);


useEffect(() => {
    let active = true;

    async function initializeAuth() {
        try {
            const authenticated = await authApi.initializeAuth();

            if (!active) {
                return;
            }

            if (!authenticated) {
                setUser(null);
                return;
            }

            const currentUser = await authApi.getCurrentUser();

            if (active) {
                setUser(currentUser);
            }
        } catch (error) {
            if (active) {
                setUser(null);
            }
        } finally {
            if (active) {
                setLoading(false);
            }
        }
    }

    void initializeAuth();

    return () => {
        active = false;
    };
}, []);

  const login = useCallback(() => {
    authApi.startGoogleLogin();
  }, []);

  const logout = useCallback(async (): Promise<void> => {
    await authApi.logout();
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      login,
      logout,
      refreshUser,
    }),
    [user, loading, login, logout, refreshUser]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
