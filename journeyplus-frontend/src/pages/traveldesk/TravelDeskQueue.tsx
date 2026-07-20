import React from "react";
import { useNavigate } from "react-router-dom";
import { usePendingBookings, useTravelDeskDashboard } from "../../hooks/useTravelDesk";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Button } from "../../components/ui/button";
import {
  Plane,
  Building,
  Globe,
  CheckCircle,
  Clock,
  ArrowRight,
  FileCheck,
} from "lucide-react";

export const TravelDeskQueue: React.FC = () => {
  const navigate = useNavigate();
  const { data: queue, isLoading } = usePendingBookings();
  const { data: metrics } = useTravelDeskDashboard();

  const columns = [
    {
      header: "Trip ID",
      accessor: (t: any) => <span className="font-semibold text-xs">#{t.tripId || t.id}</span>,
    },
    {
      header: "Employee",
      accessor: (t: any) => (
        <div className="flex flex-col">
          <span className="font-medium text-xs">{t.employeeName}</span>
          <span className="text-[10px] text-muted-foreground">{t.department}</span>
        </div>
      ),
    },
    {
      header: "Destination",
      accessor: (t: any) => <span className="text-xs font-medium">{t.destination}</span>,
    },
    {
      header: "Travel Type",
      accessor: (t: any) => (
        <span
          className={`text-[10px] font-semibold uppercase px-2 py-0.5 rounded-full ${
            t.travelType === "INTERNATIONAL"
              ? "bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-300"
              : "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300"
          }`}
        >
          {t.travelType}
        </span>
      ),
    },
    {
      header: "Dates",
      accessor: (t: any) => (
        <span className="text-xs text-muted-foreground">
          {formatDate(t.departureDate)} - {formatDate(t.returnDate)}
        </span>
      ),
    },
    {
      header: "Estimated Cost",
      accessor: (t: any) => <span className="text-xs font-semibold">{formatCurrency(t.estimatedCost)}</span>,
    },
    {
      header: "Booking Status",
      accessor: (t: any) => <StatusBadge status={t.bookingStatus || "PENDING"} />,
    },
    {
      header: "Visa Status",
      accessor: (t: any) => <StatusBadge status={t.visaStatus || "NOT_REQUIRED"} />,
    },
    {
      header: "Action",
      accessor: (t: any) => (
        <Button
          size="sm"
          variant="outline"
          className="text-xs h-8 gap-1"
          onClick={() => navigate(`/travel-desk/bookings/${t.tripId || t.id}`)}
        >
          Fulfill <ArrowRight className="h-3 w-3" />
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in-50 duration-200">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Travel Desk Booking Queue</h1>
        <p className="text-xs text-muted-foreground">
          Review manager-approved trip requests, issue itineraries, enter flight/hotel bookings, and confirm travel.
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Bookings</span>
            <p className="text-2xl font-bold text-blue-600">{metrics?.pendingBookings ?? queue?.length ?? 0}</p>
          </div>
          <div className="p-2 bg-blue-50 dark:bg-blue-950/20 rounded-full text-blue-600">
            <Clock className="h-5 w-5" />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Waiting For Itinerary</span>
            <p className="text-2xl font-bold text-yellow-600">{metrics?.waitingForItinerary ?? 0}</p>
          </div>
          <div className="p-2 bg-yellow-50 dark:bg-yellow-950/20 rounded-full text-yellow-600">
            <Plane className="h-5 w-5" />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Waiting For Visa</span>
            <p className="text-2xl font-bold text-purple-600">{metrics?.waitingForVisa ?? 0}</p>
          </div>
          <div className="p-2 bg-purple-50 dark:bg-purple-950/20 rounded-full text-purple-600">
            <Globe className="h-5 w-5" />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Completed Bookings</span>
            <p className="text-2xl font-bold text-green-600">{metrics?.completedBookings ?? 0}</p>
          </div>
          <div className="p-2 bg-green-50 dark:bg-green-950/20 rounded-full text-green-600">
            <CheckCircle className="h-5 w-5" />
          </div>
        </div>
      </div>

      {/* Queue Table */}
      <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
        <h2 className="text-sm font-semibold flex items-center gap-2">
          <FileCheck className="h-4 w-4 text-primary" /> Manager-Approved Trips Queue
        </h2>
        <DataTable
          columns={columns}
          data={queue || []}
          isLoading={isLoading}
          emptyMessage="No pending manager-approved trips awaiting travel desk processing."
        />
      </div>
    </div>
  );
};

export default TravelDeskQueue;
