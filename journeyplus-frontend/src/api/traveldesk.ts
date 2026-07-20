import api from "../lib/axios";

export interface TravelDeskMetrics {
  pendingBookings: number;
  completedBookings: number;
  waitingForItinerary: number;
  waitingForVisa: number;
  todaysTravel: number;
  upcomingTravel: number;
  internationalTrips: number;
  domesticTrips: number;
  recentlyCompleted: number;
  flightBookings?: number;
  hotelBookings?: number;
  visaRequests?: number;
  completedItineraries?: number;
}

export interface TravelDeskQueueItem {
  tripId: number;
  id: number;
  employeeName: string;
  department: string;
  destination: string;
  travelType: string;
  departureDate: string;
  returnDate: string;
  purpose: string;
  estimatedCost: number;
  status: string;
  bookingStatus: string;
  visaStatus: string;
}

export const getPendingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  const tripsResponse = await api.get<any[]>("/api/trips", {
    params: { status: "APPROVED" },
  });

  const queueItems: TravelDeskQueueItem[] = [];
  for (const t of tripsResponse.data) {
    let itineraryLegs: any[] = [];
    try {
      const legResp = await api.get(`/api/trips/${t.id}/itinerary`);
      itineraryLegs = legResp.data || [];
    } catch (e) {
      itineraryLegs = [];
    }

    let visaStatus = "NOT_REQUIRED";
    if (t.travelType === "INTERNATIONAL") {
      try {
        const visaResp = await api.get(`/api/trips/${t.id}/visa`);
        const visas = visaResp.data || [];
        if (visas.length > 0) {
          visaStatus = visas[0].status || "PENDING";
        } else {
          visaStatus = "REQUIRED";
        }
      } catch (e) {
        visaStatus = "REQUIRED";
      }
    }

    const hasLegs = itineraryLegs.length > 0;
    const allBooked = hasLegs && itineraryLegs.every((l: any) => l.status === "CONFIRMED");

    // If NO itinerary exists, or not all legs are booked, display in Booking Queue
    if (!hasLegs || !allBooked) {
      queueItems.push({
        tripId: t.id,
        id: t.id,
        employeeName: t.employee?.name || t.employee?.username || "Employee",
        department: "Engineering",
        destination: t.destination,
        travelType: t.travelType,
        departureDate: t.departureDate,
        returnDate: t.returnDate,
        purpose: t.purpose,
        estimatedCost: t.estimatedCost,
        status: t.status,
        bookingStatus: hasLegs ? (allBooked ? "CONFIRMED" : "IN_PROGRESS") : "PENDING",
        visaStatus,
      });
    }
  }

  return queueItems;
};

export const getTravelDeskDashboard = async (): Promise<TravelDeskMetrics> => {
  const allApprovedTripsResp = await api.get<any[]>("/api/trips", {
    params: { status: "APPROVED" },
  });

  let waitingForItinerary = 0;
  let waitingForVisa = 0;
  let completedBookings = 0;

  for (const t of allApprovedTripsResp.data) {
    let legs: any[] = [];
    try {
      const lResp = await api.get(`/api/trips/${t.id}/itinerary`);
      legs = lResp.data || [];
    } catch (e) {
      legs = [];
    }

    if (legs.length === 0) {
      waitingForItinerary++;
    } else if (legs.every((l: any) => l.status === "CONFIRMED")) {
      completedBookings++;
    }

    if (t.travelType === "INTERNATIONAL") {
      try {
        const vResp = await api.get(`/api/trips/${t.id}/visa`);
        const visas = vResp.data || [];
        if (visas.length === 0 || visas.some((v: any) => v.status !== "GRANTED" && v.status !== "APPROVED")) {
          waitingForVisa++;
        }
      } catch (e) {
        waitingForVisa++;
      }
    }
  }

  return {
    pendingBookings: waitingForItinerary,
    completedBookings,
    waitingForItinerary,
    waitingForVisa,
    todaysTravel: 0,
    upcomingTravel: allApprovedTripsResp.data.length,
    internationalTrips: allApprovedTripsResp.data.filter((t: any) => t.travelType === "INTERNATIONAL").length,
    domesticTrips: allApprovedTripsResp.data.filter((t: any) => t.travelType === "DOMESTIC").length,
    recentlyCompleted: completedBookings,
  };
};

export const getUpcomingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  return getPendingBookings();
};

export const addItineraryLeg = async (tripId: number, data: any): Promise<any> => {
  const response = await api.post(`/api/trips/${tripId}/itinerary`, data);
  return response.data;
};

export const updateItineraryLeg = async (tripId: number, legId: number, data: any): Promise<any> => {
  const response = await api.put(`/api/itinerary/${legId}`, data);
  return response.data;
};

export const deleteItineraryLeg = async (tripId: number, legId: number): Promise<any> => {
  const response = await api.delete(`/api/itinerary/${legId}`);
  return response.data;
};

export const bookItineraryLeg = async (tripId: number, legId: number, data: any): Promise<any> => {
  const response = await api.post(`/api/trips/${tripId}/legs/${legId}/book`, data);
  return response.data;
};

export const addVisaRequirement = async (tripId: number, data: any): Promise<any> => {
  const response = await api.post(`/api/trips/${tripId}/visa`, data);
  return response.data;
};

export const updateVisaRequirement = async (tripId: number, visaId: number, data: any): Promise<any> => {
  const response = await api.put(`/api/visa/${visaId}`, data);
  return response.data;
};

export const confirmBooking = async (tripId: number, comments?: string): Promise<any> => {
  const response = await api.post(`/api/trips/${tripId}/complete`);
  return response.data;
};

export const getTripTravelDetails = async (tripId: number): Promise<any> => {
  const tripResp = await api.get(`/api/trips/${tripId}`);
  const legsResp = await api.get(`/api/trips/${tripId}/itinerary`);
  const visasResp = await api.get(`/api/trips/${tripId}/visa`);

  const legs = legsResp.data || [];
  const visas = visasResp.data || [];
  const trip = tripResp.data;

  const hasLegs = legs.length > 0;
  const allConfirmed = hasLegs && legs.every((l: any) => l.status === "CONFIRMED");
  const pnr = legs.find((l: any) => l.bookingRef)?.bookingRef || "PNR-PENDING";

  return {
    trip,
    itineraryLegs: legs,
    visaRequirements: visas,
    bookingStatus: allConfirmed ? "CONFIRMED" : (hasLegs ? "IN_PROGRESS" : "PENDING"),
    travelRemarks: trip.comments || "No remarks.",
    pnr,
    bookingRef: pnr,
    ticketNumber: `TKT-${tripId}-${tripId * 100}`,
  };
};
