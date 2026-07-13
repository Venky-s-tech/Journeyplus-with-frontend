import React, { createContext, useContext, useState, useEffect } from "react";
import { User, UserRole } from "../types";
import { api, setAccessToken } from "./axios";

interface AuthContextType {
  user: User | null;
  accessToken: string | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<User>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [accessTokenState, setAccessTokenState] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchProfile = async (token: string): Promise<User> => {
    setAccessToken(token);
    setAccessTokenState(token);
    try {
      const response = await api.get<User>("/api/users/me");
      setUser(response.data);
      localStorage.setItem("user", JSON.stringify(response.data));
      return response.data;
    } catch (error) {
      logout();
      throw error;
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      const storedRefreshToken = localStorage.getItem("refreshToken");
      if (storedRefreshToken) {
        try {
          // Attempt token refresh
          const response = await api.post("/api/auth/refresh", {
            refreshToken: storedRefreshToken,
          });
          const { accessToken, refreshToken } = response.data;
          if (accessToken) {
            setAccessToken(accessToken);
            setAccessTokenState(accessToken);
            if (refreshToken) {
              localStorage.setItem("refreshToken", refreshToken);
            }
            await fetchProfile(accessToken);
          }
        } catch (e) {
          console.error("Auto login failed: token expired or invalid", e);
          logout();
        }
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (username: string, password: string): Promise<User> => {
    const response = await api.post("/api/auth/login", { username, password });
    const { accessToken, refreshToken, role } = response.data;
    
    setAccessToken(accessToken);
    setAccessTokenState(accessToken);
    localStorage.setItem("refreshToken", refreshToken);

    const userProfile = await fetchProfile(accessToken);
    return userProfile;
  };

  const logout = () => {
    setAccessToken(null);
    setAccessTokenState(null);
    setUser(null);
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken: accessTokenState,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
