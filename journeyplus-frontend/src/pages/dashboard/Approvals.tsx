import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  useTrips,
  useAdvances,
  useAdvanceSummary,
  useTrip,
  useClaims,
  useApproveTrip,
  useRejectTrip,
  useApproveAdvance,
  useApproveClaim,
  useRejectClaim,
} from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "../../components/ui/tabs";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";
import { Check, X, Eye, Coins } from "lucide-react";

export const Approvals: React.FC = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [commentsMap, setCommentsMap] = useState<Record<string, string>>({});
  const [viewAdvanceId, setViewAdvanceId] = useState<number | null>(null);

  // Query pending datasets - these hooks already call the role-scoped
  // "pending approvals" endpoints server-side, so no client re-filtering is
  // needed (and for advances, re-filtering was actively wrong - see below).
  const { data: trips, isLoading: tripsLoading } = useTrips("APPROVING_MANAGER");
  const { data: advances, isLoading: advLoading } = useAdvances("APPROVING_MANAGER");
  const { data: claims, isLoading: claimsLoading } = useClaims("APPROVING_MANAGER");

  // "View" modal for an advance: pulls the real /summary endpoint (full
  // settlement history + running totals) plus the linked trip's own details
  // (destination, dates, purpose) so the manager isn't approving blind.
  const { data: summary, isLoading: isSummaryLoading } = useAdvanceSummary(viewAdvanceId || 0);
  const { data: summaryTrip } = useTrip(summary?.advanceDetails?.tripRequestId || 0);

  const pendingTrips = trips?.filter((t) => t.status === "SUBMITTED") || [];
  // FIX: previously filtered on a.status === "PENDING_APPROVAL", but that
  // status value doesn't exist anywhere in the real backend AdvanceStatus
  // enum (REQUESTED/APPROVED/DISBURSED/SETTLED/FORFEITED) - every advance
  // failed this check, so the Advances tab always showed empty regardless
  // of how many were actually awaiting approval. useAdvances("APPROVING_MANAGER")
  // already calls the server's pending-approvals endpoint, so just use it directly.
  const pendingAdvances = advances || [];
  const pendingClaims = claims?.filter((c) => c.status === "SUBMITTED") || [];

  // Mutations
  const approveTripMut = useApproveTrip();
  const rejectTripMut = useRejectTrip();
  const approveAdvMut = useApproveAdvance();
  const approveClaimMut = useApproveClaim();
  const rejectClaimMut = useRejectClaim();

  const handleCommentChange = (key: string, val: string) => {
    setCommentsMap((prev) => ({ ...prev, [key]: val }));
  };

  const handleTripAction = (id: number, approve: boolean) => {
    const comment = commentsMap[`trip-${id}`] || "";
    const mutation = approve ? approveTripMut : rejectTripMut;
    mutation.mutate(
      { id, comments: comment },
      {
        onSuccess: () => {
          toast(approve ? "Trip Request Approved" : "Trip Request Rejected", "success", "Success");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Action failed", "error");
        },
      }
    );
  };

  const handleAdvanceApproval = (id: number) => {
    approveAdvMut.mutate(id, {
      onSuccess: () => {
        toast("Travel Advance Approved", "success", "Success");
        if (viewAdvanceId === id) setViewAdvanceId(null);
      },
      onError: (err: any) => {
        toast(err.response?.data?.message || "Approval failed", "error");
      },
    });
  };

  const handleClaimAction = (id: number, approve: boolean) => {
    const comment = commentsMap[`claim-${id}`] || "";
    const mutation = approve ? approveClaimMut : rejectClaimMut;
    mutation.mutate(
      { id, comments: comment },
      {
        onSuccess: () => {
          toast(approve ? "Expense Claim Approved" : "Expense Claim Rejected", "success", "Success");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Action failed", "error");
        },
      }
    );
  };

  const tripColumns = [
    {
      header: "Employee",
      // FIX: SimpleUser (what the backend actually returns for `employee`)
      // has no `name` field, only username/email/role/id.
      accessor: (t: any) => <span>{t.employee?.username || "—"}</span>,
    },
    {
      header: "Destination",
      accessor: (t: any) => <span>{t.destination}</span>,
    },
    {
      header: "Dates",
      accessor: (t: any) => (
        <span className="text-xs">
          {formatDate(t.departureDate)} - {formatDate(t.returnDate)}
        </span>
      ),
    },
    {
      header: "Cost",
      accessor: (t: any) => <span>{formatCurrency(t.estimatedCost)}</span>,
    },
    {
      header: "Details",
      accessor: (t: any) => (
        // Full itinerary, visa requirements, and policy-relevant metadata
        // live on the trip detail page, which this role already has access
        // to (RoleGuard on /trips/:id includes APPROVING_MANAGER) - link
        // there instead of asking the manager to approve from a bare row.
        <Button size="sm" variant="outline" className="gap-1" onClick={() => navigate(`/trips/${t.id}`)}>
          <Eye className="h-3.5 w-3.5" /> View
        </Button>
      ),
    },
    {
      header: "Comments / Action",
      accessor: (t: any) => (
        <div className="flex items-center gap-2 max-w-sm">
          <Input
            placeholder="Review comments..."
            value={commentsMap[`trip-${t.id}`] || ""}
            onChange={(e) => handleCommentChange(`trip-${t.id}`, e.target.value)}
            className="h-8 text-xs flex-1"
          />
          <Button
            size="sm"
            onClick={() => handleTripAction(t.id, true)}
            className="h-8 bg-green-600 hover:bg-green-700 p-2"
          >
            <Check className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="destructive"
            onClick={() => handleTripAction(t.id, false)}
            className="h-8 p-2"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  const advanceColumns = [
    {
      header: "Trip ID",
      accessor: (a: any) => <span className="font-semibold">#{a.tripRequestId}</span>,
    },
    {
      header: "Requested Amount",
      accessor: (a: any) => <span className="font-semibold">{formatCurrency(a.requestedAmount, a.currency)}</span>,
    },
    {
      header: "Purpose Description",
      accessor: (a: any) => <span className="text-xs text-muted-foreground">{a.purposeDetails}</span>,
    },
    {
      header: "Action",
      accessor: (a: any) => (
        <div className="flex gap-2 justify-end">
          {/* Full trip context (destination, dates, purpose, estimated
              cost) plus settlement history via the real /summary endpoint -
              so the manager can see what this advance is actually for
              before approving cash out. */}
          <Button size="sm" variant="outline" className="gap-1" onClick={() => setViewAdvanceId(a.id)}>
            <Eye className="h-3.5 w-3.5" /> View
          </Button>
          <Button
            size="sm"
            onClick={() => handleAdvanceApproval(a.id)}
            className="bg-green-600 hover:bg-green-700 gap-1"
            disabled={approveAdvMut.isPending}
          >
            <Check className="h-3.5 w-3.5" /> Approve
          </Button>
        </div>
      ),
      align: "right" as const,
    },
  ];

  const claimColumns = [
    {
      header: "Title",
      accessor: (c: any) => <span className="font-semibold">{c.claimTitle}</span>,
    },
    {
      header: "Claim Amount",
      accessor: (c: any) => <span className="font-semibold">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>,
    },
    {
      header: "Date Filed",
      accessor: (c: any) => <span className="text-xs">{formatDate(c.submittedDate)}</span>,
    },
    {
      header: "Details",
      accessor: (c: any) => (
        // Full expense line breakdown, receipts, trip context, and the
        // advance/net-reimbursement calculation all live on this page now -
        // "Lines: N" alone gave no basis to actually judge the claim.
        <Button size="sm" variant="outline" className="gap-1" onClick={() => navigate(`/expenses/${c.id}`)}>
          <Eye className="h-3.5 w-3.5" /> View
        </Button>
      ),
    },
    {
      header: "Review comments / Decision",
      accessor: (c: any) => (
        <div className="flex items-center gap-2 max-w-sm">
          <Input
            placeholder="Review comments..."
            value={commentsMap[`claim-${c.id}`] || ""}
            onChange={(e) => handleCommentChange(`claim-${c.id}`, e.target.value)}
            className="h-8 text-xs flex-1"
          />
          <Button
            size="sm"
            onClick={() => handleClaimAction(c.id, true)}
            className="h-8 bg-green-600 hover:bg-green-700 p-2"
          >
            <Check className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="destructive"
            onClick={() => handleClaimAction(c.id, false)}
            className="h-8 p-2"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Approvals Inbox</h1>
        <p className="text-xs text-muted-foreground">
          Review and approve pending trip requests, cash advances, and employee expense claims.
        </p>
      </div>

      <Tabs defaultValue="trips">
        <TabsList className="mb-4">
          <TabsTrigger value="trips">Trips ({pendingTrips.length})</TabsTrigger>
          <TabsTrigger value="advances">Advances ({pendingAdvances.length})</TabsTrigger>
          <TabsTrigger value="claims">Expense Claims ({pendingClaims.length})</TabsTrigger>
        </TabsList>

        <TabsContent value="trips">
          <DataTable
            columns={tripColumns}
            data={pendingTrips}
            isLoading={tripsLoading}
            emptyMessage="No pending trip requests awaiting approval."
          />
        </TabsContent>

        <TabsContent value="advances">
          <DataTable
            columns={advanceColumns}
            data={pendingAdvances}
            isLoading={advLoading}
            emptyMessage="No pending travel advance cash requests."
          />
        </TabsContent>

        <TabsContent value="claims">
          <DataTable
            columns={claimColumns}
            data={pendingClaims}
            isLoading={claimsLoading}
            emptyMessage="No pending expense claims awaiting approval."
          />
        </TabsContent>
      </Tabs>

      {/* Advance View modal: real settlement/summary data + linked trip context */}
      <Dialog open={!!viewAdvanceId} onOpenChange={(open) => !open && setViewAdvanceId(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Coins className="h-4 w-4 text-muted-foreground" /> Advance Request Details
            </DialogTitle>
          </DialogHeader>

          {isSummaryLoading ? (
            <div className="flex h-32 items-center justify-center">
              <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
            </div>
          ) : summary?.advanceDetails ? (
            <div className="space-y-3 text-xs">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Requested Amount:</span>
                <span className="font-semibold text-primary">{formatCurrency(summary.advanceDetails.requestedAmount, summary.advanceDetails.currency)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Purpose:</span>
                <span className="font-medium text-right max-w-[60%]">{summary.advanceDetails.purposeDetails}</span>
              </div>

              {summaryTrip && (
                <div className="pt-2 border-t mt-2 space-y-1">
                  <span className="font-semibold block mb-1">Trip Context</span>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Destination:</span>
                    <span className="font-medium">{summaryTrip.destination}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Purpose:</span>
                    <span className="font-medium text-right max-w-[60%]">{summaryTrip.purpose}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Travel Dates:</span>
                    <span className="font-medium">{formatDate(summaryTrip.departureDate)} - {formatDate(summaryTrip.returnDate)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Estimated Trip Cost:</span>
                    <span className="font-medium">{formatCurrency(summaryTrip.estimatedCost)}</span>
                  </div>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="w-full h-7 text-xs mt-1"
                    onClick={() => navigate(`/trips/${summaryTrip.id}`)}
                  >
                    Open Full Trip Record
                  </Button>
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <Button variant="outline" onClick={() => setViewAdvanceId(null)}>
                  Close
                </Button>
                <Button
                  className="bg-green-600 hover:bg-green-700 gap-1"
                  disabled={approveAdvMut.isPending}
                  onClick={() => handleAdvanceApproval(summary.advanceDetails.id)}
                >
                  <Check className="h-3.5 w-3.5" /> Approve
                </Button>
              </div>
            </div>
          ) : (
            <p className="text-xs text-muted-foreground py-4 text-center">Unable to load advance details.</p>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Approvals;
