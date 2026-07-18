import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useExceptions, useResolveException } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";
import { Eye } from "lucide-react";

export const Exceptions: React.FC = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [selectedExcId, setSelectedExcId] = useState<number | null>(null);
  // FIX: the backend's /resolve endpoint only accepts the literal strings
  // "APPROVE" or "REJECT" (case-insensitive) - it throws
  // IllegalArgumentException("Invalid action") for anything else, so the
  // previous values here ("Approved_With_Justification"/"Rejected_Violation")
  // meant every single resolution attempt failed.
  const [actionInput, setActionInput] = useState("APPROVE");
  const [justification, setJustification] = useState("");

  // FIX: request the server-side filtered list (the backend already
  // supports ?status=PENDING via findByApprovalStatus) instead of fetching
  // everything and re-filtering client-side on a field name (`status`) that
  // doesn't exist on the real entity (`approvalStatus`) - that mismatch
  // meant this queue always rendered empty regardless of how many
  // exceptions were actually pending.
  const { data: pendingExceptions, isLoading } = useExceptions("PENDING");
  const resolveMutation = useResolveException();

  const selectedException = pendingExceptions?.find((e: any) => e.id === selectedExcId);

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
          setIsDialogOpen(false);
        },
        onError: (err: any) => {
          toast(getErrorMessage(err, "Failed to resolve exception"), "error");
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
      header: "Amount Exceeded",
      accessor: (e: any) => <span className="font-semibold text-primary">{formatCurrency(e.amountExceeded)}</span>,
    },
    {
      header: "Linked Line Item",
      accessor: (e: any) => (
        <span className="text-xs text-muted-foreground">
          {e.expenseLine?.category || "Expense Receipt"} {e.claimId != null && <>(Claim #{e.claimId})</>}
        </span>
      ),
    },
    {
      header: "Policy Details",
      accessor: (e: any) =>
        e.policy ? (
          <div className="text-[11px] text-muted-foreground">
            <div className="font-medium text-foreground">{e.policy.policyName}</div>
            <div>Per Diem: {formatCurrency(e.policy.perDiemRate)} | Conveyance Limit: {formatCurrency(e.policy.localConveyanceLimit)}</div>
            <div>Max/Trip: {formatCurrency(e.policy.maxAmountPerTrip)}</div>
          </div>
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        ),
    },
    {
      header: "Status",
      accessor: (e: any) => <StatusBadge status={e.approvalStatus || "PENDING"} />,
    },
    {
      header: "Action",
      accessor: (e: any) => (
        <div className="flex gap-2 justify-end">
          {e.claimId != null && (
            <Button size="sm" variant="outline" className="gap-1" onClick={() => navigate(`/expenses/${e.claimId}`)}>
              <Eye className="h-3.5 w-3.5" /> View Claim
            </Button>
          )}
          <Button
            size="sm"
            onClick={() => {
              setSelectedExcId(e.id);
              setActionInput("APPROVE");
              setIsDialogOpen(true);
            }}
          >
            Resolve
          </Button>
        </div>
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

          {selectedException && (
            <div className="text-xs space-y-1 pb-2 mb-2 border-b">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Violation:</span>
                <span className="font-semibold text-destructive">{selectedException.violationType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Amount Exceeded:</span>
                <span className="font-semibold">{formatCurrency(selectedException.amountExceeded)}</span>
              </div>
              {selectedException.policy && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Policy Limit:</span>
                  <span className="font-medium">{selectedException.policy.policyName}</span>
                </div>
              )}
              {selectedException.claimId != null && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="w-full h-7 text-xs mt-1"
                  onClick={() => navigate(`/expenses/${selectedException.claimId}`)}
                >
                  View Full Claim & Trip Details
                </Button>
              )}
            </div>
          )}

          <form onSubmit={handleResolve} className="space-y-3 text-xs">
            <div className="space-y-1">
              <Label>Resolution Action</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                value={actionInput}
                onChange={(e) => setActionInput(e.target.value)}
              >
                <option value="APPROVE">Approve with Justification</option>
                <option value="REJECT">Reject Violation & Deduct</option>
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
