import React, { useState } from "react";
import { useAuth } from "../../lib/auth-context";
import { useAdvances, useRequestAdvance, useTrips } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { Coins, Plus, AlertCircle } from "lucide-react";

export const Advances: React.FC = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const [isOpen, setIsOpen] = useState(false);

  // Form states
  const [selectedTripId, setSelectedTripId] = useState("");
  const [amount, setAmount] = useState(100);
  const [purpose, setPurpose] = useState("");

  const { data: advances, isLoading } = useAdvances(user?.role || "EMPLOYEE");
  const { data: trips } = useTrips("EMPLOYEE");
  const requestMutation = useRequestAdvance();

  // Filter only APPROVED trips that don't have advances already
  const approvedTrips = trips?.filter((t) => t.status === "APPROVED") || [];

  const handleRequest = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTripId) {
      toast("Please select an approved trip", "error");
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
          const msg = err.response?.data?.message || "Failed to request advance";
          toast(msg, "error", "Request Failed");
        },
      }
    );
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
      header: "Date Disbursed",
      accessor: (a: any) => (
        <span className="text-xs">{a.disbursedDate ? formatDate(a.disbursedDate) : "Not Disbursed"}</span>
      ),
    },
    {
      header: "Status",
      accessor: (a: any) => <StatusBadge status={a.status} />,
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
              <Button className="gap-2">
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
                        <option key={t.id} value={t.id}>
                          #{t.id} - {t.destination} ({formatDate(t.departureDate)})
                        </option>
                      ))}
                    </select>
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
                    <Button type="submit" disabled={requestMutation.isPending}>
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
    </div>
  );
};

export default Advances;
