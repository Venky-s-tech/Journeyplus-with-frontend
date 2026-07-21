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

export const getTravelDeskDashboard = async (): Promise<TravelDeskMetrics> => {
  const response = await api.get<any>("/api/travel-desk/dashboard");
  const data = response.data || {};
  return {
    pendingBookings: data.pendingBookings || 0,
    completedBookings: data.completedBookings || 0,
    waitingForItinerary: data.waitingForItinerary || 0,
    waitingForVisa: data.waitingForVisa || 0,
    todaysTravel: 0,
    upcomingTravel: data.upcomingTrips || 0,
    internationalTrips: data.travelRequests || 0,
    domesticTrips: 0,
    recentlyCompleted: data.completedBookings || 0,
    flightBookings: data.flightBookings || 0,
    hotelBookings: data.hotelBookings || 0,
    visaRequests: data.waitingForVisa || 0,
    completedItineraries: data.completedBookings || 0,
  };
};

export const getPendingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  const response = await api.get<any[]>("/api/travel-desk/queue");
  return (response.data || []).map((t: any) => ({
    tripId: t.tripId || t.id,
    id: t.id || t.tripId,
    employeeName: t.employeeName || "Employee",
    department: t.department || "General",
    destination: t.destination,
    travelType: t.travelType,
    departureDate: t.departureDate,
    returnDate: t.returnDate,
    purpose: t.purpose,
    estimatedCost: t.estimatedCost,
    status: t.status,
    bookingStatus: t.bookingStatus || "PENDING_BOOKING",
    visaStatus: t.visaStatus || "NOT_REQUIRED",
  }));
};

export const getUpcomingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  return getPendingBookings();
};

export const addItineraryLeg = async (tripId: number, data: any): Promise<any> => {
  const response = await api.post("/api/travel-desk/itinerary", data, {
    params: { tripId },
  });
  return response.data;
};

export const updateItineraryLeg = async (tripId: number, legId: number, data: any): Promise<any> => {
  const response = await api.put(`/api/travel-desk/itinerary/${legId}`, data);
  return response.data;
};

export const deleteItineraryLeg = async (tripId: number, legId: number): Promise<any> => {
  const response = await api.delete(`/api/travel-desk/itinerary/${legId}`);
  return response.data;
};

export const bookItineraryLeg = async (tripId: number, legId: number, data: any): Promise<any> => {
  const response = await api.put(`/api/travel-desk/itinerary/${legId}`, data);
  return response.data;
};

export const addVisaRequirement = async (tripId: number, data: any): Promise<any> => {
  const response = await api.post("/api/travel-desk/visa", data, {
    params: { tripId },
  });
  return response.data;
};

export const updateVisaRequirement = async (tripId: number, visaId: number, data: any): Promise<any> => {
  const response = await api.put("/api/travel-desk/visa/status", {
    visaId,
    status: data.status,
    notes: data.notes,
  });
  return response.data;
};

export const confirmBooking = async (tripId: number, comments?: string): Promise<any> => {
  const response = await api.post(`/api/travel-desk/booking/confirm/${tripId}`, null, {
    params: { comments },
  });
  return response.data;
};

export const rejectTripBack = async (tripId: number, comments?: string): Promise<any> => {
  const response = await api.post(`/api/travel-desk/reject/${tripId}`, null, {
    params: { comments },
  });
  return response.data;
};

export const getTripTravelDetails = async (tripId: number): Promise<any> => {
  const response = await api.get(`/api/trips/${tripId}/travel-details`);
  return response.data;
};
