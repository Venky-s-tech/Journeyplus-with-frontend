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
  const response = await api.get<any>("/api/dashboard/summary", {
    params: { role: "TRAVEL_DESK" },
  });
  return {
    pendingBookings: response.data?.pendingBookings || 0,
    completedBookings: response.data?.completedItineraries || 0,
    waitingForItinerary: response.data?.pendingBookings || 0,
    waitingForVisa: response.data?.visaRequests || 0,
    todaysTravel: 0,
    upcomingTravel: response.data?.upcomingTrips || 0,
    internationalTrips: response.data?.travelRequests || 0,
    domesticTrips: 0,
    recentlyCompleted: response.data?.completedItineraries || 0,
    flightBookings: response.data?.flightBookings || 0,
    hotelBookings: response.data?.hotelBookings || 0,
    visaRequests: response.data?.visaRequests || 0,
    completedItineraries: response.data?.completedItineraries || 0,
  };
};

export const getPendingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  const response = await api.get<any[]>("/api/trips", {
    params: { status: "APPROVED" },
  });
  return response.data.map((t: any) => ({
    tripId: t.id,
    id: t.id,
    employeeName: t.employee?.username || t.employee?.name || "Employee",
    department: "Engineering",
    destination: t.destination,
    travelType: t.travelType,
    departureDate: t.departureDate,
    returnDate: t.returnDate,
    purpose: t.purpose,
    estimatedCost: t.estimatedCost,
    status: t.status,
    bookingStatus: t.bookingStatus || "PENDING_BOOKING",
    visaStatus: t.travelType === "INTERNATIONAL" ? "REQUIRED" : "NOT_REQUIRED",
  }));
};

export const getUpcomingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  const response = await api.get<any[]>("/api/trips", {
    params: { status: "APPROVED" },
  });
  return response.data.map((t: any) => ({
    tripId: t.id,
    id: t.id,
    employeeName: t.employee?.username || t.employee?.name || "Employee",
    department: "Engineering",
    destination: t.destination,
    travelType: t.travelType,
    departureDate: t.departureDate,
    returnDate: t.returnDate,
    purpose: t.purpose,
    estimatedCost: t.estimatedCost,
    status: t.status,
    bookingStatus: t.bookingStatus || "PENDING_BOOKING",
    visaStatus: t.travelType === "INTERNATIONAL" ? "REQUIRED" : "NOT_REQUIRED",
  }));
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
  const response = await api.get(`/api/trips/${tripId}/travel-details`);
  return response.data;
};
