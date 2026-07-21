import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  useTripTravelDetails,
  useAddTravelDeskItinerary,
  useUpdateTravelDeskItinerary,
  useDeleteTravelDeskItinerary,
  useAddTravelDeskVisa,
  useUpdateTravelDeskVisaStatus,
  useConfirmBooking,
  useRejectTripBack,
} from "../../hooks/useTravelDesk";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Button } from "../../components/ui/button";
import { useToast } from "../../components/ui/toast";
import {
  Plane,
  Building,
  Globe,
  CheckCircle,
  Plus,
  ArrowLeft,
  FileCheck,
  Trash2,
  Pencil,
  XCircle,
} from "lucide-react";

export const TravelDeskDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const tripId = Number(id);
  const navigate = useNavigate();
  const { toast } = useToast();

  const { data: details, isLoading } = useTripTravelDetails(tripId);
  const addItineraryMutation = useAddTravelDeskItinerary(tripId);
  const updateItineraryMutation = useUpdateTravelDeskItinerary(tripId);
  const deleteItineraryMutation = useDeleteTravelDeskItinerary(tripId);
  const addVisaMutation = useAddTravelDeskVisa(tripId);
  const updateVisaStatusMutation = useUpdateTravelDeskVisaStatus(tripId);
  const confirmBookingMutation = useConfirmBooking();
  const rejectTripBackMutation = useRejectTripBack();

  // Itinerary form state
  const [editingLegId, setEditingLegId] = useState<number | null>(null);
  const [legType, setLegType] = useState("FLIGHT");
  const [origin, setOrigin] = useState("");
  const [destination, setDestination] = useState("");
  const [carrierDetails, setCarrierDetails] = useState("");
  const [bookingRef, setBookingRef] = useState("");
  const [cost, setCost] = useState("");

  // Visa form state
  const [country, setCountry] = useState("");
  const [visaType, setVisaType] = useState("BUSINESS");
  const [visaNotes, setVisaNotes] = useState("");

  // Comments
  const [confirmComments, setConfirmComments] = useState("");
  const [rejectComments, setRejectComments] = useState("");

  if (isLoading) {
    return (
      <div className="p-8 text-center text-sm text-muted-foreground">
        Loading travel desk request details...
      </div>
    );
  }

  if (!details || !details.trip) {
    return (
      <div className="p-8 text-center space-y-4">
        <p className="text-sm text-destructive">Trip request not found.</p>
        <Button onClick={() => navigate("/travel-desk")}>Return to Queue</Button>
      </div>
    );
  }

  const trip = details.trip;
  const legs = details.itineraryLegs || [];
  const visas = details.visaRequirements || [];
  const isInternational = trip.travelType === "INTERNATIONAL";

  const handleOpenAddLeg = () => {
    setEditingLegId(null);
    setLegType("FLIGHT");
    setOrigin("");
    setDestination(trip.destination);
    setCarrierDetails("");
    setBookingRef("");
    setCost("");
  };

  const handleOpenEditLeg = (leg: any) => {
    setEditingLegId(leg.id);
    setLegType(leg.legType || "FLIGHT");
    setOrigin(leg.origin || "");
    setDestination(leg.destination || "");
    setCarrierDetails(leg.carrierDetails || "");
    setBookingRef(leg.bookingRef || leg.bookingReference || "");
    setCost(leg.cost != null ? String(leg.cost) : "");
  };

  const handleSaveLeg = (e: React.FormEvent) => {
    e.preventDefault();
    const legData = {
      legType,
      origin: origin || "Origin City",
      destination: destination || trip.destination,
      carrierDetails: carrierDetails || (legType === "FLIGHT" ? "Flight Airline" : "Hotel Booking"),
      bookingRef: bookingRef || "REF-" + Math.floor(Math.random() * 900000 + 100000),
      cost: Number(cost) || 250,
      travelDate: trip.departureDate,
      status: "CONFIRMED",
    };

    if (editingLegId) {
      updateItineraryMutation.mutate(
        { legId: editingLegId, data: legData },
        {
          onSuccess: () => {
            toast("Itinerary item updated", "success", "Updated");
            handleOpenAddLeg();
          },
          onError: (err: any) => {
            toast(err.response?.data?.message || "Failed to update item", "error");
          },
        }
      );
    } else {
      addItineraryMutation.mutate(legData, {
        onSuccess: () => {
          toast("Itinerary item added", "success", "Added");
          handleOpenAddLeg();
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Failed to add item", "error");
        },
      });
    }
  };

  const handleDeleteLeg = (legId: number) => {
    if (confirm("Delete this itinerary leg?")) {
      deleteItineraryMutation.mutate(legId, {
        onSuccess: () => {
          toast("Itinerary leg deleted", "success", "Deleted");
        },
      });
    }
  };

  const handleAddVisa = (e: React.FormEvent) => {
    e.preventDefault();
    addVisaMutation.mutate(
      {
        visaType,
        country: country || trip.destination,
        requiresVisa: true,
        status: "PENDING",
        notes: visaNotes || "Visa application created",
      },
      {
        onSuccess: () => {
          toast("Visa requirement added", "success", "Added");
          setCountry("");
          setVisaNotes("");
        },
      }
    );
  };

  const handleUpdateVisaStatus = (visaId: number, status: string) => {
    updateVisaStatusMutation.mutate(
      { visaId, status, notes: `Visa status updated to ${status}` },
      {
        onSuccess: () => {
          toast(`Visa status updated to ${status}`, "success", "Updated");
        },
      }
    );
  };

  const handleConfirmBooking = () => {
    confirmBookingMutation.mutate(
      { tripId, comments: confirmComments || "All flights, hotels, and travel details confirmed by Travel Desk." },
      {
        onSuccess: () => {
          toast("Booking confirmed successfully", "success", "Confirmed");
          navigate("/travel-desk");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Failed to confirm booking", "error");
        },
      }
    );
  };

  const handleRejectBack = () => {
    if (!rejectComments) {
      toast("Please enter a reason for rejecting back", "error");
      return;
    }
    rejectTripBackMutation.mutate(
      { tripId, comments: rejectComments },
      {
        onSuccess: () => {
          toast("Trip rejected back", "success", "Rejected");
          navigate("/travel-desk");
        },
      }
    );
  };

  return (
    <div className="space-y-6 animate-in fade-in-50 duration-200">
      <div className="flex items-center justify-between">
        <Button variant="ghost" size="sm" onClick={() => navigate("/travel-desk")} className="gap-2 text-xs">
          <ArrowLeft className="h-4 w-4" /> Back to Travel Desk Queue
        </Button>
        <StatusBadge status={details.bookingStatus || trip.status || "APPROVED"} />
      </div>

      {/* Header Banner */}
      <div className="p-6 bg-card border border-border rounded-lg shadow-sm space-y-2">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-bold">Trip #{trip.id}: {trip.destination}</h1>
          <span className="text-xs uppercase font-semibold px-2 py-1 bg-primary/10 text-primary rounded-full">
            {trip.travelType}
          </span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 text-xs text-muted-foreground pt-2 border-t border-border">
          <div>
            <span className="font-bold text-foreground">Employee:</span> {trip.employee?.username || trip.employee?.name || "Corporate Traveller"}
          </div>
          <div>
            <span className="font-bold text-foreground">Purpose:</span> {trip.purpose}
          </div>
          <div>
            <span className="font-bold text-foreground">Dates:</span> {formatDate(trip.departureDate)} - {formatDate(trip.returnDate)}
          </div>
          <div>
            <span className="font-bold text-foreground">Estimated Cost:</span> {formatCurrency(trip.estimatedCost)}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Column: Itinerary Management */}
        <div className="space-y-6">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold flex items-center gap-2">
              <Plane className="h-4 w-4 text-primary" /> Issued Itinerary Legs
            </h2>
            <div className="divide-y divide-border">
              {legs.length === 0 ? (
                <p className="text-xs text-muted-foreground py-4 text-center">No itinerary legs added yet.</p>
              ) : (
                legs.map((leg: any) => (
                  <div key={leg.id} className="py-3 flex items-center justify-between text-xs">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2 font-semibold">
                        <span>{leg.legType || leg.travelMode}: {leg.origin || leg.departureCity} ➔ {leg.destination || leg.arrivalCity}</span>
                        <StatusBadge status={leg.status || "CONFIRMED"} />
                      </div>
                      <div className="text-[11px] text-muted-foreground flex gap-3">
                        <span>Ref: {leg.bookingRef || leg.bookingReference || "N/A"}</span>
                        <span>Carrier: {leg.carrierDetails || "—"}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-semibold text-foreground">{formatCurrency(leg.cost || leg.estimatedCost)}</span>
                      <Button size="sm" variant="ghost" className="h-7 w-7 p-0" onClick={() => handleOpenEditLeg(leg)}>
                        <Pencil className="h-3.5 w-3.5" />
                      </Button>
                      <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-destructive" onClick={() => handleDeleteLeg(leg.id)}>
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Add / Edit Leg Form */}
            <form onSubmit={handleSaveLeg} className="pt-4 border-t border-border space-y-3">
              <h3 className="text-xs font-bold uppercase text-muted-foreground">
                {editingLegId ? "Edit Itinerary Item" : "Add Flight / Hotel / Transport Leg"}
              </h3>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <select value={legType} onChange={(e) => setLegType(e.target.value)} className="p-2 border rounded bg-background">
                  <option value="FLIGHT">Flight</option>
                  <option value="HOTEL">Hotel</option>
                  <option value="TRAIN">Train</option>
                  <option value="BUS">Bus</option>
                  <option value="CAR_RENTAL">Local Transport / Taxi</option>
                </select>
                <input
                  type="text"
                  placeholder="Carrier / Hotel Name"
                  value={carrierDetails}
                  onChange={(e) => setCarrierDetails(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
                <input
                  type="text"
                  placeholder="Origin City"
                  value={origin}
                  onChange={(e) => setOrigin(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
                <input
                  type="text"
                  placeholder="Destination City"
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
                <input
                  type="text"
                  placeholder="PNR / Booking Reference"
                  value={bookingRef}
                  onChange={(e) => setBookingRef(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
                <input
                  type="number"
                  placeholder="Cost (USD)"
                  value={cost}
                  onChange={(e) => setCost(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
              </div>
              <div className="flex gap-2">
                {editingLegId && (
                  <Button type="button" variant="outline" size="sm" className="flex-1 text-xs" onClick={handleOpenAddLeg}>
                    Cancel Edit
                  </Button>
                )}
                <Button type="submit" size="sm" className="flex-1 gap-1 text-xs" disabled={addItineraryMutation.isPending || updateItineraryMutation.isPending}>
                  <Plus className="h-3 w-3" /> {editingLegId ? "Update Itinerary Leg" : "Save Itinerary Leg"}
                </Button>
              </div>
            </form>
          </div>
        </div>

        {/* Right Column: Visa & Booking Confirmation */}
        <div className="space-y-6">
          {/* Visa Queue Section */}
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold flex items-center gap-2">
              <Globe className="h-4 w-4 text-purple-600" /> Visa Queue ({isInternational ? "International Trip" : "Domestic Trip"})
            </h2>

            {!isInternational ? (
              <div className="p-3 bg-muted/20 border rounded-md text-xs text-muted-foreground text-center">
                Domestic travel request — Visa verification not required.
              </div>
            ) : (
              <>
                <div className="divide-y divide-border">
                  {visas.length === 0 ? (
                    <p className="text-xs text-muted-foreground py-2 text-center">No visa records found for this international trip.</p>
                  ) : (
                    visas.map((v: any) => (
                      <div key={v.id} className="py-2.5 flex flex-col gap-2 text-xs">
                        <div className="flex items-center justify-between">
                          <div>
                            <span className="font-bold text-foreground">{v.country} ({v.visaType || "BUSINESS"})</span>
                            {v.notes && <p className="text-[10px] text-muted-foreground">{v.notes}</p>}
                          </div>
                          <StatusBadge status={v.status || "PENDING"} />
                        </div>

                        {/* Visa Action Buttons */}
                        <div className="flex flex-wrap gap-1.5 pt-1 border-t border-border/50">
                          <Button size="sm" variant="outline" className="h-6 text-[10px] px-2" onClick={() => handleUpdateVisaStatus(v.id, "APPLIED")}>
                            Mark Applied
                          </Button>
                          <Button size="sm" className="h-6 text-[10px] px-2 bg-green-600 hover:bg-green-700" onClick={() => handleUpdateVisaStatus(v.id, "GRANTED")}>
                            Grant Visa
                          </Button>
                          <Button size="sm" variant="destructive" className="h-6 text-[10px] px-2" onClick={() => handleUpdateVisaStatus(v.id, "REJECTED")}>
                            Reject Visa
                          </Button>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                <form onSubmit={handleAddVisa} className="pt-2 border-t border-border space-y-3">
                  <h3 className="text-xs font-bold uppercase text-muted-foreground">Add Additional Visa Requirement</h3>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <input
                      type="text"
                      placeholder="Country"
                      value={country}
                      onChange={(e) => setCountry(e.target.value)}
                      className="p-2 border rounded bg-background"
                    />
                    <select value={visaType} onChange={(e) => setVisaType(e.target.value)} className="p-2 border rounded bg-background">
                      <option value="BUSINESS">Business Visa</option>
                      <option value="TOURIST">Tourist Visa</option>
                      <option value="TRANSIT">Transit Visa</option>
                    </select>
                  </div>
                  <input
                    type="text"
                    placeholder="Notes / Visa File reference"
                    value={visaNotes}
                    onChange={(e) => setVisaNotes(e.target.value)}
                    className="w-full p-2 border rounded bg-background text-xs"
                  />
                  <Button type="submit" size="sm" variant="outline" className="w-full gap-1 text-xs" disabled={addVisaMutation.isPending}>
                    <Plus className="h-3 w-3" /> Create Visa Record
                  </Button>
                </form>
              </>
            )}
          </div>

          {/* Confirm Booking Box */}
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold flex items-center gap-2">
              <CheckCircle className="h-4 w-4 text-green-600" /> Confirm Final Travel Booking
            </h2>
            <textarea
              placeholder="Enter travel remarks for employee (e.g. Flight ticket attached, hotel vouchers sent to email)..."
              value={confirmComments}
              onChange={(e) => setConfirmComments(e.target.value)}
              className="w-full p-2 border rounded bg-background text-xs h-20"
            />
            <div className="flex gap-2">
              <Button
                onClick={handleConfirmBooking}
                className="flex-1 gap-2 text-xs bg-green-600 hover:bg-green-700 text-white"
                disabled={confirmBookingMutation.isPending || legs.length === 0}
              >
                <FileCheck className="h-4 w-4" /> Confirm Booking & Notify Employee
              </Button>
            </div>
          </div>

          {/* Reject Back Box */}
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-3">
            <h2 className="text-sm font-semibold flex items-center gap-2 text-destructive">
              <XCircle className="h-4 w-4" /> Reject Back to Manager
            </h2>
            <input
              type="text"
              placeholder="Reason for rejecting back..."
              value={rejectComments}
              onChange={(e) => setRejectComments(e.target.value)}
              className="w-full p-2 border rounded bg-background text-xs"
            />
            <Button
              variant="destructive"
              size="sm"
              onClick={handleRejectBack}
              className="w-full text-xs"
              disabled={rejectTripBackMutation.isPending}
            >
              Reject Back
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TravelDeskDetails;
