import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as tripsApi from "../api/trips";
import * as advancesApi from "../api/advances";
import * as expensesApi from "../api/expenses";
import * as complianceApi from "../api/compliance";
import * as reportsApi from "../api/reports";
import * as notificationsApi from "../api/notifications";
import * as adminApi from "../api/admin";
import { UserRole } from "../types";

// ==========================================
// TRIPS HOOKS
// ==========================================
export const useTrips = (role: UserRole) => {
  return useQuery({
    queryKey: ["trips", role],
    queryFn: () => {
      if (role === "EMPLOYEE") {
        return tripsApi.getMyTrips();
      } else if (role === "APPROVING_MANAGER") {
        return tripsApi.getPendingTripApprovals();
      } else {
        return tripsApi.getTrips();
      }
    },
  });
};

export const useTrip = (id: number) => {
  return useQuery({
    queryKey: ["trip", id],
    queryFn: () => tripsApi.getTripDetails(id),
    enabled: !!id,
  });
};

export const useCreateTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripsApi.createTrip,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

export const useUpdateTrip = (id: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: any) => tripsApi.updateTrip(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", id] });
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

export const useSubmitTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripsApi.submitTrip,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["trip", data.id] });
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

export const useApproveTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, comments }: { id: number; comments?: string }) =>
      tripsApi.approveTrip(id, comments),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["trip", data.id] });
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

export const useRejectTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, comments }: { id: number; comments?: string }) =>
      tripsApi.rejectTrip(id, comments),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["trip", data.id] });
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

export const useCompleteTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripsApi.completeTrip,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["trip", data.id] });
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

export const useCancelTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripsApi.cancelTrip,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["trip", data.id] });
      queryClient.invalidateQueries({ queryKey: ["trips"] });
    },
  });
};

// Itinerary
export const useAddItineraryLeg = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: any) => tripsApi.addItineraryLeg(tripId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["itinerary", tripId] });
    },
  });
};

export const useDeleteItineraryLeg = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripsApi.deleteItineraryLeg,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["itinerary", tripId] });
    },
  });
};

export const useBookItineraryLeg = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      legId,
      bookingReference,
      bookingStatus,
    }: {
      legId: number;
      bookingReference: string;
      bookingStatus: string;
    }) => tripsApi.bookItineraryLeg(tripId, legId, { bookingReference, bookingStatus }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["itinerary", tripId] });
    },
  });
};

// Visa
export const useAddVisaRequirement = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: any) => tripsApi.addVisaRequirement(tripId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["visa", tripId] });
    },
  });
};

export const useUpdateVisaRequirement = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ visaId, data }: { visaId: number; data: any }) =>
      tripsApi.updateVisaRequirement(visaId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["visa", tripId] });
    },
  });
};

// ==========================================
// ADVANCES HOOKS
// ==========================================
export const useAdvances = (role: UserRole) => {
  return useQuery({
    queryKey: ["advances", role],
    queryFn: () => {
      if (role === "EMPLOYEE") {
        return advancesApi.getMyAdvances();
      } else if (role === "APPROVING_MANAGER") {
        return advancesApi.getPendingAdvanceApprovals();
      } else if (role === "FINANCE") {
        return advancesApi.getPendingDisbursements();
      } else {
        return advancesApi.getAdvances();
      }
    },
  });
};

export const useAdvance = (id: number) => {
  return useQuery({
    queryKey: ["advance", id],
    queryFn: () => advancesApi.getAdvanceDetails(id),
    enabled: !!id,
  });
};

// GET /api/advances/{id}/summary - the endpoint that actually includes
// settlement history and running totals (utilised/returned/outstanding).
// The plain useAdvance()/GET /api/advances/{id} response does not include
// these - use this hook for any "View advance details" UI.
export const useAdvanceSummary = (id: number) => {
  return useQuery({
    queryKey: ["advance-summary", id],
    queryFn: () => advancesApi.getAdvanceSummary(id),
    enabled: !!id,
  });
};

