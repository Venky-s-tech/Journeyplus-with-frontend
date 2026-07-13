import api from "../lib/axios";
import { User, Grade, CityTier, TravelPolicy, AuditLog, DelegationSetup } from "../types";

// User management
export const getUsers = async (params?: any): Promise<User[]> => {
  const response = await api.get<User[]>("/api/users", { params });
  return response.data;
};

export const createUserDirect = async (data: Partial<User> & { password?: string }): Promise<User> => {
  const response = await api.post<User>("/api/users", data);
  return response.data;
};

export const updateUserProfile = async (id: number, data: Partial<User>): Promise<User> => {
  const response = await api.put<User>(`/api/users/${id}`, data);
  return response.data;
};

export const deactivateUser = async (id: number): Promise<any> => {
  const response = await api.delete(`/api/users/${id}`);
  return response.data;
};

export const setupDelegation = async (data: DelegationSetup): Promise<any> => {
  const response = await api.post("/api/users/delegate", data);
  return response.data;
};

export const getPendingUsers = async (): Promise<User[]> => {
  const response = await api.get<User[]>("/api/admin/pending");
  return response.data;
};

export const approveUser = async (id: number): Promise<any> => {
  const response = await api.post(`/api/admin/approve/${id}`);
  return response.data;
};

export const rejectUser = async (id: number): Promise<any> => {
  const response = await api.post(`/api/admin/reject/${id}`);
  return response.data;
};

export const getUserRole = async (id: number): Promise<{ role: string }> => {
  const response = await api.get<{ role: string }>(`/api/admin/users/${id}/role`);
  return response.data;
};

export const updateUserRole = async (id: number, role: string): Promise<any> => {
  const response = await api.post(`/api/admin/users/${id}/role`, { role });
  return response.data;
};

// Grades
export const getGrades = async (): Promise<Grade[]> => {
  const response = await api.get<Grade[]>("/api/grades");
  return response.data;
};

export const getGradeDetails = async (id: string): Promise<Grade> => {
  const response = await api.get<Grade>(`/api/grades/${id}`);
  return response.data;
};

export const createGrade = async (data: Grade): Promise<Grade> => {
  const response = await api.post<Grade>("/api/grades", data);
  return response.data;
};

export const updateGrade = async (id: string, data: Partial<Grade>): Promise<Grade> => {
  const response = await api.put<Grade>(`/api/grades/${id}`, data);
  return response.data;
};

export const deleteGrade = async (id: string): Promise<any> => {
  const response = await api.delete(`/api/grades/${id}`);
  return response.data;
};

// City Tiers
export const getCityTiers = async (): Promise<CityTier[]> => {
  const response = await api.get<CityTier[]>("/api/city-tiers");
  return response.data;
};

export const getCityTierDetails = async (id: number): Promise<CityTier> => {
  const response = await api.get<CityTier>(`/api/city-tiers/${id}`);
  return response.data;
};

export const createCityTier = async (data: Partial<CityTier>): Promise<CityTier> => {
  const response = await api.post<CityTier>("/api/city-tiers", data);
  return response.data;
};

export const updateCityTier = async (id: number, data: Partial<CityTier>): Promise<CityTier> => {
  const response = await api.put<CityTier>(`/api/city-tiers/${id}`, data);
  return response.data;
};

export const deleteCityTier = async (id: number): Promise<any> => {
  const response = await api.delete(`/api/city-tiers/${id}`);
  return response.data;
};

export const getCityCostDetails = async (cityName: string, country: string): Promise<any> => {
  const response = await api.get("/api/city-tiers/cost-details", {
    params: { cityName, country },
  });
  return response.data;
};

// Travel Policies
export const getTravelPolicies = async (): Promise<TravelPolicy[]> => {
  const response = await api.get<TravelPolicy[]>("/api/travel-policies");
  return response.data;
};

export const getTravelPolicyDetails = async (id: number): Promise<TravelPolicy> => {
  const response = await api.get<TravelPolicy>(`/api/travel-policies/${id}`);
  return response.data;
};

export const createTravelPolicy = async (data: Partial<TravelPolicy>): Promise<TravelPolicy> => {
  const response = await api.post<TravelPolicy>("/api/travel-policies", data);
  return response.data;
};

export const updateTravelPolicy = async (id: number, data: Partial<TravelPolicy>): Promise<TravelPolicy> => {
  const response = await api.put<TravelPolicy>(`/api/travel-policies/${id}`, data);
  return response.data;
};

export const deleteTravelPolicy = async (id: number): Promise<any> => {
  const response = await api.delete(`/api/travel-policies/${id}`);
  return response.data;
};

export const searchTravelPolicy = async (gradeId: string, travelType: string): Promise<TravelPolicy> => {
  const response = await api.get<TravelPolicy>("/api/travel-policies/search", {
    params: { gradeId, travelType },
  });
  return response.data;
};

export const calculateAllowance = async (
  gradeId: string,
  travelType: string,
  cityName: string,
  country: string
): Promise<any> => {
  const response = await api.get("/api/travel-policies/calculate-allowance", {
    params: { gradeId, travelType, cityName, country },
  });
  return response.data;
};

// Audit logs
export const getAuditLogs = async (): Promise<AuditLog[]> => {
  const response = await api.get<AuditLog[]>("/api/audit");
  return response.data;
};
