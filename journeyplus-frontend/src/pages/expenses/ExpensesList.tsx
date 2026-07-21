import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useClaims, useCreateClaim, useTrips } from "../../hooks";
import { useAuth } from "../../lib/auth-context";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { FileSpreadsheet, Plus, AlertCircle } from "lucide-react";

export const ExpensesList: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  const [isOpen, setIsOpen] = useState(false);

  // Form state
  const [selectedTripId, setSelectedTripId] = useState("");
  const [title, setTitle] = useState("");
  const [approverUsername, setApproverUsername] = useState("");

  const { data: claims, isLoading } = useClaims(user?.role || "EMPLOYEE");
  const { data: trips } = useTrips("EMPLOYEE");
  const createMutation = useCreateClaim();

  // Item 3: the real backend field names (computed server-side in
  // ExpenseService#calculateAdvanceAndNetReimbursable) are `advanceAdjusted`
  // and `netReimbursable` - not advanceClaimed/netReimbursed.
  const getAdvanceClaimed = (c: any) => c.advanceAdjusted ?? 0;
  const getNetReimbursed = (c: any) => c.netReimbursable ?? c.totalAmount;
  // ExpenseClaim.approverUsername is now a real, properly-exposed backend
  // field (see ExpenseClaim#getApproverUsername) - fall back to the linked
  // trip's approver only for claims created before that fix existed.
  const getApproverUsername = (c: any) => {
    if (c.approverUsername) return c.approverUsername;
    const trip = trips?.find((t) => t.id === c.tripRequestId);
    return trip?.approver?.username || "—";
  };

  // Filter completed trips
  const completedTrips = trips?.filter((t) => t.status === "COMPLETED") || [];

  // "if expense got approved not create again for that particular trip":
  // block re-creating a claim once one for this trip has reached APPROVED
  // (or beyond - PAID/PARTIALLY_PAID, which necessarily passed through
  // APPROVED). The backend has its own separate, stricter rule (blocks a
  // second claim once an existing one has any expense lines, regardless of
  // status) - but that can't be reliably replicated here since the claims
  // list endpoint never includes line data (same limitation as
  // expenseLines elsewhere), so trying to guess it client-side ends up
  // over-blocking legitimate creation for lineless DRAFT claims. That rarer
  // edge case surfaces via the actual backend error message instead (see
  // getErrorMessage / handleCreate's onError below).
  const CLAIM_APPROVED_OR_BEYOND = ["APPROVED", "PAID", "PARTIALLY_PAID"];
  const tripIdsWithExistingClaim = new Set(
    (claims || [])
      .filter((c: any) => CLAIM_APPROVED_OR_BEYOND.includes(c.status))
      .map((c: any) => c.tripRequestId)
  );

  const selectableTrips = completedTrips.map((t) => ({
    ...t,
    hasApprovedClaim: tripIdsWithExistingClaim.has(t.id),
  }));

  // Auto-fill the approver from the selected trip (it's already known - the
  // employee shouldn't have to look it up), but leave it editable in case a
  // different approver genuinely needs to sign off this specific claim.
  const handleTripSelect = (tripId: string) => {
    setSelectedTripId(tripId);
    const trip = trips?.find((t) => String(t.id) === tripId);
    setApproverUsername(trip?.approver?.username || "");
  };

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTripId) {
      toast("Please select a completed trip", "error");
      return;
    }

    if (tripIdsWithExistingClaim.has(Number(selectedTripId))) {
      toast("An expense claim has already been approved for this trip.", "error", "Not Allowed");
      return;
    }

    createMutation.mutate(
      {
        tripId: Number(selectedTripId),
        data: {
          claimTitle: title,
          submittedDate: new Date().toISOString().split("T")[0],
          originalCurrency: "USD",
          approverUsername,
        },
      },
      {
        onSuccess: (data) => {
          toast("Expense draft claim created", "success", "Created");
          setIsOpen(false);
          setTitle("");
          setSelectedTripId("");
          setApproverUsername("");
          // Redirect to details view to add expense lines
          navigate(`/expenses/${data.id}`);
        },
        onError: (err: any) => {
          const msg = getErrorMessage(err, "Failed to create claim");
          toast(msg, "error", "Error");
        },
      }
    );
  };

  const columns = [
    {
      header: "Claim ID",
      accessor: (c: any) => <span className="font-semibold">#{c.id}</span>,
    },
    {
      header: "Trip ID",
      accessor: (c: any) => (
        <span className="font-medium">{c.tripRequestId != null ? `#${c.tripRequestId}` : "—"}</span>
      ),
    },
    {
      header: "Claim Title",
      accessor: (c: any) => <span>{c.claimTitle}</span>,
    },
    {
      header: "Date Logged",
      accessor: (c: any) => <span className="text-xs">{formatDate(c.submittedDate)}</span>,
    },
    {
      header: "Total Value",
      accessor: (c: any) => <span className="font-semibold text-primary">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>,
    },
    {
      header: "Advance Claimed",
      accessor: (c: any) => <span className="text-xs">{formatCurrency(getAdvanceClaimed(c), c.originalCurrency)}</span>,
    },
    {
      header: "Net Reimbursed",
      accessor: (c: any) => <span className="font-semibold text-emerald-600">{formatCurrency(getNetReimbursed(c), c.originalCurrency)}</span>,
    },
    {
      header: "Approver Username",
      accessor: (c: any) => <span className="text-xs">{getApproverUsername(c)}</span>,
    },
    {
      header: "Status",
      accessor: (c: any) => <StatusBadge status={c.status} />,
    },
    {
      header: "Action",
      accessor: (c: any) => (
        <Button size="sm" variant="outline" onClick={() => navigate(`/expenses/${c.id}`)}>
          Details
        </Button>
      ),
      align: "right" as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Expense Claims</h1>
          <p className="text-xs text-muted-foreground">
            Review and create expense claims for completed corporate trips.
          </p>
        </div>

        {user?.role === "EMPLOYEE" && (
          <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogTrigger asChild>
              <Button
                className="gap-2 bg-purple-600 hover:bg-purple-700"
                disabled={selectableTrips.length > 0 && selectableTrips.every((t) => t.hasApprovedClaim)}
                title={
                  selectableTrips.length > 0 && selectableTrips.every((t) => t.hasApprovedClaim)
                    ? "An expense claim has already been approved for this trip."
                    : undefined
                }
              >
                <Plus className="h-4 w-4" /> Create Expense Claim
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-md">
              <DialogHeader>
                <DialogTitle>Log Expense Claim</DialogTitle>
              </DialogHeader>

              {completedTrips.length === 0 ? (
                <div className="p-4 border rounded-md bg-yellow-50 dark:bg-yellow-950/20 border-yellow-250 flex gap-2 items-start text-xs text-yellow-700 dark:text-yellow-400">
                  <AlertCircle className="h-5 w-5 shrink-0" />
                  <div>
                    <span className="font-semibold block mb-0.5">No Completed Trips Found</span>
                    Expense claims can only be filed against trip requests that have completed travel and are marked as COMPLETED by the employee.
                  </div>
                </div>
              ) : (
                <form onSubmit={handleCreate} className="space-y-4 text-xs">
                  <div className="space-y-1">
                    <Label htmlFor="trip">Select Completed Trip</Label>
                    <select
                      id="trip"
                      required
                      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                      value={selectedTripId}
                      onChange={(e) => handleTripSelect(e.target.value)}
                    >
                      <option value="">Select completed trip</option>
                      {selectableTrips.map((t) => (
                        <option
                          key={t.id}
                          value={t.id}
                          disabled={t.hasApprovedClaim}
                          title={t.hasApprovedClaim ? "An expense claim has already been approved for this trip." : undefined}
                        >
                          #{t.id} - {t.destination} ({formatDate(t.departureDate)})
                          {t.hasApprovedClaim ? " — claim already approved" : ""}
                        </option>
                      ))}
                    </select>
                    {selectedTripId && tripIdsWithExistingClaim.has(Number(selectedTripId)) && (
                      <p className="text-[10px] text-destructive flex items-center gap-1 pt-1">
                        <AlertCircle className="h-3 w-3" /> An expense claim has already been approved for this trip.
                      </p>
                    )}
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="title">Claim Description Title</Label>
                    <Input
                      id="title"
                      required
                      placeholder="e.g. Q3 Sales Onsite Reimbursables"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                    />
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="approverUsername">Approver Username</Label>
                    <Input
                      id="approverUsername"
                      required
                      placeholder="e.g. manager username"
                      value={approverUsername}
                      onChange={(e) => setApproverUsername(e.target.value)}
                    />
                    <p className="text-[10px] text-muted-foreground">Auto-filled from the trip's approver - edit only if a different manager should approve this claim.</p>
                  </div>

                  <div className="flex justify-end gap-2 pt-2">
                    <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>
                      Cancel
                    </Button>
                    <Button
                      type="submit"
                      disabled={createMutation.isPending || (!!selectedTripId && tripIdsWithExistingClaim.has(Number(selectedTripId)))}
                    >
                      {createMutation.isPending ? "Creating..." : "Create Draft Claim"}
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
        data={claims}
        isLoading={isLoading}
        emptyMessage="No expense claims found."
      />
    </div>
  );
};

export default ExpensesList;
