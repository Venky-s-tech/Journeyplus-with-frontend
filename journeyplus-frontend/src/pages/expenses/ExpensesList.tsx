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
import { formatCurrency, formatDate } from "../../lib/utils";
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

  const { data: claims, isLoading } = useClaims(user?.role || "EMPLOYEE");
  const { data: trips } = useTrips("EMPLOYEE");
  const createMutation = useCreateClaim();

  // Filter completed trips
  const completedTrips = trips?.filter((t) => t.status === "COMPLETED") || [];

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTripId) {
      toast("Please select a completed trip", "error");
      return;
    }

    createMutation.mutate(
      {
        tripId: Number(selectedTripId),
        data: {
          claimTitle: title,
          submittedDate: new Date().toISOString().split("T")[0],
          originalCurrency: "USD",
          expenseLines: [],
        },
      },
      {
        onSuccess: (data) => {
          toast("Expense claim folder created", "success", "Created");
          setIsOpen(false);
          setTitle("");
          setSelectedTripId("");
          // Redirect directly to details to start adding receipt lines
          navigate(`/expenses/${data.id}`);
        },
        onError: (err: any) => {
          const msg = err.response?.data?.message || "Failed to create claim folder";
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
      header: "Claim Title",
      accessor: (c: any) => <span>{c.claimTitle}</span>,
    },
    {
      header: "Date Logged",
      accessor: (c: any) => <span className="text-xs">{formatDate(c.submittedDate)}</span>,
    },
    {
      header: "Lines count",
      accessor: (c: any) => <span className="text-xs">{c.expenseLines?.length || 0} items</span>,
    },
    {
      header: "Total Value",
      accessor: (c: any) => <span className="font-semibold text-primary">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>,
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
              <Button className="gap-2 bg-purple-600 hover:bg-purple-700">
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
                    Expense claims can only be filed against trip requests that have completed all bookings and are marked as COMPLETED by the Travel Desk.
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
                      onChange={(e) => setSelectedTripId(e.target.value)}
                    >
                      <option value="">Select completed trip</option>
                      {completedTrips.map((t) => (
                        <option key={t.id} value={t.id}>
                          #{t.id} - {t.destination} ({formatDate(t.departureDate)})
                        </option>
                      ))}
                    </select>
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

                  <div className="flex justify-end gap-2 pt-2">
                    <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>
                      Cancel
                    </Button>
                    <Button type="submit" disabled={createMutation.isPending}>
                      {createMutation.isPending ? "Creating..." : "Create Claim Folder"}
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