export const useRequestAdvance = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: advancesApi.requestAdvance,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["advances"] });
      queryClient.invalidateQueries({ queryKey: ["trip", data.tripRequestId] });
    },
  });
};

export const useApproveAdvance = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: advancesApi.approveAdvance,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["advance", data.id] });
      queryClient.invalidateQueries({ queryKey: ["advances"] });
    },
  });
};

export const useDisburseAdvance = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: advancesApi.disburseAdvance,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["advance", data.id] });
      queryClient.invalidateQueries({ queryKey: ["advances"] });
    },
  });
};

export const useForfeitAdvance = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: advancesApi.forfeitAdvance,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["advance", data.id] });
      queryClient.invalidateQueries({ queryKey: ["advances"] });
    },
  });
};

export const useSettleAdvance = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      advancesApi.settleAdvance(id, data),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["advance", data.id] });
      queryClient.invalidateQueries({ queryKey: ["advances"] });
    },
  });
};

// ==========================================
// EXPENSE CLAIMS HOOKS
// ==========================================
export const useClaims = (role: UserRole) => {
  return useQuery({
    queryKey: ["claims", role],
    queryFn: () => {
      if (role === "EMPLOYEE") {
        return expensesApi.getMyClaims();
      } else {
        // Fetch all claims for Manager / Finance
        return expensesApi.getMyClaims(); // (or endpoints that list active queues)
      }
    },
  });
};

export const useClaim = (id: number) => {
  return useQuery({
    queryKey: ["claim", id],
    queryFn: () => expensesApi.getClaimDetails(id),
    enabled: !!id,
  });
};

// The backend's ExpenseClaim entity has NO expenseLines collection field -
// GET /api/expenses/{id} never returns lines, regardless of how many exist.
// Lines must be fetched from the dedicated GET /api/expenses/{id}/lines
// endpoint, which is what this hook does. This is the fix for "expense
// lines are adding but not showing": the claim response was never going to
// contain them no matter how the claim data was reloaded.
export const useClaimLines = (claimId: number) => {
  return useQuery({
    queryKey: ["claim-lines", claimId],
    queryFn: () => expensesApi.getClaimLines(claimId),
    enabled: !!claimId,
  });
};

export const useCreateClaim = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tripId, data }: { tripId: number; data: any }) =>
      expensesApi.createClaim(tripId, data),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

export const useAddClaimLine = (claimId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: any) => expensesApi.addClaimLine(claimId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["claim", claimId] });
      queryClient.invalidateQueries({ queryKey: ["claim-lines", claimId] });
    },
  });
};

export const useUpdateClaimLine = (claimId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ lineId, data }: { lineId: number; data: any }) =>
      expensesApi.updateClaimLine(claimId, lineId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["claim", claimId] });
      queryClient.invalidateQueries({ queryKey: ["claim-lines", claimId] });
      queryClient.invalidateQueries({ queryKey: ["exceptions"] });
    },
  });
};

export const useDeleteClaimLine = (claimId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (lineId: number) => expensesApi.deleteClaimLine(claimId, lineId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["claim", claimId] });
      queryClient.invalidateQueries({ queryKey: ["claim-lines", claimId] });
      queryClient.invalidateQueries({ queryKey: ["exceptions"] });
    },
  });
};

export const useUploadLineReceipt = (claimId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ lineId, file }: { lineId: number; file: File }) =>
      expensesApi.uploadLineReceipt(claimId, lineId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["claim", claimId] });
      queryClient.invalidateQueries({ queryKey: ["claim-lines", claimId] });
      queryClient.invalidateQueries({ queryKey: ["exceptions"] });
    },
  });
};

export const useDeleteLineReceipt = (claimId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (lineId: number) => expensesApi.deleteLineReceipt(claimId, lineId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["claim", claimId] });
      queryClient.invalidateQueries({ queryKey: ["claim-lines", claimId] });
      queryClient.invalidateQueries({ queryKey: ["exceptions"] });
    },
  });
};

