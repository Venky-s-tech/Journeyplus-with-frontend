import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  useAdvances,
  useAdvanceSummary,
  useTrip,
  useDisburseAdvance,
  useForfeitAdvance,
  useSettleAdvance,
} from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";
import { Check, Eye, Coins } from "lucide-react";

export const AdvancesQueue: React.FC = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [selectedAdvanceId, setSelectedAdvanceId] = useState<number | null>(null);
  const [isSettleOpen, setIsSettleOpen] = useState(false);
  const [viewAdvanceId, setViewAdvanceId] = useState<number | null>(null);

  // Settlement Form state
  const [amountUtilised, setAmountUtilised] = useState(0);
  const [amountReturned, setAmountReturned] = useState(0);
  const [remarks, setRemarks] = useState("");

  const { data: advances, isLoading } = useAdvances("FINANCE");
  const disburseMutation = useDisburseAdvance();
  const forfeitMutation = useForfeitAdvance();
  const settleMutation = useSettleAdvance();

  // FIX: this page previously read `a.tripRequest?.employee?.name` and
  // `a.settlements` directly off the plain advance list - neither field
  // exists on the real AdvanceResponse (no nested trip object, no
  // settlements array), so the employee name and settlement history
  // silently never rendered. The real settlement history + running totals
  // only exist via GET /api/advances/{id}/summary, and trip/employee
  // context only exists via a separate trip lookup - both wired in below
  // for the View modal instead of guessing at fields that were never there.
  const { data: summary, isLoading: isSummaryLoading } = useAdvanceSummary(viewAdvanceId || 0);
  const { data: summaryTrip } = useTrip(summary?.advanceDetails?.tripRequestId || 0);

  const handleDisburse = (id: number) => {
    disburseMutation.mutate(id, {
      onSuccess: () => {
        toast("Travel Advance Cash Disbursed", "success", "Success");
      },
      onError: (err: any) => {
        toast(getErrorMessage(err, "Disbursement failed"), "error");
      },
    });
  };

  const handleForfeit = (id: number) => {
    forfeitMutation.mutate(id, {
      onSuccess: () => {
        toast("Travel Advance Forfeited", "success", "Forfeited");
      },
      onError: (err: any) => {
        toast(getErrorMessage(err, "Forfeit failed"), "error");
      },
    });
  };

  const handleSettle = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAdvanceId) return;

    settleMutation.mutate(
      {
        id: selectedAdvanceId,
        data: {
          amountUtilised: Number(amountUtilised),
          amountReturned: Number(amountReturned),
          remarks,
        },
      },
      {
        onSuccess: () => {
          toast("Travel Advance settled and closed", "success", "Settled");
          setSelectedAdvanceId(null);
          setAmountUtilised(0);
          setAmountReturned(0);
          setRemarks("");
          setIsSettleOpen(false);
        },
        onError: (err: any) => {
          const msg =
            getErrorMessage(err, "Sum of Utilised and Returned amounts cannot exceed the Advance Amount when utilised is less than or equal to advance");
          toast(msg, "error", "Settlement Failed");
        },
      }
    );
  };

  const columns = [
    {
      header: "ID",
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
      header: "Status",
      accessor: (a: any) => <StatusBadge status={a.status} />,
    },
    {
      header: "Actions",
      accessor: (a: any) => (
        <div className="flex flex-col gap-2 items-end">
          <div className="flex gap-2">
            <Button size="sm" variant="outline" className="gap-1 h-8" onClick={() => setViewAdvanceId(a.id)}>
              <Eye className="h-3.5 w-3.5" /> View
            </Button>
            {a.status === "APPROVED" && (
              <Button size="sm" onClick={() => handleDisburse(a.id)} className="bg-purple-600 hover:bg-purple-700 h-8">
                Disburse Cash
              </Button>
            )}
            {a.status === "DISBURSED" && (
              <>
                <Button
                  size="sm"
                  onClick={() => {
                    setSelectedAdvanceId(a.id);
                    setAmountUtilised(a.requestedAmount);
                    setAmountReturned(0);
                    setIsSettleOpen(true);
                  }}
                  className="bg-emerald-600 hover:bg-emerald-700 h-8"
                >
                  Settle Account
                </Button>
                <Button size="sm" variant="destructive" onClick={() => handleForfeit(a.id)} className="h-8">
                  Forfeit
                </Button>
              </>
            )}
          </div>
        </div>
      ),
      align: "right" as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Advances Disbursements Queue</h1>
        <p className="text-xs text-muted-foreground">
          Finance approvals workspace for dispersing approved advances and settling active cash accounts.
        </p>
      </div>

      <DataTable
        columns={columns}
        data={advances}
        isLoading={isLoading}
        emptyMessage="No advances pending disbursement or settlement."
      />

      {/* View modal: real settlement history + trip context, backed by the
          actual /summary endpoint rather than fields that never existed on
          the plain advance list. */}
      <Dialog open={!!viewAdvanceId} onOpenChange={(open) => !open && setViewAdvanceId(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Coins className="h-4 w-4 text-muted-foreground" /> Advance Details
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
              <div className="flex justify-between">
                <span className="text-muted-foreground">Status:</span>
                <StatusBadge status={summary.currentStatus} />
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

              {summaryTrip && (
                <div className="pt-2 border-t mt-2 space-y-1">
                  <span className="font-semibold block mb-1">Trip Context</span>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Employee:</span>
                    <span className="font-medium">{summaryTrip.employee?.username || "—"}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Destination:</span>
                    <span className="font-medium">{summaryTrip.destination}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Travel Dates:</span>
                    <span className="font-medium">{formatDate(summaryTrip.departureDate)} - {formatDate(summaryTrip.returnDate)}</span>
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

      {/* Settle Dialog */}
      <Dialog
        open={isSettleOpen && !!selectedAdvanceId}
        onOpenChange={(op) => {
          setIsSettleOpen(op);
          if (!op) setSelectedAdvanceId(null);
        }}
      >
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Settle Travel Advance</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSettle} className="space-y-3 text-xs">
            <div className="space-y-1">
              <Label>Amount Utilised (USD)</Label>
              <Input
                type="number"
                required
                value={amountUtilised}
                onChange={(e) => setAmountUtilised(Number(e.target.value))}
              />
            </div>

            <div className="space-y-1">
              <Label>Amount Returned (USD)</Label>
              <Input
                type="number"
                required
                value={amountReturned}
                onChange={(e) => setAmountReturned(Number(e.target.value))}
              />
            </div>

            <div className="space-y-1">
              <Label>Remarks / Comments</Label>
              <textarea
                className="flex min-h-[60px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm text-foreground"
                placeholder="Spent on meals, returned rest."
                required
                value={remarks}
                onChange={(e) => setRemarks(e.target.value)}
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setIsSettleOpen(false);
                  setSelectedAdvanceId(null);
                }}
              >
                Cancel
              </Button>
              <Button type="submit">Submit Settlement</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdvancesQueue;
