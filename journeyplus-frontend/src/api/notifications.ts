import api from "../lib/axios";
import { Notification } from "../types";

export const getNotifications = async (): Promise<{ notifications: Notification[] }> => {
  const response = await api.get<{ notifications: Notification[] }>("/api/notifications");
  return response.data;
};

export const getUnreadNotifications = async (): Promise<Notification[]> => {
  const response = await api.get<Notification[]>("/api/notifications/unread");
  return response.data;
};

export const markNotificationRead = async (id: number): Promise<any> => {
  const response = await api.post(`/api/notifications/${id}/read`);
  return response.data;
};

export const dismissNotification = async (id: number): Promise<any> => {
  const response = await api.post(`/api/notifications/${id}/dismiss`);
  return response.data;
};
