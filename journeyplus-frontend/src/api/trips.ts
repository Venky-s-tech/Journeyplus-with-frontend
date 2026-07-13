import api from "../lib/axios";
import { TripRequest, ItineraryLeg, VisaRequirement } from "../types";

export const getTrips = async (): Promise<TripRequest[]> => {
  const response = await api.get<TripRequest[]>("/api/trips");
  return response.data;
};

export const getMyTrips = async (): Promise<TripRequest[]> => {
  const response = await api.get<TripRequest[]>("/api/trips/my-trips");
  return response.data;
};

export const getPendingTripApprovals = async (): Promise<TripRequest[]> => {
  const response = await api.get<TripRequest[]>("/api/trips/pending-approvals");
  return response.data;
};

export const getTripDetails = async (id: number): Promise<TripRequest> => {
  const response = await api.get<TripRequest>(`/api/trips/${id}`);
  return response.data;
};

export const getTripSummary = async (id: number): Promise<any> => {
  const response = await api.get(`/api/trips/${id}/summary`);
  return response.data;
};

export const createTrip = async (data: Partial<TripRequest>): Promise<TripRequest> => {
  const response = await api.post<TripRequest>("/api/trips", data);
  return response.data;
};

export const updateTrip = async (id: number, data: Partial<TripRequest>): Promise<TripRequest> => {
  const response = await api.put<TripRequest>(`/api/trips/${id}`, data);
  return response.data;
};

export const submitTrip = async (id: number): Promise<TripRequest> => {
  const response = await api.post<TripRequest>(`/api/trips/${id}/submit`);
  return response.data;
};

export const approveTrip = async (id: number, comments?: string): Promise<TripRequest> => {
  const response = await api.post<TripRequest>(
    `/api/trips/${id}/approve`,
    null,
    { params: { comments } }
  );
  return response.data;
};

export const rejectTrip = async (id: number, comments?: string): Promise<TripRequest> => {
  const response = await api.post<TripRequest>(
    `/api/trips/${id}/reject`,
    null,
    { params: { comments } }
  );
  return response.data;
};

export const completeTrip = async (id: number): Promise<TripRequest> => {
  const response = await api.post<TripRequest>(`/api/trips/${id}/complete`);
  return response.data;
};

export const cancelTrip = async (id: number): Promise<TripRequest> => {
  const response = await api.post<TripRequest>(`/api/trips/${id}/cancel`);
  return response.data;
};

// Itinerary Legs
export const getItineraryLegs = async (tripId: number): Promise<ItineraryLeg[]> => {
  const response = await api.get<ItineraryLeg[]>(`/api/trips/${tripId}/itinerary`);
  return response.data;
};

export const addItineraryLeg = async (tripId: number, data: Partial<ItineraryLeg>): Promise<ItineraryLeg> => {
  const response = await api.post<ItineraryLeg>(`/api/trips/${tripId}/itinerary`, data);
  return response.data;
};

export const updateItineraryLeg = async (legId: number, data: Partial<ItineraryLeg>): Promise<ItineraryLeg> => {
  const response = await api.put<ItineraryLeg>(`/api/itinerary/${legId}`, data);
  return response.data;
};

export const deleteItineraryLeg = async (legId: number): Promise<any> => {
  const response = await api.delete(`/api/itinerary/${legId}`);
  return response.data;
};

export const bookItineraryLeg = async (
  tripId: number,
  legId: number,
  data: { bookingReference: string; bookingStatus: string }
): Promise<any> => {
  const response = await api.post(`/api/trips/${tripId}/legs/${legId}/book`, data);
  return response.data;
};

// Visa Requirements
export const getVisaRequirements = async (tripId: number): Promise<VisaRequirement[]> => {
  const response = await api.get<VisaRequirement[]>(`/api/trips/${tripId}/visa`);
  return response.data;
};

export const addVisaRequirement = async (tripId: number, data: Partial<VisaRequirement>): Promise<VisaRequirement> => {
  const response = await api.post<VisaRequirement>(`/api/trips/${tripId}/visa`, data);
  return response.data;
};

export const updateVisaRequirement = async (visaId: number, data: Partial<VisaRequirement>): Promise<VisaRequirement> => {
  const response = await api.put<VisaRequirement>(`/api/visa/${visaId}`, data);
  return response.data;
};

export const updateVisaOnTrip = async (
  tripId: number,
  visaId: number,
  data: Partial<VisaRequirement>
): Promise<any> => {
  const response = await api.post(`/api/trips/${tripId}/visas/${visaId}`, data);
  return response.data;
};
