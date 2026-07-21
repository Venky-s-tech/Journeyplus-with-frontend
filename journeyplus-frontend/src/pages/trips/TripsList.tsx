import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../lib/auth-context";
import { useTrips, useSubmitTrip, useCancelTrip, useCompleteTrip } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import { TripStatus } from "../../types";
import {
  Plane,
  Plus,
  Search,
  CheckCircle,
  Eye,
  Calendar,
  Filter,
} from "lucide-react";

export const TripsList: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL");

  const { data: trips, isLoading, error } = useTrips(user?.role || "EMPLOYEE");
  const submitMutation = useSubmitTrip();
  const cancelMutation = useCancelTrip();
  const completeMutation = useCompleteTrip();

  const handleAction = (tripId: number, actionName: string, mutation: any) => {
    mutation.mutate(tripId, {
      onSuccess: () => {
        toast(`Trip #${tripId} status updated: ${actionName}`, "success", "Success");
      },
      onError: (err: any) => {
        const msg = getErrorMessage(err, `Failed to ${actionName.toLowerCase()} trip`);
        toast(msg, "error", "Action Failed");
      },
    });
  };

  const handleComplete = (tripId: number) => {
    if (window.confirm("Have you completed your business trip? Once completed, you can submit your expense claim and settle eligible travel advances.")) {
      handleAction(tripId, "COMPLETED", completeMutation);
    }
  };

  const filteredTrips = (trips || []).filter((trip) => {
    const matchesSearch =
      trip.destination?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      trip.purpose?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      trip.id.toString().includes(searchQuery);

    const matchesStatus =
      selectedStatus === "ALL" || trip.status === selectedStatus;

    return matchesSearch && matchesStatus;
  });

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 text-center bg-card border border-border rounded-lg">
        <p className="text-sm text-destructive">Error loading trip requests.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Trip Requests</h1>
          <p className="text-xs text-muted-foreground">
            Manage business travel requests, logistics approval, and completion.
          </p>
        </div>
        {user?.role === "EMPLOYEE" && (
          <Button onClick={() => navigate("/trips/new")} className="gap-2">
            <Plus className="h-4 w-4" /> Raise Trip Request
          </Button>
        )}
      </div>

      {/* Search & Filter Bar */}
      <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by destination or purpose..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9 h-9 text-xs"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <Filter className="h-4 w-4 text-muted-foreground shrink-0" />
          <select
            className="flex h-9 w-full md:w-48 rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-sm transition-colors text-foreground"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="APPROVED">Approved</option>
            <option value="BOOKED">Booked</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
      </div>

      {/* Trips Table / Cards */}
      <div className="bg-card border border-border rounded-lg shadow-sm overflow-hidden">
        {filteredTrips.length === 0 ? (
          <div className="p-12 text-center text-muted-foreground space-y-3">
            <Plane className="h-10 w-10 mx-auto text-muted-foreground/50" />
            <p className="text-sm font-medium">No trip requests found matching your filters.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left border-collapse">
              <thead>
                <tr className="border-b border-border bg-muted/50 text-muted-foreground font-semibold uppercase text-[10px]">
                  <th className="py-3 px-4">Trip ID</th>
                  <th className="py-3 px-4">Destination</th>
                  <th className="py-3 px-4">Purpose</th>
                  <th className="py-3 px-4">Type</th>
                  <th className="py-3 px-4">Dates</th>
                  <th className="py-3 px-4">Est. Cost</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {filteredTrips.map((t) => {
                  const isOwner = user?.username === t.employee?.username;
                  return (
                    <tr key={t.id} className="hover:bg-muted/30 transition-colors">
                      <td className="py-3 px-4 font-mono font-semibold">#{t.id}</td>
                      <td className="py-3 px-4 font-medium">{t.destination}</td>
                      <td className="py-3 px-4 text-muted-foreground truncate max-w-[200px]">{t.purpose}</td>
                      <td className="py-3 px-4 uppercase font-bold text-[10px] text-muted-foreground">{t.travelType}</td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-1 text-muted-foreground">
                          <Calendar className="h-3 w-3 shrink-0" />
                          <span>{formatDate(t.departureDate)} - {formatDate(t.returnDate)}</span>
                        </div>
                      </td>
                      <td className="py-3 px-4 font-semibold text-primary">{formatCurrency(t.estimatedCost)}</td>
                      <td className="py-3 px-4">
                        <StatusBadge status={t.status} />
                      </td>
                      <td className="py-3 px-4 text-right space-x-2">
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => navigate(`/trips/${t.id}`)}
                          className="h-7 text-xs gap-1"
                        >
                          <Eye className="h-3.5 w-3.5" /> Details
                        </Button>

                        {isOwner && t.status === "DRAFT" && (
                          <Button
                            size="sm"
                            onClick={() => handleAction(t.id, "SUBMITTED", submitMutation)}
                            className="h-7 text-xs"
                          >
                            Submit
                          </Button>
                        )}

                        {/* Owner action: Mark Trip Completed only when status === BOOKED */}
                        {isOwner && t.status === "BOOKED" && (
                          <Button
                            size="sm"
                            onClick={() => handleComplete(t.id)}
                            className="h-7 text-xs bg-emerald-600 hover:bg-emerald-700 text-white gap-1"
                          >
                            <CheckCircle className="h-3.5 w-3.5" /> Mark Completed
                          </Button>
                        )}

                        {isOwner && (t.status === "DRAFT" || t.status === "APPROVED") && (
                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => handleAction(t.id, "CANCELLED", cancelMutation)}
                            className="h-7 text-xs"
                          >
                            Cancel
                          </Button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default TripsList;