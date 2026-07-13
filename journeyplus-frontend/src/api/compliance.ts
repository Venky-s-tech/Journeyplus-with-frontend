import api from "../lib/axios";
import { ComplianceException } from "../types";

export const getExceptions = async (status?: string): Promise<ComplianceException[]> => {
  const response = await api.get<ComplianceException[]>("/api/compliance/exceptions", {
    params: { status },
  });
  return response.data;
};

export const resolveException = async (
  id: number,
  action: string,
  justification: string
): Promise<any> => {
  const response = await api.post(`/api/compliance/exceptions/${id}/resolve`, null, {
    params: { action, justification },
  });
  return response.data;
};

export const auditClaim = async (
  claimId: number,
  params: { findings: string; outcome: string; status: string }
): Promise<any> => {
  const response = await api.post(`/api/compliance/claims/${claimId}/audit`, null, {
    params,
  });
  return response.data;
};
