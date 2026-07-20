import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as travelDeskApi from "../api/traveldesk";

export const useTravelDeskDashboard = () => {
  return useQuery({
    queryKey: ["traveldesk-dashboard"],
    queryFn: travelDeskApi.getTravelDeskDashboard,
    staleTime: 30000,
  });
};

export const usePendingBookings = () => {
  return useQuery({
    queryKey: ["traveldesk-bookings"],
    queryFn: travelDeskApi.getPendingBookings,
    staleTime: 15000,
  });
};

export const useUpcomingBookings = () => {
  return useQuery({
    queryKey: ["traveldesk-upcoming"],
    queryFn: travelDeskApi.getUpcomingBookings,
    staleTime: 15000,
  });
};

export const useTripTravelDetails = (tripId: number) => {
  return useQuery({
    queryKey: ["trip-travel-details", tripId],
    queryFn: () => travelDeskApi.getTripTravelDetails(tripId),
    enabled: !!tripId,
  });
};

export const useAddTravelDeskItinerary = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: any) => travelDeskApi.addItineraryLeg(tripId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["trip-travel-details", tripId] });
      queryClient.invalidateQueries({ queryKey: ["traveldesk-bookings"] });
    },
  });
};

export const useAddTravelDeskVisa = (tripId: number) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: any) => travelDeskApi.addVisaRequirement(tripId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trip", tripId] });
      queryClient.invalidateQueries({ queryKey: ["trip-travel-details", tripId] });
      queryClient.invalidateQueries({ queryKey: ["traveldesk-bookings"] });
    },
  });
};

export const useConfirmBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tripId, comments }: { tripId: number; comments?: string }) =>
      travelDeskApi.confirmBooking(tripId, comments),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["trip", variables.tripId] });
      queryClient.invalidateQueries({ queryKey: ["trip-travel-details", variables.tripId] });
      queryClient.invalidateQueries({ queryKey: ["traveldesk-bookings"] });
      queryClient.invalidateQueries({ queryKey: ["traveldesk-dashboard"] });
    },
  });
};
