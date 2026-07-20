import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  useTripTravelDetails,
  useAddTravelDeskItinerary,
  useAddTravelDeskVisa,
  useConfirmBooking,
} from "../../hooks/useTravelDesk";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import { Button } from "../../components/ui/button";
import {
  Plane,
  Building,
  Globe,
  CheckCircle,
  Plus,
  ArrowLeft,
  FileCheck,
  Receipt,
} from "lucide-react";

export const TravelDeskDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const tripId = Number(id);
  const navigate = useNavigate();

  const { data: details, isLoading } = useTripTravelDetails(tripId);
  const addItineraryMutation = useAddTravelDeskItinerary(tripId);
  const addVisaMutation = useAddTravelDeskVisa(tripId);
  const confirmBookingMutation = useConfirmBooking();

  // Itinerary form state
  const [legType, setLegType] = useState("FLIGHT");
  const [origin, setOrigin] = useState("");
  const [destination, setDestination] = useState("");
  const [carrierDetails, setCarrierDetails] = useState("");
  const [bookingRef, setBookingRef] = useState("");
  const [cost, setCost] = useState("");

  // Visa form state
  const [visaType, setVisaType] = useState("BUSINESS");
  const [country, setCountry] = useState("");
  const [visaNotes, setVisaNotes] = useState("");
  const [visaStatus, setVisaStatus] = useState("ISSUED");

  // Confirmation comments
  const [confirmComments, setConfirmComments] = useState("");

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
        <p className="text-sm text-muted-foreground">Trip request not found.</p>
        <Button onClick={() => navigate("/travel-desk")}>Return to Queue</Button>
      </div>
    );
  }

  const trip = details.trip;
  const legs = details.itineraryLegs || [];
  const visas = details.visaRequirements || [];
  const isInternational = trip.travelType === "INTERNATIONAL";

  const handleAddLeg = (e: React.FormEvent) => {
    e.preventDefault();
    addItineraryMutation.mutate({
      legType,
      origin: origin || "Origin City",
      destination: destination || trip.destination,
      carrierDetails: carrierDetails || (legType === "FLIGHT" ? "Flight #AI-101" : "Hotel Booking"),
      bookingRef: bookingRef || "REF-" + Math.floor(Math.random() * 900000 + 100000),
      cost: Number(cost) || 250,
      travelDate: trip.departureDate,
    });
    setOrigin("");
    setDestination("");
    setCarrierDetails("");
    setBookingRef("");
    setCost("");
  };

  const handleAddVisa = (e: React.FormEvent) => {
    e.preventDefault();
    addVisaMutation.mutate({
      visaType,
      country: country || trip.destination,
      status: visaStatus,
      notes: visaNotes || "Visa issued and verified.",
    });
    setCountry("");
    setVisaNotes("");
  };

  const handleConfirmBooking = () => {
    confirmBookingMutation.mutate(
      { tripId, comments: confirmComments || "All flights, hotels, and visas confirmed by Travel Desk." },
      {
        onSuccess: () => {
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
        <StatusBadge status={details.bookingStatus || "PENDING"} />
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
            <span className="font-bold text-foreground">Employee:</span> {trip.employee?.name || "Corporate Traveller"}
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
                  <div key={leg.id} className="py-3 space-y-1">
                    <div className="flex items-center justify-between text-xs font-semibold">
                      <span>{leg.legType || leg.travelMode}: {leg.origin || leg.departureCity} ➔ {leg.destination || leg.arrivalCity}</span>
                      <StatusBadge status={leg.status || "CONFIRMED"} />
                    </div>
                    <div className="text-[11px] text-muted-foreground flex justify-between">
                      <span>{leg.carrierDetails || "Carrier Info"} (PNR: {leg.bookingRef || leg.bookingReference || "N/A"})</span>
                      <span className="font-semibold text-foreground">{formatCurrency(leg.cost || leg.estimatedCost)}</span>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Add Leg Form */}
            <form onSubmit={handleAddLeg} className="pt-4 border-t border-border space-y-3">
              <h3 className="text-xs font-bold uppercase text-muted-foreground">Add Flight / Hotel Leg</h3>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <select value={legType} onChange={(e) => setLegType(e.target.value)} className="p-2 border rounded bg-background">
                  <option value="FLIGHT">Flight</option>
                  <option value="HOTEL">Hotel</option>
                  <option value="TRAIN">Train</option>
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
                  placeholder="Origin"
                  value={origin}
                  onChange={(e) => setOrigin(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
                <input
                  type="text"
                  placeholder="Destination"
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                  className="p-2 border rounded bg-background"
                />
                <input
                  type="text"
                  placeholder="PNR / Booking Ref"
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
              <Button type="submit" size="sm" className="w-full gap-1 text-xs" disabled={addItineraryMutation.isPending}>
                <Plus className="h-3 w-3" /> Add Itinerary Item
              </Button>
            </form>
          </div>
        </div>

        {/* Right Column: Visa & Booking Confirmation */}
        <div className="space-y-6">
          {/* Visa Form */}
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold flex items-center gap-2">
              <Globe className="h-4 w-4 text-purple-600" /> Visa Requirement ({isInternational ? "International Trip" : "Domestic"})
            </h2>

            <div className="divide-y divide-border">
              {visas.length === 0 ? (
                <p className="text-xs text-muted-foreground py-2 text-center">
                  {isInternational ? "No visa details updated yet." : "Visa not required for domestic travel."}
                </p>
              ) : (
                visas.map((v: any) => (
                  <div key={v.id} className="py-2 flex items-center justify-between text-xs">
                    <div>
                      <span className="font-semibold">{v.country} ({v.visaType})</span>
                      <p className="text-[10px] text-muted-foreground">{v.notes || "No notes"}</p>
                    </div>
                    <StatusBadge status={v.status || "ISSUED"} />
                  </div>
                ))
              )}
            </div>

            {isInternational && (
              <form onSubmit={handleAddVisa} className="pt-2 border-t border-border space-y-3">
                <h3 className="text-xs font-bold uppercase text-muted-foreground">Update Visa Status</h3>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <input
                    type="text"
                    placeholder="Country"
                    value={country}
                    onChange={(e) => setCountry(e.target.value)}
                    className="p-2 border rounded bg-background"
                  />
                  <select value={visaStatus} onChange={(e) => setVisaStatus(e.target.value)} className="p-2 border rounded bg-background">
                    <option value="ISSUED">ISSUED</option>
                    <option value="APPLIED">APPLIED</option>
                    <option value="NOT_REQUIRED">NOT_REQUIRED</option>
                    <option value="REJECTED">REJECTED</option>
                  </select>
                </div>
                <input
                  type="text"
                  placeholder="Visa Remarks / Notes"
                  value={visaNotes}
                  onChange={(e) => setVisaNotes(e.target.value)}
                  className="w-full p-2 border rounded bg-background text-xs"
                />
                <Button type="submit" size="sm" variant="outline" className="w-full gap-1 text-xs" disabled={addVisaMutation.isPending}>
                  <Plus className="h-3 w-3" /> Update Visa Record
                </Button>
              </form>
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
            <Button
              onClick={handleConfirmBooking}
              className="w-full gap-2 text-xs bg-green-600 hover:bg-green-700 text-white"
              disabled={confirmBookingMutation.isPending}
            >
              <FileCheck className="h-4 w-4" /> Confirm Booking & Notify Employee
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TravelDeskDetails;
