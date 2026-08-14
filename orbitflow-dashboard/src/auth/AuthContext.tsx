import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { clearToken, getToken, setToken } from "../api/client";
import { login as loginRequest } from "../api/endpoints";

interface AuthContextValue {
  isAuthenticated: boolean;
  username: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function usernameFromToken(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.sub ?? null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken());

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginRequest(username, password);
    setToken(response.accessToken);
    setTokenState(response.accessToken);
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setTokenState(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: token !== null,
      username: token ? usernameFromToken(token) : null,
      login,
      logout,
    }),
    [token, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
