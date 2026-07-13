import React, { useState } from "react";
import { useClaims, useAuditClaim } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";

export const Audits: React.FC = () => {
  const { toast } = useToast();
  const [selectedClaimId, setSelectedClaimId] = useState<number | null>(null);

  // Audit Form states
  const [findings, setFindings] = useState("");
  const [outcome, setOutcome] = useState("Passed");
  const [status, setStatus] = useState("Completed");

  const { data: claims, isLoading } = useClaims("COMPLIANCE");
  const auditMutation = useAuditClaim();

  const handleAuditSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedClaimId) return;

    auditMutation.mutate(
      {
        claimId: selectedClaimId,
        params: { findings, outcome, status },
      },
      {
        onSuccess: () => {
          toast("Claim audited and updated successfully", "success", "Audited");
          setSelectedClaimId(null);
          setFindings("");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Audit submission failed", "error");
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
      header: "Title",
      accessor: (c: any) => <span>{c.claimTitle}</span>,
    },
    {
      header: "Amount",
      accessor: (c: any) => <span className="font-semibold text-primary">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>,
    },
    {
      header: "Date Filed",
      accessor: (c: any) => <span>{formatDate(c.submittedDate)}</span>,
    },
    {
      header: "Status",
      accessor: (c: any) => <StatusBadge status={c.status} />,
    },
    {
      header: "Action",
      accessor: (c: any) => (
        <Button
          size="sm"
          onClick={() => {
            setSelectedClaimId(c.id);
            setIsDialogOpen(true);
          }}
        >
          Perform Audit
        </Button>
      ),
      align: "right" as const,
    },
  ];

  const [isDialogOpen, setIsDialogOpen] = useState(false);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Expense Claims Auditing</h1>
        <p className="text-xs text-muted-foreground">
          Perform compliance reviews and audit logs checks on active expense claims.
        </p>
      </div>

      <DataTable
        columns={columns}
        data={claims}
        isLoading={isLoading}
        emptyMessage="No claims found to audit."
      />

      <Dialog
        open={isDialogOpen && !!selectedClaimId}
        onOpenChange={(op) => {
          setIsDialogOpen(op);
          if (!op) setSelectedClaimId(null);
        }}
      >
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Perform Audit Review</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAuditSubmit} className="space-y-3 text-xs">
            <div className="space-y-1">
              <Label>Findings / Violations Details</Label>
              <textarea
                required
                className="flex min-h-[60px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm text-foreground"
                placeholder="Findings details..."
                value={findings}
                onChange={(e) => setFindings(e.target.value)}
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Audit Outcome</Label>
                <select
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                  value={outcome}
                  onChange={(e) => setOutcome(e.target.value)}
                >
                  <option value="Passed">Passed / Compliant</option>
                  <option value="Flagged">Flagged / Warning</option>
                  <option value="Rejected">Rejected</option>
                </select>
              </div>

              <div className="space-y-1">
                <Label>Audit Status</Label>
                <select
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="Completed">Completed</option>
                  <option value="In_Review">In Review</option>
                </select>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setIsDialogOpen(false);
                  setSelectedClaimId(null);
                }}
              >
                Cancel
              </Button>
              <Button type="submit">Submit Audit</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Audits;
