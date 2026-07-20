import React, { useState } from "react";
import { useAuth } from "../../lib/auth-context";
import { useAdvances, useAdvanceSummary, useRequestAdvance, useTrips } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { Coins, Plus, AlertCircle, Eye } from "lucide-react";
import { TravelAdvance } from "../../types";

// Statuses that mean "an advance was approved for this trip at some point"
// (DISBURSED/SETTLED both necessarily passed through APPROVED first).
// Matches the real backend enum: REQUESTED, APPROVED, DISBURSED, SETTLED,
// FORFEITED - there is no REJECTED/PENDING_APPROVAL value for advances.
const APPROVED_OR_BEYOND: string[] = ["APPROVED", "DISBURSED", "SETTLED"];

export const Advances: React.FC = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const [isOpen, setIsOpen] = useState(false);
  const [viewAdvanceId, setViewAdvanceId] = useState<number | null>(null);

  // Form states
  const [selectedTripId, setSelectedTripId] = useState("");
  const [amount, setAmount] = useState(100);
  const [purpose, setPurpose] = useState("");

  const { data: advances, isLoading } = useAdvances(user?.role || "EMPLOYEE");
  const { data: trips } = useTrips("EMPLOYEE");
  const requestMutation = useRequestAdvance();

  // Detail fetch for the "View" modal: the plain GET /api/advances/{id}
  // response does NOT include settlement history or running totals - only
  // GET /api/advances/{id}/summary does, so the modal uses that endpoint.
  const { data: summary, isLoading: isViewLoading } = useAdvanceSummary(viewAdvanceId || 0);

  // Item 2 (validation): a trip may only have ONE approved advance. Once an
  // advance for a trip has reached APPROVED (or progressed further, to
  // DISBURSED/SETTLED), that trip is no longer eligible for a new request.
  const tripIdsWithApprovedAdvance = new Set(
    (advances || [])
      .filter((a) => APPROVED_OR_BEYOND.includes(a.status))
      .map((a) => a.tripRequestId)
  );

  // All APPROVED trips are shown in the selector, but ones that already have
  // an approved advance are rendered disabled with an explanatory message,
  // rather than silently removed - so the employee understands why.
  const approvedTrips = (trips?.filter((t) => t.status === "APPROVED") || []).map((t) => ({
    ...t,
    hasApprovedAdvance: tripIdsWithApprovedAdvance.has(t.id),
  }));

  const allApprovedTripsBlocked =
    approvedTrips.length > 0 && approvedTrips.every((t) => t.hasApprovedAdvance);

  const handleRequest = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTripId) {
      toast("Please select an approved trip", "error");
      return;
    }

    // Defense-in-depth: block submission client-side even if the disabled
    // <option> was somehow bypassed.
    if (tripIdsWithApprovedAdvance.has(Number(selectedTripId))) {
      toast("An advance has already been approved for this trip.", "error", "Not Allowed");
      return;
    }

    requestMutation.mutate(
      {
        tripRequestId: Number(selectedTripId),
        requestedAmount: Number(amount),
        currency: "USD",
        purposeDetails: purpose,
        usdEquivalent: Number(amount),
      },
      {
        onSuccess: () => {
          toast("Travel Advance requested successfully", "success", "Requested");
          setIsOpen(false);
          setSelectedTripId("");
          setPurpose("");
          setAmount(100);
        },
        onError: (err: any) => {
          const msg = getErrorMessage(err, "Failed to request advance");
          toast(msg, "error", "Request Failed");
        },
      }
    );
  };

  // Item 2 (data display): the AdvanceResponse has NO approver info at all
  // (only an approvedById user id, never a username) and NO nested trip
  // object - so the only way to show the approver's username is to resolve
  // it via the linked TripRequest (trip.approver.username), matched by
  // tripRequestId against the trips list already loaded above.
  const getApproverUsername = (a: TravelAdvance) => {
    const trip = trips?.find((t) => t.id === a.tripRequestId);
    return trip?.approver?.username || "—";
  };

  const columns = [
    {
      header: "Advance ID",
      accessor: (a: any) => <span className="font-semibold">#{a.id}</span>,
    },
    {
      header: "Trip ID",
      accessor: (a: any) => <span className="font-medium">#{a.tripRequestId}</span>,
    },
    {
      header: "Amount",
      accessor: (a: any) => <span className="font-semibold text-primary">{formatCurrency(a.requestedAmount, a.currency)}</span>,
    },
    {
      header: "Purpose",
      accessor: (a: any) => <span className="text-xs text-muted-foreground">{a.purposeDetails}</span>,
    },
    {
      header: "Approver User Name",
      accessor: (a: any) => <span className="text-xs">{getApproverUsername(a)}</span>,
    },
    {
      header: "Date Disbursed",
      accessor: (a: any) => (
        <span className="text-xs">{a.disbursementDate ? formatDate(a.disbursementDate) : "Not Disbursed"}</span>
      ),
    },
    {
      header: "Status",
      accessor: (a: any) => <StatusBadge status={a.status} />,
    },
    {
      header: "Action",
      accessor: (a: any) => (
        <Button size="sm" variant="outline" className="gap-1" onClick={() => setViewAdvanceId(a.id)}>
          <Eye className="h-3.5 w-3.5" /> View
        </Button>
      ),
      align: "right" as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Travel Advances</h1>
          <p className="text-xs text-muted-foreground">
            Track and request cash advances for approved corporate travels.
          </p>
        </div>

        {user?.role === "EMPLOYEE" && (
          <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogTrigger asChild>
              <Button className="gap-2" disabled={allApprovedTripsBlocked} title={allApprovedTripsBlocked ? "An advance has already been approved for this trip." : undefined}>
                <Plus className="h-4 w-4" /> Request Advance
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-md">
              <DialogHeader>
                <DialogTitle>Request Cash Advance</DialogTitle>
              </DialogHeader>

              {approvedTrips.length === 0 ? (
                <div className="p-4 border rounded-md bg-yellow-50 dark:bg-yellow-950/20 border-yellow-250 flex gap-2 items-start text-xs text-yellow-700 dark:text-yellow-400">
                  <AlertCircle className="h-5 w-5 shrink-0" />
                  <div>
                    <span className="font-semibold block mb-0.5">No Approved Trips Found</span>
                    Advances can only be requested for active trip requests that have been APPROVED by your manager.
                  </div>
                </div>
              ) : (
                <form onSubmit={handleRequest} className="space-y-4 text-xs">
                  <div className="space-y-1">
                    <Label htmlFor="trip">Select Approved Trip</Label>
                    <select
                      id="trip"
                      required
                      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                      value={selectedTripId}
                      onChange={(e) => setSelectedTripId(e.target.value)}
                    >
                      <option value="">Select a trip</option>
                      {approvedTrips.map((t) => (
                        <option
                          key={t.id}
                          value={t.id}
                          disabled={t.hasApprovedAdvance}
                          title={t.hasApprovedAdvance ? "An advance has already been approved for this trip." : undefined}
                        >
                          #{t.id} - {t.destination} ({formatDate(t.departureDate)})
                          {t.hasApprovedAdvance ? " — advance already approved" : ""}
                        </option>
                      ))}
                    </select>
                    {selectedTripId && tripIdsWithApprovedAdvance.has(Number(selectedTripId)) && (
                      <p className="text-[10px] text-destructive flex items-center gap-1 pt-1">
                        <AlertCircle className="h-3 w-3" /> An advance has already been approved for this trip.
                      </p>
                    )}
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="amount">Requested Amount (USD)</Label>
                    <Input
                      id="amount"
                      type="number"
                      required
                      min={1}
                      value={amount}
                      onChange={(e) => setAmount(Number(e.target.value))}
                    />
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="purpose">Purpose & Spend details</Label>
                    <textarea
                      id="purpose"
                      required
                      className="flex min-h-[60px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm text-foreground"
                      placeholder="e.g. Local taxi transfers and daily meals for client meetings"
                      value={purpose}
                      onChange={(e) => setPurpose(e.target.value)}
                    />
                  </div>

                  <div className="flex justify-end gap-2 pt-2">
                    <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>
                      Cancel
                    </Button>
                    <Button
                      type="submit"
                      disabled={requestMutation.isPending || (!!selectedTripId && tripIdsWithApprovedAdvance.has(Number(selectedTripId)))}
                    >
                      {requestMutation.isPending ? "Requesting..." : "Submit Request"}
                    </Button>
                  </div>
                </form>
              )}
            </DialogContent>
          </Dialog>
        )}
      </div>

      <DataTable
        columns={columns}
        data={advances}
        isLoading={isLoading}
        emptyMessage="No travel advances found."
      />

      {/* Item 2 (view action): full advance detail modal, backed by the real
          /summary endpoint (settlement history + running totals) */}
      <Dialog open={!!viewAdvanceId} onOpenChange={(open) => !open && setViewAdvanceId(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Coins className="h-4 w-4 text-muted-foreground" /> Advance Details
            </DialogTitle>
          </DialogHeader>

          {isViewLoading ? (
            <div className="flex h-32 items-center justify-center">
              <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
            </div>
          ) : summary?.advanceDetails ? (
            <div className="space-y-3 text-xs">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Advance ID:</span>
                <span className="font-semibold">#{summary.advanceDetails.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Trip Request:</span>
                <span className="font-medium">#{summary.advanceDetails.tripRequestId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Requested Amount:</span>
                <span className="font-semibold text-primary">{formatCurrency(summary.advanceDetails.requestedAmount, summary.advanceDetails.currency)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Approver User Name:</span>
                <span className="font-medium">{getApproverUsername(summary.advanceDetails)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Purpose:</span>
                <span className="font-medium text-right max-w-[60%]">{summary.advanceDetails.purposeDetails}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Status:</span>
                <StatusBadge status={summary.currentStatus} />
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Date Disbursed:</span>
                <span className="font-medium">{summary.advanceDetails.disbursementDate ? formatDate(summary.advanceDetails.disbursementDate) : "Not Disbursed"}</span>
              </div>

              <div className="pt-2 border-t mt-2 space-y-1">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Total Utilised:</span>
                  <span className="font-medium">{formatCurrency(summary.totalUtilisedAmount, summary.advanceDetails.currency)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Total Returned:</span>
                  <span className="font-medium">{formatCurrency(summary.totalReturnedAmount, summary.advanceDetails.currency)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Outstanding:</span>
                  <span className="font-semibold">{formatCurrency(summary.outstandingAmount, summary.advanceDetails.currency)}</span>
                </div>
              </div>

              {summary.settlementDetails && summary.settlementDetails.length > 0 && (
                <div className="pt-2 border-t mt-2">
                  <span className="font-semibold block mb-1">Settlement History</span>
                  <div className="space-y-1">
                    {summary.settlementDetails.map((s: any) => (
                      <div key={s.id} className="p-2 border rounded bg-muted/20 text-[10px] text-muted-foreground">
                        Utilised: {formatCurrency(s.amountUtilised)} | Returned: {formatCurrency(s.amountReturned)}
                        {s.settlementDate ? ` on ${formatDate(s.settlementDate)}` : ""}
                        {s.remarks ? ` — ${s.remarks}` : ""}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex justify-end pt-2">
                <Button variant="outline" onClick={() => setViewAdvanceId(null)}>
                  Close
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

export default Advances;
