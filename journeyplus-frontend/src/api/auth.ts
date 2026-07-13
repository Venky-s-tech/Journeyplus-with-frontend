import api from "../lib/axios";
import { UserRegistration } from "../types";

export const registerUser = async (data: UserRegistration): Promise<any> => {
  const response = await api.post("/api/auth/register", data);
  return response.data;
};
