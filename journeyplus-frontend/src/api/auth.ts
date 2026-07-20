import api from "../lib/axios";
import { UserRegistration } from "../types";

export const registerUser = async (data: UserRegistration): Promise<any> => {
  const response = await api.post("/api/auth/register", data);
  return response.data;
};

export const getMe = async (): Promise<any> => {
  const response = await api.get("/api/auth/me");
  return response.data;
};

export const logoutUser = async (): Promise<any> => {
  const response = await api.post("/api/auth/logout");
  return response.data;
};
