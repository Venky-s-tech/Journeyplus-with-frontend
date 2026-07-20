import api from "../lib/axios";

export interface DashboardSummaryResponse {
  role: string;
  userId: number;
  totalTrips?: number;
  upcomingTrips?: number;
  draftTrips?: number;
  pendingApprovalTrips?: number;
  approvedTrips?: number;
  rejectedTrips?: number;
  completedTrips?: number;
  totalExpenseClaims?: number;
  draftClaims?: number;
  submittedClaims?: number;
  approvedClaims?: number;
  rejectedClaims?: number;
  paidClaims?: number;
  advanceRequests?: number;
  pendingAdvances?: number;
  approvedAdvances?: number;
  settledAdvances?: number;
  activeCashAdvanceAmount?: number;
  pendingTripApprovals?: number;
  pendingExpenseApprovals?: number;
  pendingAdvanceRequests?: number;
  teamTravelSummaryCount?: number;
  teamExpenseSummaryCount?: number;
  employeesCurrentlyTravelling?: number;
  pendingBookings?: number;
  flightBookings?: number;
  hotelBookings?: number;
  visaRequests?: number;
  completedItineraries?: number;
  upcomingTravel?: number;
  pendingReimbursements?: number;
  pendingAdvanceDisbursements?: number;
  processedPayments?: number;
  failedPayments?: number;
  totalBudgetAllocated?: number;
  totalDisbursedAdvances?: number;
  totalExpenseSpend?: number;
  policyExceptions?: number;
  openExceptions?: number;
  highValueClaims?: number;
  auditSummaryCount?: number;
  users?: number;
  activeUsers?: number;
  inactiveUsers?: number;
  departments?: number;
  roles?: number;
  trips?: number;
  expenses?: number;
  policies?: number;
  advances?: number;
  complianceCases?: number;
  activeGrades?: number;
}

export interface AnalyticsSummaryResponse {
  budgetUtilisationPct: number;
  advanceSettlementRatePct: number;
  policyExceptionRatePct: number;
  totalSpendUsd: number;
}

export interface SpendByDepartmentItem {
  name: string;
  amount: number;
}

export interface SpendByCategoryItem {
  name: string;
  value: number;
}

export interface MonthlyTrendItem {
  month: string;
  amount: number;
}

export const getDashboardSummary = async (role?: string): Promise<DashboardSummaryResponse> => {
  const response = await api.get<DashboardSummaryResponse>("/api/dashboard/summary", {
    params: { role },
  });
  return response.data;
};

export const getAdminDashboard = async (): Promise<any> => {
  const response = await api.get("/api/dashboard/admin");
  return response.data;
};

export const getEmployeeDashboard = async (): Promise<any> => {
  const response = await api.get("/api/dashboard/employee");
  return response.data;
};

export const getManagerDashboard = async (): Promise<any> => {
  const response = await api.get("/api/dashboard/manager");
  return response.data;
};

export const getFinanceDashboard = async (): Promise<any> => {
  const response = await api.get("/api/dashboard/finance");
  return response.data;
};

export const getTravelDeskDashboard = async (): Promise<any> => {
  const response = await api.get("/api/dashboard/traveldesk");
  return response.data;
};

export const getComplianceDashboard = async (): Promise<any> => {
  const response = await api.get("/api/dashboard/compliance");
  return response.data;
};

export const getAnalyticsSummary = async (): Promise<AnalyticsSummaryResponse> => {
  const response = await api.get<AnalyticsSummaryResponse>("/api/analytics/summary");
  return response.data;
};

export const getSpendByDepartment = async (): Promise<SpendByDepartmentItem[]> => {
  const response = await api.get<SpendByDepartmentItem[]>("/api/analytics/spend-by-department");
  return response.data;
};

export const getSpendByCategory = async (): Promise<SpendByCategoryItem[]> => {
  const response = await api.get<SpendByCategoryItem[]>("/api/analytics/spend-by-category");
  return response.data;
};

export const getMonthlyTrends = async (): Promise<MonthlyTrendItem[]> => {
  const response = await api.get<MonthlyTrendItem[]>("/api/analytics/monthly-trends");
  return response.data;
};
