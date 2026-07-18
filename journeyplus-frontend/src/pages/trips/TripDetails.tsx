import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../lib/auth-context";
import {
  useTrip,
  useSubmitTrip,
  useApproveTrip,
  useRejectTrip,
  useCompleteTrip,
  useCancelTrip,
  useAddItineraryLeg,
  useDeleteItineraryLeg,
  useBookItineraryLeg,
  useAddVisaRequirement,
  useUpdateVisaRequirement,
} from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate, getErrorMessage } from "../../lib/utils";
import {
  Plane,
  Calendar,
  DollarSign,
  Briefcase,
  FileText,
  User,
  Plus,
  Trash2,
  Bookmark,
  CheckCircle,
  FileCheck,
} from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";

export const TripDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  const tripId = Number(id);

  const { data: trip, isLoading, error } = useTrip(tripId);

  // Mutations
  const submitMutation = useSubmitTrip();
  const approveMutation = useApproveTrip();
  const rejectMutation = useRejectTrip();
  const completeMutation = useCompleteTrip();
  const cancelMutation = useCancelTrip();
  const addLegMutation = useAddItineraryLeg(tripId);
  const deleteLegMutation = useDeleteItineraryLeg(tripId);
  const bookLegMutation = useBookItineraryLeg(tripId);
  const addVisaMutation = useAddVisaRequirement(tripId);
  const updateVisaMutation = useUpdateVisaRequirement(tripId);

  // States
  const [managerComment, setManagerComment] = useState("");
  const [isLegDialogOpen, setIsLegDialogOpen] = useState(false);
  const [isVisaDialogOpen, setIsVisaDialogOpen] = useState(false);
  const [isBookDialogOpen, setIsBookDialogOpen] = useState(false);
  const [selectedLegId, setSelectedLegId] = useState<number | null>(null);

  // Leg Form state
  const [legOrigin, setLegOrigin] = useState("");
  const [legDest, setLegDest] = useState("");
  const [legType, setLegType] = useState<"FLIGHT" | "TRAIN" | "HOTEL" | "CAR_RENTAL">("FLIGHT");
  const [legTravelDate, setLegTravelDate] = useState("");
  const [legDepTime, setLegDepTime] = useState("");
  const [legArrTime, setLegArrTime] = useState("");
  const [legCarrier, setLegCarrier] = useState("");
  const [legBookingRef, setLegBookingRef] = useState("");
  const [legCost, setLegCost] = useState(0);

  // Visa Form state
  const [visaCountry, setVisaCountry] = useState("");
  const [visaType, setVisaType] = useState("BUSINESS");
  const [visaNotes, setVisaNotes] = useState("");

  // Book Form state
  const [bookingRefInput, setBookingRefInput] = useState("");

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
      </div>
    );
  }

  if (error || !trip) {
    return (
      <div className="p-6 text-center bg-card border border-border rounded-lg">
        <p className="text-sm text-destructive">Error loading trip request details.</p>
        <Button onClick={() => navigate("/trips")} className="mt-4">
          Back to List
        </Button>
      </div>
    );
  }

  const isOwner = user?.username === trip.employee?.username;
  const isApprover = user?.username === trip.approver?.username || user?.role === "APPROVING_MANAGER";
  const isTD = user?.role === "TRAVEL_DESK";

  const handleAction = (mutation: any, actionName: string) => {
    mutation.mutate(tripId, {
      onSuccess: () => {
        toast(`Trip request status: ${actionName}`, "success", "Success");
      },
      onError: (err: any) => {
        const msg = getErrorMessage(err, `Failed to ${actionName.toLowerCase()} trip`);
        toast(msg, "error", "Error");
      },
    });
  };

  const handleManagerAction = (approve: boolean) => {
    const mutation = approve ? approveMutation : rejectMutation;
    mutation.mutate(
      { id: tripId, comments: managerComment },
      {
        onSuccess: () => {
          toast(approve ? "Trip Approved" : "Trip Rejected", "success", "Success");
          setManagerComment("");
        },
        onError: (err: any) => {
          const msg = getErrorMessage(err, "Action failed");
          toast(msg, "error", "Error");
        },
      }
    );
  };

  const handleAddLeg = (e: React.FormEvent) => {
    e.preventDefault();
    addLegMutation.mutate(
      {
        origin: legOrigin,
        destination: legDest,
        legType,
        travelDate: legTravelDate,
        departureDateTime: legDepTime ? `${legTravelDate}T${legDepTime}:00.000Z` : undefined,
        arrivalDateTime: legArrTime ? `${legTravelDate}T${legArrTime}:00.000Z` : undefined,
        carrierDetails: legCarrier,
        bookingRef: legBookingRef,
        cost: Number(legCost),
        originalCurrency: "USD",
        usdEquivalent: Number(legCost),
      },
      {
        onSuccess: () => {
          toast("Itinerary leg added", "success", "Success");
          setIsLegDialogOpen(false);
          // reset form
          setLegOrigin("");
          setLegDest("");
          setLegCarrier("");
          setLegBookingRef("");
          setLegCost(0);
        },
        onError: (err: any) => {
          const msg = getErrorMessage(err, "Failed to add leg");
          toast(msg, "error", "Error");
        },
      }
    );
  };

  const handleAddVisa = (e: React.FormEvent) => {
    e.preventDefault();
    addVisaMutation.mutate(
      {
        country: visaCountry,
        visaType,
        requiresVisa: true,
        notes: visaNotes,
        status: "PENDING",
      },
      {
        onSuccess: () => {
          toast("Visa requirement added", "success", "Success");
          setIsVisaDialogOpen(false);
          setVisaCountry("");
          setVisaNotes("");
        },
        onError: (err: any) => {
          const msg = getErrorMessage(err, "Failed to add visa requirement");
          toast(msg, "error", "Error");
        },
      }
    );
  };

  const handleBookLeg = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedLegId) return;
    bookLegMutation.mutate(
      {
        legId: selectedLegId,
        bookingReference: bookingRefInput,
        bookingStatus: "BOOKED",
      },
      {
        onSuccess: () => {
          toast("Itinerary leg booked successfully", "success", "Success");
          setIsBookDialogOpen(false);
          setBookingRefInput("");
          setSelectedLegId(null);
        },
        onError: (err: any) => {
          const msg = getErrorMessage(err, "Booking reference must be the same as the existing booking reference");
          toast(msg, "error", "Booking Failed");
        },
      }
    );
  };

  const handleResolveVisa = (visaId: number) => {
    updateVisaMutation.mutate(
      {
        visaId,
        data: { status: "GRANTED" },
      },
      {
        onSuccess: () => {
          toast("Visa status updated to GRANTED", "success", "Success");
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      {/* Header Panel */}
      <div className="p-6 bg-card border border-border rounded-lg shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold">Trip Request Details</h1>
            <StatusBadge status={trip.status} />
          </div>
          <p className="text-xs text-muted-foreground">ID: #{trip.id} | Purpose: {trip.purpose}</p>
        </div>

        {/* Dynamic Action Buttons based on status & role */}
        <div className="flex flex-wrap gap-2">
          {/* Employee actions */}
          {isOwner && trip.status === "DRAFT" && (
            <>
              <Button onClick={() => handleAction(submitMutation, "SUBMITTED")}>
                Submit for Approval
              </Button>
              <Button variant="destructive" onClick={() => handleAction(cancelMutation, "CANCELLED")}>
                Cancel Trip
              </Button>
            </>
          )}
          {isOwner && trip.status === "APPROVED" && (
            <Button variant="destructive" onClick={() => handleAction(cancelMutation, "CANCELLED")}>
              Cancel Trip
            </Button>
          )}

          {/* Travel Desk actions */}
          {/* Item 1: Complete button — shown only when status is APPROVED, Travel Desk only */}
          {isTD && trip.status === "APPROVED" && (
            <Button onClick={() => handleAction(completeMutation, "COMPLETED")} className="bg-purple-600 hover:bg-purple-700">
              Complete
            </Button>
          )}

          {/* Manager Approvals queue actions */}
          {isApprover && trip.status === "SUBMITTED" && (
            <div className="flex flex-col gap-2 w-full sm:w-auto">
              <Input
                placeholder="Approval/rejection comments..."
                value={managerComment}
                onChange={(e) => setManagerComment(e.target.value)}
                className="w-full sm:w-64"
              />
              <div className="flex gap-2">
                <Button onClick={() => handleManagerAction(true)} className="bg-green-600 hover:bg-green-700 flex-1 sm:flex-initial">
                  Approve
                </Button>
                <Button onClick={() => handleManagerAction(false)} variant="destructive" className="flex-1 sm:flex-initial">
                  Reject
                </Button>
              </div>
            </div>
          )}

          <Button variant="outline" onClick={() => navigate("/trips")}>
            Back
          </Button>
        </div>
      </div>

      {/* Main Info Blocks */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left column - Metadata */}
        <div className="md:col-span-1 space-y-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold border-b pb-2 flex items-center gap-2">
              <Briefcase className="h-4 w-4 text-muted-foreground" /> Trip Metadata
            </h2>
            <div className="space-y-3 text-xs">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Destination:</span>
                <span className="font-medium">{trip.destination}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Travel Type:</span>
                <span className="font-medium">{trip.travelType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Departure:</span>
                <span className="font-medium">{formatDate(trip.departureDate)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Return:</span>
                <span className="font-medium">{formatDate(trip.returnDate)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Estimated Cost:</span>
                <span className="font-semibold text-primary">{formatCurrency(trip.estimatedCost)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Employee:</span>
                {/* Backend's SimpleUserDTO for `employee` has no `name` field, only username/email/role/id - showing username here rather than a field that's never populated */}
                <span className="font-medium">{trip.employee?.username || "—"}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Approver Manager:</span>
                <span className="font-medium">{trip.approver?.username || "—"}</span>
              </div>
            </div>
          </div>

          {/* Quick Cash Advance request CTA for APPROVED trips */}
          {isOwner && trip.status === "APPROVED" && (
            <div className="p-4 bg-card border border-primary/20 bg-primary/5 rounded-lg shadow-sm space-y-3">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <DollarSign className="h-4 w-4 text-primary" /> Request Travel Advance
              </h2>
              <p className="text-xs text-muted-foreground">
                You can request a cash advance pocket allowance for this approved trip.
              </p>
              <Button onClick={() => navigate(`/advances`)} className="w-full h-8 text-xs">
                Request Advance
              </Button>
            </div>
          )}

          {/* Create Expense Claim CTA for COMPLETED trips */}
          {isOwner && trip.status === "COMPLETED" && (
            <div className="p-4 bg-card border border-purple-200 bg-purple-50 dark:bg-purple-950/20 dark:border-purple-900 rounded-lg shadow-sm space-y-3">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <FileCheck className="h-4 w-4 text-purple-600" /> Create Expense Claim
              </h2>
              <p className="text-xs text-muted-foreground">
                This trip has completed. Create a claim to log your incurred receipts.
              </p>
              <Button onClick={() => navigate(`/expenses`)} className="w-full h-8 text-xs bg-purple-600 hover:bg-purple-700">
                Log Expense Claim
              </Button>
            </div>
          )}
        </div>

        {/* Right column - Itinerary Legs & Visas */}
        <div className="md:col-span-2 space-y-6">
          {/* Itinerary Section */}
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b pb-2">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <Plane className="h-4 w-4 text-muted-foreground" /> Itinerary Legs
              </h2>
              {isTD && (
                <Dialog open={isLegDialogOpen} onOpenChange={setIsLegDialogOpen}>
                  <DialogTrigger asChild>
                    <Button size="sm" variant="outline" className="gap-1 h-7 text-xs">
                      <Plus className="h-3.5 w-3.5" /> Add Leg
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="max-w-md">
                    <DialogHeader>
                      <DialogTitle>Add Itinerary Leg</DialogTitle>
                    </DialogHeader>
                    <form onSubmit={handleAddLeg} className="space-y-3 text-xs">
                      <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                          <Label>Origin</Label>
                          <Input required value={legOrigin} onChange={(e) => setLegOrigin(e.target.value)} />
                        </div>
                        <div className="space-y-1">
                          <Label>Destination</Label>
                          <Input required value={legDest} onChange={(e) => setLegDest(e.target.value)} />
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                          <Label>Leg Type</Label>
                          <select
                            className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                            value={legType}
                            onChange={(e: any) => setLegType(e.target.value)}
                          >
                            <option value="FLIGHT">Flight</option>
                            <option value="TRAIN">Train</option>
                            <option value="HOTEL">Hotel</option>
                            <option value="CAR_RENTAL">Car Rental</option>
                          </select>
                        </div>
                        <div className="space-y-1">
                          <Label>Cost (USD)</Label>
                          <Input type="number" required value={legCost} onChange={(e) => setLegCost(Number(e.target.value))} />
                        </div>
                      </div>

                      <div className="space-y-1">
                        <Label>Travel Date</Label>
                        <Input type="date" required value={legTravelDate} onChange={(e) => setLegTravelDate(e.target.value)} />
                      </div>

                      <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                          <Label>Departure Time</Label>
                          <Input type="time" value={legDepTime} onChange={(e) => setLegDepTime(e.target.value)} />
                        </div>
                        <div className="space-y-1">
                          <Label>Arrival Time</Label>
                          <Input type="time" value={legArrTime} onChange={(e) => setLegArrTime(e.target.value)} />
                        </div>
                      </div>

                      <div className="space-y-1">
                        <Label>Carrier Details / Hotel Name</Label>
                        <Input placeholder="e.g. BA-173 / Marriott" value={legCarrier} onChange={(e) => setLegCarrier(e.target.value)} />
                      </div>

                      <div className="space-y-1">
                        <Label>Proposed Booking Reference</Label>
                        <Input placeholder="e.g. BKREF123" value={legBookingRef} onChange={(e) => setLegBookingRef(e.target.value)} />
                      </div>

                      <div className="flex justify-end gap-2 pt-2">
                        <Button type="button" variant="outline" onClick={() => setIsLegDialogOpen(false)}>
                          Cancel
                        </Button>
                        <Button type="submit">Add Leg</Button>
                      </div>
                    </form>
                  </DialogContent>
                </Dialog>
              )}
            </div>

            <div className="divide-y divide-border">
              {!trip.itineraryLegs || trip.itineraryLegs.length === 0 ? (
                <p className="text-xs text-muted-foreground py-4 text-center">No itinerary legs defined.</p>
              ) : (
                trip.itineraryLegs.map((leg) => (
                  <div key={leg.id} className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
                    <div className="space-y-0.5">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold">{leg.origin} → {leg.destination}</span>
                        <span className="text-[10px] uppercase font-bold text-muted-foreground">({leg.legType})</span>
                      </div>
                      <p className="text-[10px] text-muted-foreground">
                        Date: {formatDate(leg.travelDate)} | Carrier: {leg.carrierDetails || "N/A"}
                      </p>
                      <p className="text-[10px] text-muted-foreground">
                        Booking Ref: <code className="px-1 bg-muted">{leg.bookingRef || leg.bookingReference || "None"}</code>
                      </p>
                    </div>

                    <div className="flex items-center gap-3 ml-auto sm:ml-0">
                      <span className="font-semibold text-primary">{formatCurrency(leg.cost)}</span>
                      <StatusBadge status={leg.bookingStatus || "PENDING"} />

                      {isTD && leg.bookingStatus !== "BOOKED" && (
                        <Button
                          size="sm"
                          onClick={() => {
                            setSelectedLegId(leg.id);
                            // Autofill the leg's defined bookingRef
                            setBookingRefInput(leg.bookingRef || "");
                            setIsBookDialogOpen(true);
                          }}
                        >
                          Book
                        </Button>
                      )}

                      {isTD && (
                        <button
                          onClick={() => deleteLegMutation.mutate(leg.id)}
                          className="text-muted-foreground hover:text-destructive p-1 rounded transition-colors"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Visa Requirements Section (Only displayed for International travel type) */}
          {trip.travelType === "INTERNATIONAL" && (
            <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
              <div className="flex items-center justify-between border-b pb-2">
                <h2 className="text-sm font-semibold flex items-center gap-2">
                  <Bookmark className="h-4 w-4 text-muted-foreground" /> Visa Requirements
                </h2>
                {isTD && (
                  <Dialog open={isVisaDialogOpen} onOpenChange={setIsVisaDialogOpen}>
                    <DialogTrigger asChild>
                      <Button size="sm" variant="outline" className="gap-1 h-7 text-xs">
                        <Plus className="h-3.5 w-3.5" /> Add Visa
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="max-w-md">
                      <DialogHeader>
                        <DialogTitle>Add Visa Requirement</DialogTitle>
                      </DialogHeader>
                      <form onSubmit={handleAddVisa} className="space-y-3 text-xs">
                        <div className="space-y-1">
                          <Label>Country</Label>
                          <Input required placeholder="United States" value={visaCountry} onChange={(e) => setVisaCountry(e.target.value)} />
                        </div>
                        <div className="space-y-1">
                          <Label>Visa Type</Label>
                          <select
                            className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                            value={visaType}
                            onChange={(e) => setVisaType(e.target.value)}
                          >
                            <option value="BUSINESS">Business</option>
                            <option value="TOURIST">Tourist</option>
                            <option value="WORK">Work</option>
                          </select>
                        </div>
                        <div className="space-y-1">
                          <Label>Additional Notes</Label>
                          <textarea
                            className="flex min-h-[60px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm text-foreground"
                            placeholder="Embassy appointments, photo reqs..."
                            value={visaNotes}
                            onChange={(e) => setVisaNotes(e.target.value)}
                          />
                        </div>
                        <div className="flex justify-end gap-2 pt-2">
                          <Button type="button" variant="outline" onClick={() => setIsVisaDialogOpen(false)}>
                            Cancel
                          </Button>
                          <Button type="submit">Save</Button>
                        </div>
                      </form>
                    </DialogContent>
                  </Dialog>
                )}
              </div>

              <div className="divide-y divide-border">
                {!trip.visas || trip.visas.length === 0 ? (
                  <p className="text-xs text-muted-foreground py-4 text-center">No visa documentation required.</p>
                ) : (
                  trip.visas.map((v) => (
                    <div key={v.id} className="py-3 flex items-center justify-between text-xs">
                      <div>
                        <span className="font-semibold">{v.country}</span>
                        <span className="text-[10px] text-muted-foreground ml-2">({v.visaType})</span>
                        {v.notes && <p className="text-[10px] text-muted-foreground mt-0.5">{v.notes}</p>}
                      </div>
                      <div className="flex items-center gap-3">
                        <StatusBadge status={v.status} />
                        {isTD && v.status !== "GRANTED" && (
                          <Button size="sm" variant="outline" className="gap-1 h-7 text-xs text-green-600 hover:text-green-700 hover:bg-green-50" onClick={() => handleResolveVisa(v.id)}>
                            <CheckCircle className="h-3.5 w-3.5" /> Approve
                          </Button>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Book Leg Reference Dialog */}
      <Dialog open={isBookDialogOpen} onOpenChange={setIsBookDialogOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Confirm Booking Reference</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleBookLeg} className="space-y-3 text-xs">
            <p className="text-muted-foreground text-xs">
              Provide the confirmed ticket reference / confirmation ID. Note that it must match the pre-configured booking reference of the itinerary leg.
            </p>
            <div className="space-y-1">
              <Label>Booking Reference</Label>
              <Input
                required
                placeholder="e.g. BKREF123"
                value={bookingRefInput}
                onChange={(e) => setBookingRefInput(e.target.value)}
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => setIsBookDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit">Confirm Booked</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default TripDetails;
