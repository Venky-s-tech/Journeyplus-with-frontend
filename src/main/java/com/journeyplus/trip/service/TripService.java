package com.journeyplus.trip.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journeyplus.config.AuditAction;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.dto.VisaRequest;
import com.journeyplus.trip.dto.VisaStatus;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;

@Service
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    @Autowired
    private TripRequestRepository tripRequestRepository;

    @Autowired
    private ItineraryLegRepository itineraryLegRepository;

    @Autowired
    private VisaRequirementRepository visaRequirementRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @AuditAction(module = "TRIP", action = "CREATE_TRIP")
    public TripRequest createTripRequest(TripRequest tripRequest, List<ItineraryLeg> legs, List<VisaRequirement> visas) {
        validateTripRequestBusinessRules(tripRequest);

        tripRequest.setStatus(TripStatus.DRAFT);
        TripRequest savedTrip = tripRequestRepository.save(tripRequest);

        try {
            Long empId = savedTrip.getEmployee() != null ? savedTrip.getEmployee().getId() : null;
            log.info("Created Trip id={} employeeId={}", savedTrip.getId(), empId);
        } catch (Exception e) {
            log.warn("Could not log saved trip employee id: {}", e.getMessage());
        }

        if (legs != null) {
            for (ItineraryLeg leg : legs) {
                validateItineraryLegBusinessRules(leg);
                leg.setTripRequest(savedTrip);
                itineraryLegRepository.save(leg);
            }
        }

        if (visas != null) {
            if (!"INTERNATIONAL".equalsIgnoreCase(savedTrip.getTravelType())) {
                throw new IllegalArgumentException("Visa records are only allowed for INTERNATIONAL trips");
            }
            for (VisaRequirement visa : visas) {
                validateVisaRequirementBusinessRules(visa);
                visa.setTripRequest(savedTrip);
                visaRequirementRepository.save(visa);
            }
        }

        return savedTrip;
    }

    @Transactional
    @AuditAction(module = "TRIP", action = "UPDATE_TRIP")
    public TripRequest updateTripRequest(Long tripId, TripRequest updatedData, User user) {
        TripRequest existing = getTripRequest(tripId);

        // Only owner can edit
        if (!existing.getEmployee().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the Trip Owner can edit this trip");
        }

        // Only DRAFT can be edited
        if (existing.getStatus() != TripStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT trips can be edited");
        }

        existing.setPurpose(updatedData.getPurpose());
        existing.setDestination(updatedData.getDestination());
        existing.setDepartureDate(updatedData.getDepartureDate());
        existing.setReturnDate(updatedData.getReturnDate());
        existing.setTravelType(updatedData.getTravelType());
        existing.setEstimatedCost(updatedData.getEstimatedCost());
        existing.setComments(updatedData.getComments());
        existing.setApprover(updatedData.getApprover());

        validateTripRequestBusinessRules(existing);

        return tripRequestRepository.save(existing);
    }

    @Transactional
    @AuditAction(module = "TRIP", action = "SUBMIT_TRIP")
    public TripRequest submitTripRequest(Long tripId) {
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        // Validate Transition DRAFT -> SUBMITTED
        if (trip.getStatus() != TripStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT trip requests can be submitted");
        }

        trip.setStatus(TripStatus.SUBMITTED);
        TripRequest saved = tripRequestRepository.save(trip);

        // Publish event for status change notification using persisted entity (saved)
        try {
            if (saved.getApprover() != null) {
                Long mgrId = saved.getApprover().getId();
                log.info("Publishing submission event for manager id={} tripId={}", mgrId, saved.getId());
                eventPublisher.publishEvent(new StatusChangeEvent(
                    mgrId,
                    "New Trip Request Submitted",
                    "A trip request has been submitted by " + saved.getEmployee().getUsername() + " and requires your review.",
                    saved.getEmployee() != null ? saved.getEmployee().getId() : null,
                    saved.getEmployee() != null ? saved.getEmployee().getUsername() : null
                ));
            } else {
                log.info("No approving manager set for tripId={}", saved.getId());
            }
        } catch (Exception e) {
            log.error("Error while publishing manager notification for tripId={}: {}", saved.getId(), e.getMessage());
        }

        try {
                eventPublisher.publishEvent(new StatusChangeEvent(
                    saved.getEmployee().getId(),
                    "Trip Request Submitted",
                    "Your trip request to " + saved.getDestination() + " has been successfully submitted.",
                    saved.getEmployee() != null ? saved.getEmployee().getId() : null,
                    saved.getEmployee() != null ? saved.getEmployee().getUsername() : null
                ));
        } catch (Exception e) {
            log.error("Error while publishing employee notification for tripId={}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }

    @Transactional
    @AuditAction(module = "TRIP", action = "APPROVE_REJECT_TRIP")
    public TripRequest approveOrRejectTripRequest(Long tripId, TripStatus newStatus, String comments, User manager) {
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        // Validate Transition SUBMITTED -> APPROVED/REJECTED
        if (trip.getStatus() != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED trip requests can be approved or rejected");
        }

        if (newStatus != TripStatus.APPROVED && newStatus != TripStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid status: Status must be APPROVED or REJECTED");
        }

        // Validate that the manager is the assigned approver
        if (trip.getApprover() == null || !trip.getApprover().getId().equals(manager.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the assigned approving manager can approve or reject this trip");
        }

        trip.setStatus(newStatus);
        trip.setComments(comments);
        TripRequest saved = tripRequestRepository.save(trip);

        // Notify employee
        eventPublisher.publishEvent(new StatusChangeEvent(
            trip.getEmployee().getId(),
            "Trip Request " + newStatus.name(),
            "Your trip request to " + trip.getDestination() + " has been " + newStatus.name().toLowerCase() + " by " + manager.getUsername() + ".",
            manager != null ? manager.getId() : null,
            manager != null ? manager.getUsername() : null
        ));

        return saved;
    }

    @Transactional
    @AuditAction(module = "TRIP", action = "COMPLETE_CANCEL_TRIP")
    public TripRequest completeOrCancelTripRequest(Long tripId, TripStatus newStatus) {
        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));

        // Validate Transitions
        if (newStatus == TripStatus.COMPLETED) {
            if (trip.getStatus() != TripStatus.APPROVED) {
                throw new IllegalStateException("Only APPROVED trips can be marked as COMPLETED");
            }
        } else if (newStatus == TripStatus.CANCELLED) {
            if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
                throw new IllegalStateException("Cannot cancel a completed or already cancelled trip");
            }
            if (trip.getStatus() != TripStatus.APPROVED) {
                throw new IllegalStateException("Only APPROVED trips can be CANCELLED");
            }
        } else {
            throw new IllegalArgumentException("Invalid target status. Must be COMPLETED or CANCELLED");
        }

        trip.setStatus(newStatus);
        TripRequest saved = tripRequestRepository.save(trip);

        // Notify employee
        eventPublisher.publishEvent(new StatusChangeEvent(
                trip.getEmployee().getId(),
                "Trip Request " + newStatus.name(),
                "Your trip request to " + trip.getDestination() + " is now " + newStatus.name().toLowerCase() + "."
        ));

        return saved;
    }

    public TripRequest getTripRequest(Long tripId) {
        return tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip request not found"));
    }

    public List<TripRequest> getTripsByEmployee(Long employeeId) {
        return tripRequestRepository.findByEmployee_Id(employeeId);
    }

    public List<TripRequest> getTripsForManager(Long managerId) {
        return tripRequestRepository.findByApprover_Id(managerId);
    }

    public List<TripRequest> getPendingApprovalsForManager(Long managerId) {
        List<TripRequest> all = tripRequestRepository.findByApprover_Id(managerId);
        // Filter only SUBMITTED trips for pending approvals queue
        return all.stream()
                .filter(t -> t.getStatus() == TripStatus.SUBMITTED)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<TripRequest> filterTrips(Long employeeId, TripStatus status, String travelType, String destination) {
        return tripRequestRepository.filterTrips(employeeId, status, travelType, destination);
    }

    // ==========================================
    // ITINERARY LEG CRUD
    // ==========================================

    @Transactional
    public ItineraryLeg addItineraryLeg(Long tripId, ItineraryLeg leg) {
        TripRequest trip = getTripRequest(tripId);
        validateItineraryLegBusinessRules(leg);
        leg.setTripRequest(trip);
        return itineraryLegRepository.save(leg);
    }

    public ItineraryLeg getItineraryLeg(Long legId) {
        return itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found"));
    }

    @Transactional
    public ItineraryLeg updateItineraryLeg(Long legId, ItineraryLeg updatedData) {
        ItineraryLeg existing = getItineraryLeg(legId);
        validateItineraryLegBusinessRules(updatedData);

        existing.setOrigin(updatedData.getOrigin());
        existing.setDestination(updatedData.getDestination());
        existing.setLegType(updatedData.getLegType());
        existing.setTravelDate(updatedData.getTravelDate());
        existing.setDepartureDateTime(updatedData.getDepartureDateTime());
        existing.setArrivalDateTime(updatedData.getArrivalDateTime());
        existing.setCarrierDetails(updatedData.getCarrierDetails());
        existing.setCost(updatedData.getCost());
        existing.setOriginalCurrency(updatedData.getOriginalCurrency());
        existing.setUsdEquivalent(updatedData.getUsdEquivalent());
        if (updatedData.getBookingRef() != null) existing.setBookingRef(updatedData.getBookingRef());
        if (updatedData.getStatus() != null) existing.setStatus(updatedData.getStatus());

        return itineraryLegRepository.save(existing);
    }

    @Transactional
    public void deleteItineraryLeg(Long legId) {
        ItineraryLeg existing = getItineraryLeg(legId);
        itineraryLegRepository.delete(existing);
    }

    // ==========================================
    // VISA REQUIREMENT CRUD
    // ==========================================

    @Transactional
    public VisaRequirement addVisaRequirement(Long tripId, VisaRequirement visa) {
        TripRequest trip = getTripRequest(tripId);
        if (!"INTERNATIONAL".equalsIgnoreCase(trip.getTravelType())) {
            throw new IllegalArgumentException("Visa records are only allowed for INTERNATIONAL trips");
        }
        validateVisaRequirementBusinessRules(visa);
        visa.setTripRequest(trip);
        return visaRequirementRepository.save(visa);
    }

    public VisaRequirement getVisaRequirement(Long visaId) {
        return visaRequirementRepository.findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found"));
    }

    @Transactional
    public VisaRequirement updateVisaRequirement(Long visaId, VisaRequirement updatedData) {
        VisaRequirement existing = getVisaRequirement(visaId);
        validateVisaRequirementBusinessRules(updatedData);

        // Validate visa status transitions
        validateVisaStatusTransition(existing.getStatus(), updatedData.getStatus());

        existing.setCountry(updatedData.getCountry());
        existing.setVisaType(updatedData.getVisaType());
        existing.setRequiresVisa(updatedData.isRequiresVisa());
        existing.setApplicationDate(updatedData.getApplicationDate());
        existing.setSubmittedDate(updatedData.getSubmittedDate());
        existing.setStatus(updatedData.getStatus());
        existing.setNotes(updatedData.getNotes());

        return visaRequirementRepository.save(existing);
    }

    @Transactional
    @AuditAction(module = "TRIP", action = "UPDATE_VISA")
    public VisaRequirement updateVisaRequirement(Long tripId, Long visaId, VisaRequest visaRequest) {
        VisaRequirement existing = getVisaRequirement(visaId);
        if (existing.getTripRequest() == null || !existing.getTripRequest().getId().equals(tripId)) {
            throw new IllegalArgumentException("Visa requirement does not belong to the specified trip");
        }

        // Validate visa status transitions
        if (visaRequest.getStatus() != null) {
            validateVisaStatusTransition(existing.getStatus(), visaRequest.getStatus());
            existing.setStatus(visaRequest.getStatus());
        }

        if (visaRequest.getCountry() != null) existing.setCountry(visaRequest.getCountry());
        if (visaRequest.getVisaType() != null) existing.setVisaType(visaRequest.getVisaType());
        if (visaRequest.getRequiresVisa() != null) existing.setRequiresVisa(visaRequest.getRequiresVisa());
        if (visaRequest.getNotes() != null) existing.setNotes(visaRequest.getNotes());
        if (visaRequest.getApplicationDate() != null) existing.setApplicationDate(visaRequest.getApplicationDate());
        if (visaRequest.getSubmittedDate() != null) existing.setSubmittedDate(visaRequest.getSubmittedDate());

        return visaRequirementRepository.save(existing);
    }

    @Transactional
    public void deleteVisaRequirement(Long visaId) {
        VisaRequirement existing = getVisaRequirement(visaId);
        visaRequirementRepository.delete(existing);
    }

    // ==========================================
    // BUSINESS RULES VALIDATION HELPERS
    // ==========================================

    private void validateTripRequestBusinessRules(TripRequest trip) {
        if (trip.getPurpose() == null || trip.getPurpose().isBlank()) {
            throw new IllegalArgumentException("Purpose is required");
        }
        if (trip.getDestination() == null || trip.getDestination().isBlank()) {
            throw new IllegalArgumentException("Destination is required");
        }
        if (trip.getDepartureDate() == null || trip.getReturnDate() == null) {
            throw new IllegalArgumentException("Departure and return dates are required");
        }
        if (!trip.getDepartureDate().isBefore(trip.getReturnDate())) {
            throw new IllegalArgumentException("DepartureDate must be before ReturnDate");
        }
        if (trip.getEstimatedCost() == null || trip.getEstimatedCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("EstimatedCost must be positive");
        }
    }

    private void validateItineraryLegBusinessRules(ItineraryLeg leg) {
        if (leg.getOrigin() == null || leg.getOrigin().isBlank()) {
            throw new IllegalArgumentException("Origin is required");
        }
        if (leg.getDestination() == null || leg.getDestination().isBlank()) {
            throw new IllegalArgumentException("Destination is required");
        }
        if (leg.getCost() == null || leg.getCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Itinerary Cost must be positive");
        }
        if (leg.getDepartureDateTime() != null && leg.getArrivalDateTime() != null) {
            if (!leg.getArrivalDateTime().isAfter(leg.getDepartureDateTime())) {
                throw new IllegalArgumentException("ArrivalDateTime must be after DepartureDateTime");
            }
        }
    }

    private void validateVisaRequirementBusinessRules(VisaRequirement visa) {
        if (visa.getCountry() == null || visa.getCountry().isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
        if (visa.getVisaType() == null || visa.getVisaType().isBlank()) {
            throw new IllegalArgumentException("VisaType is required");
        }
    }

    private void validateVisaStatusTransition(VisaStatus oldStatus, VisaStatus newStatus) {
        if (oldStatus == newStatus) return;
        switch (oldStatus) {
            case PENDING:
                // Can transition to any other status
                break;
            case APPLIED:
                if (newStatus != VisaStatus.GRANTED && newStatus != VisaStatus.REJECTED && newStatus != VisaStatus.PENDING) {
                    throw new IllegalStateException("From APPLIED, status can only transition to GRANTED, REJECTED, or PENDING");
                }
                break;
            case GRANTED:
                if (newStatus != VisaStatus.NOT_REQUIRED) {
                    throw new IllegalStateException("Cannot transition out of GRANTED except to NOT_REQUIRED");
                }
                break;
            case REJECTED:
                if (newStatus != VisaStatus.PENDING && newStatus != VisaStatus.APPLIED) {
                    throw new IllegalStateException("From REJECTED, status can only transition to PENDING or APPLIED");
                }
                break;
            case NOT_REQUIRED:
                if (newStatus != VisaStatus.PENDING) {
                    throw new IllegalStateException("From NOT_REQUIRED, status can only transition to PENDING");
                }
                break;
            default:
                throw new IllegalStateException("Invalid visa status transition");
        }
    }
}
