import React, { useState } from "react";
import {
  useTrips,
  useAdvances,
  useClaims,
  useApproveTrip,
  useRejectTrip,
  useApproveAdvance,
  useApproveClaim,
  useRejectClaim,
} from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "../../components/ui/tabs";
import { Check, X, ShieldAlert } from "lucide-react";

export const Approvals: React.FC = () => {
  const { toast } = useToast();
  const [commentsMap, setCommentsMap] = useState<Record<string, string>>({});

  // Query pending datasets
  const { data: trips, isLoading: tripsLoading } = useTrips("APPROVING_MANAGER");
  const { data: advances, isLoading: advLoading } = useAdvances("APPROVING_MANAGER");
  const { data: claims, isLoading: claimsLoading } = useClaims("APPROVING_MANAGER");

  // Filter local lists to pending items only
  const pendingTrips = trips?.filter((t) => t.status === "SUBMITTED") || [];
  const pendingAdvances = advances?.filter((a) => a.status === "PENDING_APPROVAL") || [];
  const pendingClaims = claims?.filter((c) => c.status === "SUBMITTED") || [];

  // Mutations
  const approveTripMut = useApproveTrip();
  const rejectTripMut = useRejectTrip();
  const approveAdvMut = useApproveAdvance();
  const approveClaimMut = useApproveClaim();
  const rejectClaimMut = useRejectClaim();

  const handleCommentChange = (key: string, val: string) => {
    setCommentsMap((prev) => ({ ...prev, [key]: val }));
  };

  const handleTripAction = (id: number, approve: boolean) => {
    const comment = commentsMap[`trip-${id}`] || "";
    const mutation = approve ? approveTripMut : rejectTripMut;
    mutation.mutate(
      { id, comments: comment },
      {
        onSuccess: () => {
          toast(approve ? "Trip Request Approved" : "Trip Request Rejected", "success", "Success");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Action failed", "error");
        },
      }
    );
  };

  const handleAdvanceApproval = (id: number) => {
    approveAdvMut.mutate(id, {
      onSuccess: () => {
        toast("Travel Advance Approved", "success", "Success");
      },
    });
  };

  const handleClaimAction = (id: number, approve: boolean) => {
    const comment = commentsMap[`claim-${id}`] || "";
    const mutation = approve ? approveClaimMut : rejectClaimMut;
    mutation.mutate(
      { id, comments: comment },
      {
        onSuccess: () => {
          toast(approve ? "Expense Claim Approved" : "Expense Claim Rejected", "success", "Success");
        },
      }
    );
  };

  const tripColumns = [
    {
      header: "Employee",
      accessor: (t: any) => <span>{t.employee?.name || t.employee?.username}</span>,
    },
    {
      header: "Destination",
      accessor: (t: any) => <span>{t.destination}</span>,
    },
    {
      header: "Dates",
      accessor: (t: any) => (
        <span className="text-xs">
          {formatDate(t.departureDate)} - {formatDate(t.returnDate)}
        </span>
      ),
    },
    {
      header: "Cost",
      accessor: (t: any) => <span>{formatCurrency(t.estimatedCost)}</span>,
    },
    {
      header: "Comments / Action",
      accessor: (t: any) => (
        <div className="flex items-center gap-2 max-w-sm">
          <Input
            placeholder="Review comments..."
            value={commentsMap[`trip-${t.id}`] || ""}
            onChange={(e) => handleCommentChange(`trip-${t.id}`, e.target.value)}
            className="h-8 text-xs flex-1"
          />
          <Button
            size="sm"
            onClick={() => handleTripAction(t.id, true)}
            className="h-8 bg-green-600 hover:bg-green-700 p-2"
          >
            <Check className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="destructive"
            onClick={() => handleTripAction(t.id, false)}
            className="h-8 p-2"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  const advanceColumns = [
    {
      header: "Trip ID",
      accessor: (a: any) => <span className="font-semibold">#{a.tripRequestId}</span>,
    },
    {
      header: "Requested Limit",
      accessor: (a: any) => <span className="font-semibold">{formatCurrency(a.requestedAmount, a.currency)}</span>,
    },
    {
      header: "Purpose Description",
      accessor: (a: any) => <span className="text-xs text-muted-foreground">{a.purposeDetails}</span>,
    },
    {
      header: "Action",
      accessor: (a: any) => (
        <Button
          size="sm"
          onClick={() => handleAdvanceApproval(a.id)}
          className="bg-green-600 hover:bg-green-700 gap-1"
          disabled={approveAdvMut.isPending}
        >
          <Check className="h-3.5 w-3.5" /> Approve
        </Button>
      ),
      align: "right" as const,
    },
  ];

  const claimColumns = [
    {
      header: "Title",
      accessor: (c: any) => <span className="font-semibold">{c.claimTitle}</span>,
    },
    {
      header: "Claim Amount",
      accessor: (c: any) => <span className="font-semibold">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>,
    },
    {
      header: "Date Filed",
      accessor: (c: any) => <span className="text-xs">{formatDate(c.submittedDate)}</span>,
    },
    {
      header: "Compliance",
      accessor: (c: any) => (
        <span className="text-xs">
          Lines: {c.expenseLines?.length || 0}
        </span>
      ),
    },
    {
      header: "Review comments / Decision",
      accessor: (c: any) => (
        <div className="flex items-center gap-2 max-w-sm">
          <Input
            placeholder="Review comments..."
            value={commentsMap[`claim-${c.id}`] || ""}
            onChange={(e) => handleCommentChange(`claim-${c.id}`, e.target.value)}
            className="h-8 text-xs flex-1"
          />
          <Button
            size="sm"
            onClick={() => handleClaimAction(c.id, true)}
            className="h-8 bg-green-600 hover:bg-green-700 p-2"
          >
            <Check className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="destructive"
            onClick={() => handleClaimAction(c.id, false)}
            className="h-8 p-2"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Approvals Inbox</h1>
        <p className="text-xs text-muted-foreground">
          Review and approve pending trip requests, cash advances, and employee expense claims.
        </p>
      </div>

      <Tabs defaultValue="trips">
        <TabsList className="mb-4">
          <TabsTrigger value="trips">Trips ({pendingTrips.length})</TabsTrigger>
          <TabsTrigger value="advances">Advances ({pendingAdvances.length})</TabsTrigger>
          <TabsTrigger value="claims">Expense Claims ({pendingClaims.length})</TabsTrigger>
        </TabsList>

        <TabsContent value="trips">
          <DataTable
            columns={tripColumns}
            data={pendingTrips}
            isLoading={tripsLoading}
            emptyMessage="No pending trip requests awaiting approval."
          />
        </TabsContent>

        <TabsContent value="advances">
          <DataTable
            columns={advanceColumns}
            data={pendingAdvances}
            isLoading={advLoading}
            emptyMessage="No pending travel advance cash requests."
          />
        </TabsContent>

        <TabsContent value="claims">
          <DataTable
            columns={claimColumns}
            data={pendingClaims}
            isLoading={claimsLoading}
            emptyMessage="No pending expense claims awaiting approval."
          />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default Approvals;
