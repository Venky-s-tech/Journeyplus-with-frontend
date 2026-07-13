import api from "../lib/axios";

export interface Report {
  id: number;
  title: string;
  reportType: string;
  generatedDate: string;
  filePath?: string;
  status: string;
}

export const getReports = async (): Promise<Report[]> => {
  const response = await api.get<Report[]>("/api/reports");
  return response.data;
};

export const getReportsByType = async (type: string): Promise<Report[]> => {
  const response = await api.get<Report[]>(`/api/reports/type/${type}`);
  return response.data;
};

export const generateReport = async (title: string, reportType: string): Promise<Report> => {
  const response = await api.post<Report>("/api/reports", null, {
    params: { title, reportType },
  });
  return response.data;
};

export const generateMetricsReport = async (scope: string, scopeValue: string): Promise<any> => {
  const response = await api.post("/api/reports/metrics", null, {
    params: { scope, scopeValue },
  });
  return response.data;
};

export const getTopTravellers = async (): Promise<any[]> => {
  const response = await api.get<any[]>("/api/reports/top-travellers");
  return response.data;
};
