import api from "../lib/axios";
import { ExpenseClaim, ExpenseLine } from "../types";

export const getMyClaims = async (): Promise<ExpenseClaim[]> => {
  const response = await api.get<ExpenseClaim[]>("/api/expenses/my-claims");
  return response.data;
};

export const getClaimDetails = async (id: number): Promise<ExpenseClaim> => {
  const response = await api.get<ExpenseClaim>(`/api/expenses/${id}`);
  return response.data;
};

export const getClaimLines = async (id: number): Promise<ExpenseLine[]> => {
  const response = await api.get<ExpenseLine[]>(`/api/expenses/${id}/lines`);
  return response.data;
};

export const createClaim = async (tripRequestId: number, data: Partial<ExpenseClaim>): Promise<ExpenseClaim> => {
  const response = await api.post<ExpenseClaim>("/api/expenses", data, {
    params: { tripRequestId },
  });
  return response.data;
};

export const addClaimLine = async (claimId: number, data: Partial<ExpenseLine>): Promise<ExpenseLine> => {
  const response = await api.post<ExpenseLine>(`/api/expenses/${claimId}/lines`, data);
  return response.data;
};

export const submitClaimLine = async (claimId: number, lineId: number): Promise<any> => {
  const response = await api.post(`/api/expenses/${claimId}/lines/${lineId}/submit`);
  return response.data;
};

export const submitClaim = async (claimId: number): Promise<ExpenseClaim> => {
  const response = await api.post<ExpenseClaim>(`/api/expenses/${claimId}/submit`);
  return response.data;
};

export const approveClaim = async (claimId: number, comments?: string): Promise<ExpenseClaim> => {
  const response = await api.post<ExpenseClaim>(
    `/api/expenses/${claimId}/approve`,
    null,
    { params: { comments } }
  );
  return response.data;
};

export const rejectClaim = async (claimId: number, comments?: string): Promise<ExpenseClaim> => {
  const response = await api.post<ExpenseClaim>(
    `/api/expenses/${claimId}/reject`,
    null,
    { params: { comments } }
  );
  return response.data;
};

export const reimburseClaim = async (
  claimId: number,
  data: { paymentMethod: string; transactionReference: string; amount: number }
): Promise<any> => {
  const response = await api.post(`/api/expenses/${claimId}/reimburse`, data);
  return response.data;
};

// Document Receipt Upload
export const uploadReceipt = async (file: File): Promise<{ id: number; filePath: string }> => {
  const formData = new FormData();
  formData.append("file", file);
  
  const response = await api.post("/api/documents/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
};

export const getDocumentDetails = async (id: number): Promise<any> => {
  const response = await api.get(`/api/documents/${id}`);
  return response.data;
};
