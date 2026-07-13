import React, { useState } from "react";
import {
  useAdvances,
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
import { formatCurrency, formatDate } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";
import { Check, X, ShieldAlert, Award } from "lucide-react";

export const AdvancesQueue: React.FC = () => {
  const { toast } = useToast();
  const [selectedAdvanceId, setSelectedAdvanceId] = useState<number | null>(null);

  // Settlement Form state
  const [amountUtilised, setAmountUtilised] = useState(0);
  const [amountReturned, setAmountReturned] = useState(0);
  const [remarks, setRemarks] = useState("");

  const { data: advances, isLoading } = useAdvances("FINANCE");
  const disburseMutation = useDisburseAdvance();
  const forfeitMutation = useForfeitAdvance();
  const settleMutation = useSettleAdvance();

  const handleDisburse = (id: number) => {
    disburseMutation.mutate(id, {
      onSuccess: () => {
        toast("Travel Advance Cash Disbursed", "success", "Success");
      },
      onError: (err: any) => {
        toast(err.response?.data?.message || "Disbursement failed", "error");
      },
    });
  };

  const handleForfeit = (id: number) => {
    forfeitMutation.mutate(id, {
      onSuccess: () => {
        toast("Travel Advance Forfeited", "success", "Forfeited");
      },
      onError: (err: any) => {
        toast(err.response?.data?.message || "Forfeit failed", "error");
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
        },
        onError: (err: any) => {
          const msg =
            err.response?.data?.message ||
            "Sum of Utilised and Returned amounts cannot exceed the Advance Amount when utilised is less than or equal to advance";
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
      header: "Employee",
      accessor: (a: any) => <span>{a.tripRequest?.employee?.name || `Trip #${a.tripRequestId}`}</span>,
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
      header: "Disbursements / Settlement History / Actions",
      accessor: (a: any) => {
        const hasSettlements = a.settlements && a.settlements.length > 0;

        return (
          <div className="flex flex-col gap-2">
            {/* History snippet if settlements exists */}
            {hasSettlements && (
              <div className="p-2 border rounded bg-muted/20 text-[10px] text-muted-foreground mb-1">
                <span className="font-semibold block">Settlement History:</span>
                {a.settlements.map((s: any) => (
                  <div key={s.id}>
                    Utilised: {formatCurrency(s.amountUtilised)} | Returned: {formatCurrency(s.amountReturned)} ({s.remarks})
                  </div>
                ))}
              </div>
            )}

            <div className="flex gap-2">
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
        );
      },
    },
  ];

  // Open helper
  const [isSettleOpen, setIsSettleOpen] = useState(false);

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