export const useSubmitClaimLine = (claimId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (lineId: number) => expensesApi.submitClaimLine(claimId, lineId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["claim", claimId] });
      queryClient.invalidateQueries({ queryKey: ["claim-lines", claimId] });
      queryClient.invalidateQueries({ queryKey: ["exceptions"] });
    },
  });
};

export const useSubmitClaim = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: expensesApi.submitClaim,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["claim", data.id] });
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

export const useApproveClaim = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, comments }: { id: number; comments?: string }) =>
      expensesApi.approveClaim(id, comments),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["claim", data.id] });
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

export const useRejectClaim = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, comments }: { id: number; comments?: string }) =>
      expensesApi.rejectClaim(id, comments),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["claim", data.id] });
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

export const useReimburseClaim = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      claimId,
      paymentMethod,
      transactionReference,
      amount,
    }: {
      claimId: number;
      paymentMethod: string;
      transactionReference: string;
      amount: number;
    }) => expensesApi.reimburseClaim(claimId, { paymentMethod, transactionReference, amount }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["claim", variables.claimId] });
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

// ==========================================
// COMPLIANCE HOOKS
// ==========================================
export const useExceptions = (status?: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: ["exceptions", status],
    queryFn: () => complianceApi.getExceptions(status),
    enabled,
  });
};

export const useResolveException = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      action,
      justification,
    }: {
      id: number;
      action: string;
      justification: string;
    }) => complianceApi.resolveException(id, action, justification),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["exceptions"] });
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

export const useAuditClaim = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      claimId,
      params,
    }: {
      claimId: number;
      params: { findings: string; outcome: string; status: string };
    }) => complianceApi.auditClaim(claimId, params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["claim", variables.claimId] });
      queryClient.invalidateQueries({ queryKey: ["claims"] });
    },
  });
};

// ==========================================
// ANALYTICS & REPORTS
// ==========================================
export const useReports = () => {
  return useQuery({
    queryKey: ["reports"],
    queryFn: reportsApi.getReports,
    // Bug #9: keep analytics data current in real time.
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
    staleTime: 0,
  });
};

export const useGenerateReport = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ title, reportType }: { title: string; reportType: string }) =>
      reportsApi.generateReport(title, reportType),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reports"] });
    },
  });
};

export const useTopTravellers = () => {
  return useQuery({
    queryKey: ["reports", "top-travellers"],
    queryFn: reportsApi.getTopTravellers,
    // Bug #9: refresh the analytics metrics in real time so the dashboard is never stale.
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
    staleTime: 0,
  });
};

// ==========================================
// NOTIFICATIONS
// ==========================================
export const useNotifications = () => {
  return useQuery({
    queryKey: ["notifications"],
    queryFn: notificationsApi.getNotifications,
    refetchInterval: 15000, // Auto-refetch every 15s to catch new alerts
  });
};

export const useMarkNotificationRead = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationsApi.markNotificationRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
};

export const useDismissNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationsApi.dismissNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
};

// ==========================================
// ADMIN HOOKS
// ==========================================
export const usePendingUsers = (enabled: boolean = true) => {
  return useQuery({
    queryKey: ["admin", "pending-users"],
    queryFn: adminApi.getPendingUsers,
    enabled,
  });
};

export const useApproveUser = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: adminApi.approveUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "pending-users"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });
};

export const useRejectUser = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: adminApi.rejectUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "pending-users"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });
};

export const useGrades = () => {
  return useQuery({
    queryKey: ["admin", "grades"],
    queryFn: adminApi.getGrades,
  });
};

export const useCityTiers = () => {
  return useQuery({
    queryKey: ["admin", "city-tiers"],
    queryFn: adminApi.getCityTiers,
  });
};

export const usePolicies = () => {
  return useQuery({
    queryKey: ["admin", "policies"],
    queryFn: adminApi.getTravelPolicies,
  });
};

export * from "./useAnalytics";
export * from "./useTravelDesk";
