import api from "../lib/axios";
import { TravelAdvance, Settlement } from "../types";

export const getAdvances = async (): Promise<TravelAdvance[]> => {
  const response = await api.get<TravelAdvance[]>("/api/advances");
  return response.data;
};

export const getMyAdvances = async (): Promise<TravelAdvance[]> => {
  const response = await api.get<TravelAdvance[]>("/api/advances/my-advances");
  return response.data;
};

export const getPendingAdvanceApprovals = async (): Promise<TravelAdvance[]> => {
  const response = await api.get<TravelAdvance[]>("/api/advances/pending-approvals");
  return response.data;
};

export const getPendingDisbursements = async (): Promise<TravelAdvance[]> => {
  const response = await api.get<TravelAdvance[]>("/api/advances/pending-disbursements");
  return response.data;
};

export const getAdvanceDetails = async (id: number): Promise<TravelAdvance> => {
  const response = await api.get<TravelAdvance>(`/api/advances/${id}`);
  return response.data;
};

export const getAdvanceSummary = async (id: number): Promise<any> => {
  const response = await api.get(`/api/advances/${id}/summary`);
  return response.data;
};

export const requestAdvance = async (data: Partial<TravelAdvance>): Promise<TravelAdvance> => {
  const response = await api.post<TravelAdvance>("/api/advances", data);
  return response.data;
};

export const updateAdvance = async (id: number, data: Partial<TravelAdvance>): Promise<TravelAdvance> => {
  const response = await api.put<TravelAdvance>(`/api/advances/${id}`, data);
  return response.data;
};

export const approveAdvance = async (id: number): Promise<TravelAdvance> => {
  const response = await api.post<TravelAdvance>(`/api/advances/${id}/approve`);
  return response.data;
};

export const disburseAdvance = async (id: number): Promise<TravelAdvance> => {
  const response = await api.post<TravelAdvance>(`/api/advances/${id}/disburse`);
  return response.data;
};

export const forfeitAdvance = async (id: number): Promise<TravelAdvance> => {
  const response = await api.post<TravelAdvance>(`/api/advances/${id}/forfeit`);
  return response.data;
};

// Settlements
export const getAdvanceSettlements = async (advanceId: number): Promise<Settlement[]> => {
  const response = await api.get<Settlement[]>(`/api/advances/${advanceId}/settlements`);
  return response.data;
};

export const createSettlement = async (advanceId: number, data: Partial<Settlement>): Promise<Settlement> => {
  const response = await api.post<Settlement>(`/api/advances/${advanceId}/settlements`, data);
  return response.data;
};

export const settleAdvance = async (id: number, data: Partial<Settlement>): Promise<TravelAdvance> => {
  const response = await api.post<TravelAdvance>(`/api/advances/${id}/settle`, data);
  return response.data;
};

export const getSettlementDetails = async (id: number): Promise<Settlement> => {
  const response = await api.get<Settlement>(`/api/settlements/${id}`);
  return response.data;
};

export const updateSettlement = async (id: number, data: Partial<Settlement>): Promise<Settlement> => {
  const response = await api.put<Settlement>(`/api/settlements/${id}`, data);
  return response.data;
};
