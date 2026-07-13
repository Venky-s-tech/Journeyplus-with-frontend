import React, { useState } from "react";
import { useExceptions, useResolveException } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";

export const Exceptions: React.FC = () => {
  const { toast } = useToast();
  const [selectedExcId, setSelectedExcId] = useState<number | null>(null);
  const [actionInput, setActionInput] = useState("Approved_With_Justification");
  const [justification, setJustification] = useState("");

  const { data: exceptions, isLoading } = useExceptions();
  const resolveMutation = useResolveException();

  // Filter only pending exceptions
  const pendingExceptions = exceptions?.filter((e) => e.status === "PENDING" || e.status === null) || [];

  const handleResolve = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedExcId) return;

    resolveMutation.mutate(
      {
        id: selectedExcId,
        action: actionInput,
        justification,
      },
      {
        onSuccess: () => {
          toast("Exception resolved successfully", "success", "Resolved");
          setSelectedExcId(null);
          setJustification("");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Failed to resolve exception", "error");
        },
      }
    );
  };

  const columns = [
    {
      header: "Exception ID",
      accessor: (e: any) => <span className="font-semibold">#{e.id}</span>,
    },
    {
      header: "Violation Type",
      accessor: (e: any) => <span className="font-semibold text-destructive">{e.violationType}</span>,
    },
    {
      header: "Flagged Amount",
      accessor: (e: any) => <span className="font-semibold text-primary">{formatCurrency(e.flaggedAmount)}</span>,
    },
    {
      header: "Linked Line item",
      accessor: (e: any) => (
        <span className="text-xs text-muted-foreground">
          {e.expenseLine?.category || "Expense Receipt"} (Claim #{e.expenseClaimId || "N/A"})
        </span>
      ),
    },
    {
      header: "Status",
      accessor: (e: any) => <StatusBadge status={e.status || "PENDING"} />,
    },
    {
      header: "Action",
      accessor: (e: any) => (
        <Button
          size="sm"
          onClick={() => {
            setSelectedExcId(e.id);
            setIsDialogOpen(true);
          }}
        >
          Resolve
        </Button>
      ),
      align: "right" as const,
    },
  ];

  const [isDialogOpen, setIsDialogOpen] = useState(false);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Compliance Exceptions Queue</h1>
        <p className="text-xs text-muted-foreground">
          Review policy breaches and audit exceptions flagged during employee claim submissions.
        </p>
      </div>

      <DataTable
        columns={columns}
        data={pendingExceptions}
        isLoading={isLoading}
        emptyMessage="No pending policy exceptions flagged."
      />

      <Dialog
        open={isDialogOpen && !!selectedExcId}
        onOpenChange={(op) => {
          setIsDialogOpen(op);
          if (!op) setSelectedExcId(null);
        }}
      >
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Resolve Exception</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleResolve} className="space-y-3 text-xs">
            <div className="space-y-1">
              <Label>Resolution Action</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                value={actionInput}
                onChange={(e) => setActionInput(e.target.value)}
              >
                <option value="Approved_With_Justification">Approve with Justification</option>
                <option value="Rejected_Violation">Reject Violation & Deduct</option>
              </select>
            </div>

            <div className="space-y-1">
              <Label>Justification / Reason</Label>
              <textarea
                required
                className="flex min-h-[60px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm text-foreground"
                placeholder="e.g. Approved by Vice President for key client onboarding."
                value={justification}
                onChange={(e) => setJustification(e.target.value)}
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setIsDialogOpen(false);
                  setSelectedExcId(null);
                }}
              >
                Cancel
              </Button>
              <Button type="submit">Confirm Resolve</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Exceptions;
