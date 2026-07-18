import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useAuth } from "../../lib/auth-context";
import { useTrips, useCreateTrip, useCompleteTrip } from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import { Plus, Search, Calendar, Landmark } from "lucide-react";

const tripSchema = z.object({
  purpose: z.string().min(3, "Purpose must be at least 3 characters"),
  destination: z.string().min(2, "Destination is required"),
  departureDate: z.string().min(1, "Departure date is required"),
  returnDate: z.string().min(1, "Return date is required"),
  travelType: z.enum(["DOMESTIC", "INTERNATIONAL"]),
  estimatedCost: z.coerce.number().min(1, "Estimated cost must be greater than 0"),
  comments: z.string().optional().or(z.literal("")),
  approverUsername: z.string().min(1, "Approver is required"),
});

type TripFields = z.infer<typeof tripSchema>;

export const TripsList: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [filterText, setFilterText] = useState("");
  const [filterType, setFilterType] = useState("ALL");
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const { data: trips, isLoading } = useTrips(user?.role || "EMPLOYEE");
  const createMutation = useCreateTrip();
  const completeMutation = useCompleteTrip();
  const isTD = user?.role === "TRAVEL_DESK";

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TripFields>({
    resolver: zodResolver(tripSchema) as any,
    defaultValues: {
      travelType: "DOMESTIC",
      estimatedCost: 100,
    },
  });

  const handleCreateTrip = (data: TripFields) => {
    const dep = new Date(data.departureDate);
    const ret = new Date(data.returnDate);
    if (ret < dep) {
      toast("Return date must be after or on departure date", "error", "Validation Error");
      return;
    }

    createMutation.mutate(data, {
      onSuccess: () => {
        toast("Trip request created successfully", "success", "Created");
        setIsDialogOpen(false);
        reset();
      },
      onError: (err: any) => {
        const msg = getErrorMessage(err, "Failed to create trip");
        toast(msg, "error", "Error");
      },
    });
  };

  // Item 1: Mark an APPROVED trip as COMPLETED directly from the list view.
  // This mutates the trip status and relies on useCompleteTrip's cache
  // invalidation (via react-query) to refresh the local `trips` list.
  const handleCompleteTrip = (tripId: number) => {
    completeMutation.mutate(tripId, {
      onSuccess: () => {
        toast("Trip marked as Completed", "success", "Success");
      },
      onError: (err: any) => {
        const msg = getErrorMessage(err, "Failed to complete trip");
        toast(msg, "error", "Error");
      },
    });
  };

  const filteredTrips = trips?.filter((t) => {
    const matchesSearch =
      t.purpose.toLowerCase().includes(filterText.toLowerCase()) ||
      t.destination.toLowerCase().includes(filterText.toLowerCase());
    const matchesType = filterType === "ALL" || t.travelType === filterType;
    return matchesSearch && matchesType;
  });

  const columns = [
    {
      header: "Trip ID",
      accessor: (t: any) => <span className="font-semibold">#{t.id}</span>,
    },
    {
      header: "Destination",
      accessor: (t: any) => <span>{t.destination}</span>,
    },
    {
      header: "Purpose",
      accessor: (t: any) => <span className="text-xs text-muted-foreground line-clamp-1">{t.purpose}</span>,
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
      header: "Type",
      accessor: (t: any) => (
        <span className="text-xs font-semibold">{t.travelType}</span>
      ),
    },
    {
      header: "Est. Cost",
      accessor: (t: any) => <span>{formatCurrency(t.estimatedCost)}</span>,
    },
    {
      header: "Status",
      accessor: (t: any) => <StatusBadge status={t.status} />,
    },
    {
      header: "Actions",
      accessor: (t: any) => (
        <div className="flex justify-end gap-2">
          {/* Item 1: show a Complete button next to any APPROVED trip (Travel Desk only) */}
          {isTD && t.status === "APPROVED" && (
            <Button
              size="sm"
              className="bg-purple-600 hover:bg-purple-700"
              disabled={completeMutation.isPending}
              onClick={(e) => {
                e.stopPropagation();
                handleCompleteTrip(t.id);
              }}
            >
              Complete
            </Button>
          )}
          <Button size="sm" variant="outline" onClick={() => navigate(`/trips/${t.id}`)}>
            View
          </Button>
        </div>
      ),
      align: "right" as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Trip Requests</h1>
          <p className="text-xs text-muted-foreground">
            Manage your draft, submitted, and approved trip requests.
          </p>
        </div>

        {user?.role === "EMPLOYEE" && (
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button className="gap-2">
                <Plus className="h-4 w-4" /> Create Trip
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-lg">
              <DialogHeader>
                <DialogTitle>New Trip Request</DialogTitle>
              </DialogHeader>
              <form onSubmit={handleSubmit(handleCreateTrip as any)} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label htmlFor="destination">Destination</Label>
                    <Input id="destination" placeholder="e.g. London" {...register("destination")} />
                    {errors.destination && <p className="text-xs text-destructive">{errors.destination.message}</p>}
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="travelType">Travel Type</Label>
                    <select
                      id="travelType"
                      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring text-foreground"
                      {...register("travelType")}
                    >
                      <option value="DOMESTIC">Domestic</option>
                      <option value="INTERNATIONAL">International</option>
                    </select>
                    {errors.travelType && <p className="text-xs text-destructive">{errors.travelType.message}</p>}
                  </div>
                </div>

                <div className="space-y-1">
                  <Label htmlFor="purpose">Purpose of Travel</Label>
                  <Input id="purpose" placeholder="Client onboarding workshop" {...register("purpose")} />
                  {errors.purpose && <p className="text-xs text-destructive">{errors.purpose.message}</p>}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label htmlFor="departureDate">Departure Date</Label>
                    <Input id="departureDate" type="date" {...register("departureDate")} />
                    {errors.departureDate && <p className="text-xs text-destructive">{errors.departureDate.message}</p>}
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="returnDate">Return Date</Label>
                    <Input id="returnDate" type="date" {...register("returnDate")} />
                    {errors.returnDate && <p className="text-xs text-destructive">{errors.returnDate.message}</p>}
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <Label htmlFor="estimatedCost">Estimated Cost (USD)</Label>
                    <Input id="estimatedCost" type="number" {...register("estimatedCost")} />
                    {errors.estimatedCost && <p className="text-xs text-destructive">{errors.estimatedCost.message}</p>}
                  </div>

                  <div className="space-y-1">
                    <Label htmlFor="approverUsername">Approver Manager</Label>
                    {/* Bug #10: free-text entry for the approver manager (was a dropdown). */}
                    <Input
                      id="approverUsername"
                      placeholder="e.g. manager username"
                      {...register("approverUsername")}
                    />
                    {errors.approverUsername && <p className="text-xs text-destructive">{errors.approverUsername.message}</p>}
                  </div>
                </div>

                <div className="space-y-1">
                  <Label htmlFor="comments">Additional Comments</Label>
                  <textarea
                    id="comments"
                    className="flex min-h-[60px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring text-foreground"
                    placeholder="Provide details about client meetings, local conveyance needs, etc."
                    {...register("comments")}
                  />
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={createMutation.isPending}>
                    {createMutation.isPending ? "Creating..." : "Submit"}
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by purpose or destination..."
            value={filterText}
            onChange={(e) => setFilterText(e.target.value)}
            className="pl-9"
          />
        </div>
        <select
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          className="flex h-9 w-full sm:w-48 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring text-foreground"
        >
          <option value="ALL">All Types</option>
          <option value="DOMESTIC">Domestic</option>
          <option value="INTERNATIONAL">International</option>
        </select>
      </div>

      {/* Data Table */}
      <DataTable columns={columns} data={filteredTrips} isLoading={isLoading} emptyMessage="No trip requests matches criteria." />
    </div>
  );
};

export default TripsList;
