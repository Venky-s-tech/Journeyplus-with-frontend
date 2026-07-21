package com.journeyplus.traveldesk.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journeyplus.config.AuditAction;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.dto.VisaStatus;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.ItineraryStatus;
import com.journeyplus.trip.entity.LegType;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;

@Service
public class TravelDeskService {

    private static final Logger log = LoggerFactory.getLogger(TravelDeskService.class);

    private final TripRequestRepository tripRequestRepository;
    private final ItineraryLegRepository itineraryLegRepository;
    private final VisaRequirementRepository visaRequirementRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TravelDeskService(
            TripRequestRepository tripRequestRepository,
            ItineraryLegRepository itineraryLegRepository,
            VisaRequirementRepository visaRequirementRepository,
            ApplicationEventPublisher eventPublisher) {
        this.tripRequestRepository = tripRequestRepository;
        this.itineraryLegRepository = itineraryLegRepository;
        this.visaRequirementRepository = visaRequirementRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Map<String, Object>> getTravelDeskQueue() {
        log.info("Fetching manager-approved trips for Travel Desk queue");
        List<TripRequest> allTrips = tripRequestRepository.findAll();
        
        // Filter ONLY approved trips waiting for booking fulfillment
        List<TripRequest> approvedTrips = allTrips.stream()
                .filter(t -> t.getStatus() == TripStatus.APPROVED || t.getStatus() == TripStatus.BOOKED)
                .sorted(Comparator.comparing(TripRequest::getDepartureDate))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (TripRequest t : approvedTrips) {
            List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(t.getId());
            List<VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(t.getId());

            String visaStatusStr = "NOT_REQUIRED";
            if (t.getTravelType() != null && ("INTERNATIONAL".equalsIgnoreCase(t.getTravelType()) || "INTL".equalsIgnoreCase(t.getTravelType()))) {
                if (visas.isEmpty()) {
                    visaStatusStr = "PENDING";
                } else {
                    visaStatusStr = visas.get(0).getStatus().name();
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("tripId", t.getId());
            item.put("id", t.getId());
            item.put("employeeId", t.getEmployee() != null ? t.getEmployee().getId() : null);
            item.put("employeeName", t.getEmployee() != null ? t.getEmployee().getUsername() : "Employee");
            item.put("department", t.getEmployee() != null && t.getEmployee().getDepartment() != null ? t.getEmployee().getDepartment() : "General");
            item.put("destination", t.getDestination());
            item.put("travelType", t.getTravelType());
            item.put("departureDate", t.getDepartureDate());
            item.put("returnDate", t.getReturnDate());
            item.put("purpose", t.getPurpose());
            item.put("estimatedCost", t.getEstimatedCost());
            item.put("status", t.getStatus().name());
            item.put("bookingStatus", t.getBookingStatus() != null ? t.getBookingStatus() : "PENDING_BOOKING");
            item.put("travelDeskStatus", t.getTravelDeskStatus() != null ? t.getTravelDeskStatus() : "QUEUED");
            item.put("visaStatus", visaStatusStr);
            item.put("approverName", t.getApprover() != null ? t.getApprover().getUsername() : "—");
            item.put("itineraryLegsCount", legs.size());
            item.put("hasItinerary", !legs.isEmpty());

            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getTravelDeskDashboard() {
        List<TripRequest> allTrips = tripRequestRepository.findAll();
        List<TripRequest> approvedTrips = allTrips.stream()
                .filter(t -> t.getStatus() == TripStatus.APPROVED)
                .collect(Collectors.toList());

        List<ItineraryLeg> allLegs = itineraryLegRepository.findAll();
        List<VisaRequirement> allVisas = visaRequirementRepository.findAll();

        Map<Long, List<ItineraryLeg>> legsByTrip = allLegs.stream()
                .filter(l -> l.getTripRequest() != null)
                .collect(Collectors.groupingBy(l -> l.getTripRequest().getId()));

        Map<Long, List<VisaRequirement>> visasByTrip = allVisas.stream()
                .filter(v -> v.getTripRequest() != null)
                .collect(Collectors.groupingBy(v -> v.getTripRequest().getId()));

        long pendingBookings = 0;
        long waitingForItinerary = 0;
        long waitingForVisa = 0;
        long completedBookings = 0;
        long flightBookings = 0;
        long hotelBookings = 0;

        for (TripRequest t : approvedTrips) {
            List<ItineraryLeg> legs = legsByTrip.getOrDefault(t.getId(), Collections.emptyList());
            boolean hasLegs = !legs.isEmpty();

            if (!hasLegs) {
                waitingForItinerary++;
            }

            boolean allConfirmed = hasLegs && legs.stream().allMatch(l -> l.getStatus() == ItineraryStatus.CONFIRMED);
            if (!allConfirmed || "PENDING_BOOKING".equalsIgnoreCase(t.getBookingStatus())) {
                pendingBookings++;
            } else {
                completedBookings++;
            }

            if (hasLegs) {
                for (ItineraryLeg leg : legs) {
                    if (leg.getLegType() == LegType.HOTEL) {
                        hotelBookings++;
                    } else if (leg.getLegType() == LegType.FLIGHT) {
                        flightBookings++;
                    }
                }
            }

            if ("INTERNATIONAL".equalsIgnoreCase(t.getTravelType()) || "INTL".equalsIgnoreCase(t.getTravelType())) {
                List<VisaRequirement> visas = visasByTrip.getOrDefault(t.getId(), Collections.emptyList());
                boolean visaGranted = !visas.isEmpty() && visas.stream().allMatch(v -> v.getStatus() == VisaStatus.GRANTED);
                if (!visaGranted) {
                    waitingForVisa++;
                }
            }
        }

        long bookedCount = allTrips.stream().filter(t -> t.getStatus() == TripStatus.BOOKED || t.getStatus() == TripStatus.COMPLETED).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("pendingBookings", pendingBookings);
        summary.put("waitingForItinerary", waitingForItinerary);
        summary.put("waitingForVisa", waitingForVisa);
        summary.put("completedBookings", completedBookings + bookedCount);
        summary.put("upcomingTrips", approvedTrips.size());
        summary.put("flightBookings", flightBookings);
        summary.put("hotelBookings", hotelBookings);
        summary.put("travelRequests", allTrips.size());

        return summary;
    }

    public List<ItineraryLeg> getItineraryLegs(Long tripId) {
        return itineraryLegRepository.findByTripRequest_Id(tripId);
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "ADD_ITINERARY_LEG")
    public ItineraryLeg addItineraryLeg(Long tripId, ItineraryLeg leg) {
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        if (trip.getStatus() != TripStatus.APPROVED && trip.getStatus() != TripStatus.BOOKED) {
            throw new IllegalStateException("Itinerary legs can only be added to APPROVED or BOOKED trips");
        }

        leg.setId(null);
        leg.setTripRequest(trip);
        if (leg.getStatus() == null) {
            leg.setStatus(ItineraryStatus.CONFIRMED);
        }

        ItineraryLeg saved = itineraryLegRepository.save(leg);
        trip.setTravelDeskStatus("IN_PROGRESS");
        tripRequestRepository.save(trip);

        return saved;
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "UPDATE_ITINERARY_LEG")
    public ItineraryLeg updateItineraryLeg(Long legId, ItineraryLeg details) {
        ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found"));

        if (details.getOrigin() != null) leg.setOrigin(details.getOrigin());
        if (details.getDestination() != null) leg.setDestination(details.getDestination());
        if (details.getLegType() != null) leg.setLegType(details.getLegType());
        if (details.getTravelDate() != null) leg.setTravelDate(details.getTravelDate());
        if (details.getDepartureDateTime() != null) leg.setDepartureDateTime(details.getDepartureDateTime());
        if (details.getArrivalDateTime() != null) leg.setArrivalDateTime(details.getArrivalDateTime());
        if (details.getCarrierDetails() != null) leg.setCarrierDetails(details.getCarrierDetails());
        if (details.getBookingRef() != null) leg.setBookingRef(details.getBookingRef());
        if (details.getCost() != null) leg.setCost(details.getCost());
        if (details.getOriginalCurrency() != null) leg.setOriginalCurrency(details.getOriginalCurrency());
        if (details.getStatus() != null) leg.setStatus(details.getStatus());

        return itineraryLegRepository.save(leg);
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "DELETE_ITINERARY_LEG")
    public void deleteItineraryLeg(Long legId) {
        ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found"));
        itineraryLegRepository.delete(leg);
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "CONFIRM_BOOKING")
    public TripRequest confirmBooking(Long tripId, String comments) {
        log.info("Confirming travel desk booking for trip ID {}", tripId);
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        if (trip.getStatus() != TripStatus.APPROVED && trip.getStatus() != TripStatus.BOOKED) {
            throw new IllegalStateException("Only APPROVED trips can be confirmed by Travel Desk");
        }

        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(tripId);
        for (ItineraryLeg leg : legs) {
            leg.setStatus(ItineraryStatus.CONFIRMED);
            itineraryLegRepository.save(leg);
        }

        trip.setStatus(TripStatus.BOOKED);
        trip.setBookingStatus("CONFIRMED");
        trip.setTravelDeskStatus("CONFIRMED");
        trip.setWorkflowStage("COMPLETED");
        if (comments != null && !comments.isBlank()) {
            trip.setComments(comments);
        }

        TripRequest saved = tripRequestRepository.save(trip);

        // Publish notifications to employee and approver
        try {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    trip.getEmployee().getId(),
                    "Travel Desk Booking Confirmed",
                    "Your travel booking for trip to " + trip.getDestination() + " has been confirmed by Travel Desk.",
                    null,
                    "Travel Desk"
            ));
        } catch (Exception e) {}

        return saved;
    }

    public List<VisaRequirement> getVisaQueue() {
        List<VisaRequirement> allVisas = visaRequirementRepository.findAll();
        return allVisas.stream()
                .filter(v -> v.getTripRequest() != null &&
                        (v.getTripRequest().getStatus() == TripStatus.APPROVED || v.getTripRequest().getStatus() == TripStatus.BOOKED))
                .collect(Collectors.toList());
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "ADD_VISA_REQUIREMENT")
    public VisaRequirement addVisaRequirement(Long tripId, VisaRequirement visa) {
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        visa.setId(null);
        visa.setTripRequest(trip);
        if (visa.getStatus() == null) {
            visa.setStatus(VisaStatus.PENDING);
        }

        return visaRequirementRepository.save(visa);
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "UPDATE_VISA_STATUS")
    public VisaRequirement updateVisaStatus(Long visaId, VisaStatus status, String notes) {
        VisaRequirement visa = visaRequirementRepository.findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found"));

        visa.setStatus(status);
        if (notes != null) {
            visa.setNotes(notes);
        }
        if (status == VisaStatus.APPLIED && visa.getApplicationDate() == null) {
            visa.setApplicationDate(LocalDate.now());
        } else if (status == VisaStatus.GRANTED && visa.getSubmittedDate() == null) {
            visa.setSubmittedDate(LocalDate.now());
        }

        VisaRequirement saved = visaRequirementRepository.save(visa);

        // Notify employee about visa update
        try {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    saved.getTripRequest().getEmployee().getId(),
                    "Visa Requirement Status Updated",
                    "Visa application for " + saved.getCountry() + " updated to " + status.name() + ".",
                    null,
                    "Travel Desk Visa Team"
            ));
        } catch (Exception e) {}

        return saved;
    }

    @Transactional
    @AuditAction(module = "TRAVEL_DESK", action = "REJECT_TRIP_BACK")
    public TripRequest rejectTripBack(Long tripId, String comments, User user) {
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        trip.setStatus(TripStatus.REJECTED);
        trip.setTravelDeskStatus("REJECTED");
        trip.setComments(comments != null ? comments : "Rejected back by Travel Desk");
        TripRequest saved = tripRequestRepository.save(trip);

        try {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    trip.getEmployee().getId(),
                    "Trip Rejected by Travel Desk",
                    "Your trip to " + trip.getDestination() + " was returned by Travel Desk: " + comments,
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : "Travel Desk"
            ));
        } catch (Exception e) {}

        return saved;
    }
}
