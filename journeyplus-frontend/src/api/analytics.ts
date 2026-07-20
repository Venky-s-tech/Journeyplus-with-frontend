import api from "../lib/axios";

export interface DashboardSummaryResponse {
  role?: string;
  userId?: number;
  // Admin fields
  totalUsers?: number;
  activeUsers?: number;
  inactiveUsers?: number;
  pendingUserApprovals?: number;
  totalTrips?: number;
  draftTrips?: number;
  submittedTrips?: number;
  approvedTrips?: number;
  rejectedTrips?: number;
  completedTrips?: number;
  cancelledTrips?: number;
  totalExpenseClaims?: number;
  pendingExpenseClaims?: number;
  approvedExpenseClaims?: number;
  rejectedExpenseClaims?: number;
  paidClaims?: number;
  totalExpenseAmount?: number;
  totalAdvances?: number;
  pendingAdvances?: number;
  approvedAdvances?: number;
  disbursedAdvances?: number;
  settledAdvances?: number;
  totalPolicies?: number;
  activePolicies?: number;
  unreadNotifications?: number;
  totalComplianceCases?: number;

  // Employee fields
  myTrips?: number;
  myExpenseClaims?: number;
  myAdvances?: number;
  upcomingTrips?: number;
  pendingTrips?: number;
  pendingClaims?: number;
  approvedClaims?: number;
  rejectedClaims?: number;
  activeCashAdvanceAmount?: number;

  // Manager fields
  tripsAwaitingApproval?: number;
  expenseClaimsAwaitingApproval?: number;
  advanceRequestsAwaitingApproval?: number;
  pendingTripApprovals?: number;
  pendingExpenseApprovals?: number;
  pendingAdvanceRequests?: number;
  submittedClaims?: number;
  teamTripCount?: number;
  teamExpenseAmount?: number;
  employeesCurrentlyTravelling?: number;
  recentApprovals?: number;

  // Finance fields
  pendingReimbursements?: number;
  pendingDisbursements?: number;
  processedPayments?: number;
  failedPayments?: number;
  monthlyReimbursementAmount?: number;
  monthlyAdvanceAmount?: number;
  totalBudgetAllocated?: number;
  totalDisbursedAdvances?: number;

  // Travel Desk fields
  pendingBookings?: number;
  completedItineraries?: number;
  visaRequests?: number;
  travelRequests?: number;
  flightBookings?: number;
  hotelBookings?: number;
  upcomingTravel?: number;

  // Compliance fields
  openExceptions?: number;
  closedExceptions?: number;
  escalatedExceptions?: number;
  policyViolations?: number;
  highValueClaims?: number;
  auditSummaryCount?: number;
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
    params: role ? { role } : {},
  });
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
