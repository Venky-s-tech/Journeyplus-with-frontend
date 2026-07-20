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
  const response = await api.get<TravelDeskMetrics>("/api/travel-desk/dashboard");
  return response.data;
};

export const getPendingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  const response = await api.get<TravelDeskQueueItem[]>("/api/travel-desk/bookings");
  return response.data;
};

export const getUpcomingBookings = async (): Promise<TravelDeskQueueItem[]> => {
  const response = await api.get<TravelDeskQueueItem[]>("/api/travel-desk/upcoming");
  return response.data;
};

export const addItineraryLeg = async (tripId: number, data: any): Promise<any> => {
  const response = await api.post(`/api/travel-desk/${tripId}/itinerary`, data);
  return response.data;
};

export const updateItineraryLeg = async (tripId: number, legId: number, data: any): Promise<any> => {
  const response = await api.put(`/api/travel-desk/${tripId}/itinerary/${legId}`, data);
  return response.data;
};

export const deleteItineraryLeg = async (tripId: number, legId: number): Promise<any> => {
  const response = await api.delete(`/api/travel-desk/${tripId}/itinerary/${legId}`);
  return response.data;
};

export const addVisaRequirement = async (tripId: number, data: any): Promise<any> => {
  const response = await api.post(`/api/travel-desk/${tripId}/visa`, data);
  return response.data;
};

export const updateVisaRequirement = async (tripId: number, visaId: number, data: any): Promise<any> => {
  const response = await api.put(`/api/travel-desk/${tripId}/visa/${visaId}`, data);
  return response.data;
};

export const confirmBooking = async (tripId: number, comments?: string): Promise<any> => {
  const response = await api.post(`/api/travel-desk/${tripId}/confirm`, { comments });
  return response.data;
};

export const getTripTravelDetails = async (tripId: number): Promise<any> => {
  const response = await api.get(`/api/trips/${tripId}/travel-details`);
  return response.data;
};
