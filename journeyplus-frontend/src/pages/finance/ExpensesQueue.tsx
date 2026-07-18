import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useClaims, useReimburseClaim } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";

export const ExpensesQueue: React.FC = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [selectedClaimId, setSelectedClaimId] = useState<number | null>(null);

  // Form fields
  const [paymentMethod, setPaymentMethod] = useState("BANK_TRANSFER");
  const [txnRef, setTxnRef] = useState("");
  const [amount, setAmount] = useState(0);

  const { data: claims, isLoading } = useClaims("FINANCE");
  const reimburseMutation = useReimburseClaim();

  // Filter approved claims (reimbursable)
  const approvedClaims = claims?.filter((c) => c.status === "APPROVED") || [];

  const handleReimburse = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedClaimId) return;

    reimburseMutation.mutate(
      {
        claimId: selectedClaimId,
        paymentMethod,
        transactionReference: txnRef,
        amount: Number(amount),
      },
      {
        onSuccess: () => {
          toast("Expense claim reimbursed successfully", "success", "Paid");
          setSelectedClaimId(null);
          setTxnRef("");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Reimbursement failed", "error");
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
      header: "Approved Date",
      accessor: (c: any) => <span className="text-xs">{formatDate(c.submittedDate)}</span>,
    },
    {
      header: "Total Due",
      accessor: (c: any) => <span className="font-semibold text-primary">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>,
    },
    {
      header: "Actions",
      accessor: (c: any) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="outline" onClick={() => navigate(`/expenses/${c.id}`)}>
            View items
          </Button>
          <Button
            size="sm"
            onClick={() => {
              setSelectedClaimId(c.id);
              setAmount(c.totalAmount);
              setIsDialogOpen(true);
            }}
            className="bg-purple-600 hover:bg-purple-700"
          >
            Reimburse
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
        <h1 className="text-2xl font-bold tracking-tight">Expenses Reimbursement Queue</h1>
        <p className="text-xs text-muted-foreground">
          Finance workspace for disbursing payments to approved employee expense claims.
        </p>
      </div>

      <DataTable
        columns={columns}
        data={approvedClaims}
        isLoading={isLoading}
        emptyMessage="No approved expense claims pending reimbursement."
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
            <DialogTitle>Disburse Reimbursement</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleReimburse} className="space-y-3 text-xs">
            <div className="space-y-1">
              <Label>Payment Mode</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
              >
                <option value="BANK_TRANSFER">Bank Wire / Transfer</option>
                <option value="CREDIT_CARD">Credit Card transfer</option>
                <option value="CASH">Petty Cash</option>
              </select>
            </div>

            <div className="space-y-1">
              <Label>Transaction Reference ID</Label>
              <Input
                required
                placeholder="TXN123456789"
                value={txnRef}
                onChange={(e) => setTxnRef(e.target.value)}
              />
            </div>

            <div className="space-y-1">
              <Label>Reimbursement Amount (USD)</Label>
              <Input
                type="number"
                required
                value={amount}
                onChange={(e) => setAmount(Number(e.target.value))}
              />
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
              <Button type="submit">Submit Disbursement</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ExpensesQueue;
