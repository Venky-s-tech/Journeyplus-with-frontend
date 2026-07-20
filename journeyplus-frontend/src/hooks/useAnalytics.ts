import { useQuery } from "@tanstack/react-query";
import {
  getDashboardSummary,
  getAdminDashboard,
  getEmployeeDashboard,
  getManagerDashboard,
  getFinanceDashboard,
  getTravelDeskDashboard,
  getComplianceDashboard,
  getAnalyticsSummary,
  getSpendByDepartment,
  getSpendByCategory,
  getMonthlyTrends,
  DashboardSummaryResponse,
  AnalyticsSummaryResponse,
  SpendByDepartmentItem,
  SpendByCategoryItem,
  MonthlyTrendItem,
} from "../api/analytics";

export const useDashboardSummary = (role?: string) => {
  return useQuery<DashboardSummaryResponse>({
    queryKey: ["dashboard-summary", role],
    queryFn: () => getDashboardSummary(role),
    staleTime: 30000,
  });
};

export const useAdminDashboard = () => {
  return useQuery({
    queryKey: ["dashboard-admin"],
    queryFn: getAdminDashboard,
    staleTime: 30000,
  });
};

export const useEmployeeDashboard = () => {
  return useQuery({
    queryKey: ["dashboard-employee"],
    queryFn: getEmployeeDashboard,
    staleTime: 30000,
  });
};

export const useManagerDashboard = () => {
  return useQuery({
    queryKey: ["dashboard-manager"],
    queryFn: getManagerDashboard,
    staleTime: 30000,
  });
};

export const useFinanceDashboard = () => {
  return useQuery({
    queryKey: ["dashboard-finance"],
    queryFn: getFinanceDashboard,
    staleTime: 30000,
  });
};

export const useTravelDeskDashboard = () => {
  return useQuery({
    queryKey: ["dashboard-traveldesk"],
    queryFn: getTravelDeskDashboard,
    staleTime: 30000,
  });
};

export const useComplianceDashboard = () => {
  return useQuery({
    queryKey: ["dashboard-compliance"],
    queryFn: getComplianceDashboard,
    staleTime: 30000,
  });
};

export const useAnalyticsSummary = () => {
  return useQuery<AnalyticsSummaryResponse>({
    queryKey: ["analytics-summary"],
    queryFn: getAnalyticsSummary,
    staleTime: 60000,
  });
};

export const useSpendByDepartment = () => {
  return useQuery<SpendByDepartmentItem[]>({
    queryKey: ["spend-by-department"],
    queryFn: getSpendByDepartment,
    staleTime: 60000,
  });
};

export const useSpendByCategory = () => {
  return useQuery<SpendByCategoryItem[]>({
    queryKey: ["spend-by-category"],
    queryFn: getSpendByCategory,
    staleTime: 60000,
  });
};

export const useMonthlyTrends = () => {
  return useQuery<MonthlyTrendItem[]>({
    queryKey: ["monthly-trends"],
    queryFn: getMonthlyTrends,
    staleTime: 60000,
  });
};
