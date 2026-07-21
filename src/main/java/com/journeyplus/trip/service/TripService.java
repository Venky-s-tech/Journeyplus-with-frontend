package com.journeyplus.trip.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final TripRequestRepository tripRequestRepository;
    private final ItineraryLegRepository itineraryLegRepository;
    private final VisaRequirementRepository visaRequirementRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final com.journeyplus.policy.repository.TravelPolicyRepository travelPolicyRepository;
    private final com.journeyplus.policy.repository.CityTierRepository cityTierRepository;

    public TripService(
            TripRequestRepository tripRequestRepository,
            ItineraryLegRepository itineraryLegRepository,
            VisaRequirementRepository visaRequirementRepository,
            ApplicationEventPublisher eventPublisher,
            com.journeyplus.policy.repository.TravelPolicyRepository travelPolicyRepository,
            com.journeyplus.policy.repository.CityTierRepository cityTierRepository) {
        this.tripRequestRepository = tripRequestRepository;
        this.itineraryLegRepository = itineraryLegRepository;
        this.visaRequirementRepository = visaRequirementRepository;
        this.eventPublisher = eventPublisher;
        this.travelPolicyRepository = travelPolicyRepository;
        this.cityTierRepository = cityTierRepository;
    }

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
        // Only overwrite the approver when the update actually supplied one.
        // Previously this unconditionally ran existing.setApprover(updatedData.getApprover()),
        // which silently wiped out an already-assigned approver to null any
        // time a client's update request simply didn't mention approverUsername
        // (e.g. a partial update via Swagger/API, or a future edit UI that
        // only changes a couple of fields) - the approver was never meant
        // to be cleared in that case, only left alone.
        if (updatedData.getApprover() != null) {
            existing.setApprover(updatedData.getApprover());
        }

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
        trip.setBookingStatus("SUBMITTED");
        trip.setWorkflowStage("MANAGER_APPROVAL");
        trip.setTravelDeskStatus("PENDING_APPROVAL");
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

        // Validate that the manager is the assigned approver or their active delegate
        boolean isAssignedApprover = trip.getApprover() != null && trip.getApprover().getId().equals(manager.getId());
        boolean isDelegateApprover = trip.getApprover() != null && trip.getApprover().getDelegateApprover() != null
                && trip.getApprover().getDelegateApprover().getId().equals(manager.getId())
                && trip.getApprover().isDelegationActive();

        if (!isAssignedApprover && !isDelegateApprover) {
            throw new org.springframework.security.access.AccessDeniedException("Only the assigned approving manager or their active delegate can approve or reject this trip");
        }

        trip.setStatus(newStatus);
        trip.setComments(comments);

        if (newStatus == TripStatus.APPROVED) {
            trip.setBookingStatus("PENDING_BOOKING");
            trip.setWorkflowStage("TRAVEL_DESK");
            trip.setTravelDeskStatus("QUEUED");
        } else if (newStatus == TripStatus.REJECTED) {
            trip.setBookingStatus("REJECTED");
            trip.setWorkflowStage("REJECTED");
            trip.setTravelDeskStatus("REJECTED");
        }
        TripRequest saved = tripRequestRepository.save(trip);

        // For INTERNATIONAL trips, automatically queue a VisaRequirement if none exists
        if (newStatus == TripStatus.APPROVED && saved.getTravelType() != null &&
                ("INTERNATIONAL".equalsIgnoreCase(saved.getTravelType()) || "INTL".equalsIgnoreCase(saved.getTravelType()))) {
            try {
                List<VisaRequirement> existingVisas = visaRequirementRepository.findByTripRequest_Id(saved.getId());
                if (existingVisas == null || existingVisas.isEmpty()) {
                    VisaRequirement visa = new VisaRequirement(
                            saved,
                            saved.getDestination() != null ? saved.getDestination() : "International",
                            "BUSINESS",
                            true,
                            VisaStatus.PENDING,
                            "Auto-generated visa requirement on trip approval"
                    );
                    visaRequirementRepository.save(visa);
                }
            } catch (Exception e) {
                log.warn("Failed to auto-create visa requirement for approved international trip ID {}: {}", saved.getId(), e.getMessage());
            }
        }

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
            if (trip.getStatus() != TripStatus.BOOKED) {
                throw new IllegalStateException("Trip must be BOOKED before it can be marked as COMPLETED.");
            }
        } else if (newStatus == TripStatus.CANCELLED) {
            if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
                throw new IllegalStateException("Cannot cancel a completed or already cancelled trip");
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

    public String getExistingBookingReference(Long tripId) {
        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(tripId);
        if (legs == null) return null;
        for (ItineraryLeg leg : legs) {
            String ref = leg.getBookingRef();
            if (ref != null && !ref.trim().isEmpty()) {
                return ref.trim();
            }
        }
        return null;
    }

    @Transactional
    public ItineraryLeg addItineraryLeg(Long tripId, ItineraryLeg leg) {
        TripRequest trip = getTripRequest(tripId);
        if (trip.getStatus() != TripStatus.APPROVED) {
            throw new IllegalStateException("Itinerary details can only be managed for APPROVED trips");
        }
        validateItineraryLegBusinessRules(leg);

        // Enforce booking reference consistency
        String existingRef = getExistingBookingReference(tripId);
        if (existingRef != null) {
            String incomingRef = leg.getBookingRef();
            if (incomingRef != null && !incomingRef.trim().isEmpty()) {
                if (!existingRef.equalsIgnoreCase(incomingRef.trim())) {
                    throw new IllegalArgumentException("Booking reference must be the same as the existing booking reference: " + existingRef);
                }
            } else {
                leg.setBookingRef(existingRef);
            }
        }

        leg.setTripRequest(trip);
        ItineraryLeg savedLeg = itineraryLegRepository.save(leg);
        if (trip.getEmployee() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    trip.getEmployee().getId(),
                    "Itinerary Leg Added",
                    "An itinerary leg (" + leg.getOrigin() + " to " + leg.getDestination() + ") has been added to your trip.",
                    null,
                    "Travel Desk",
                    com.journeyplus.notification.entity.NotificationCategory.TripRequest
            ));
        }
        return savedLeg;
    }

    public ItineraryLeg getItineraryLeg(Long legId) {
        return itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found"));
    }

    @Transactional
    public ItineraryLeg updateItineraryLeg(Long legId, ItineraryLeg updatedData) {
        ItineraryLeg existing = getItineraryLeg(legId);
        if (existing.getTripRequest() == null || existing.getTripRequest().getStatus() != TripStatus.APPROVED) {
            throw new IllegalStateException("Itinerary details can only be managed for APPROVED trips");
        }
        validateItineraryLegBusinessRules(updatedData);

        // Enforce booking reference consistency
        Long tripId = existing.getTripRequest().getId();
        String existingRef = getExistingBookingReference(tripId);
        if (existingRef != null) {
            String incomingRef = updatedData.getBookingRef();
            if (incomingRef != null && !incomingRef.trim().isEmpty()) {
                if (!existingRef.equalsIgnoreCase(incomingRef.trim())) {
                    throw new IllegalArgumentException("Booking reference must be the same as the existing booking reference: " + existingRef);
                }
            } else {
                updatedData.setBookingRef(existingRef);
            }
        }

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

        ItineraryLeg savedLeg = itineraryLegRepository.save(existing);
        if (existing.getTripRequest() != null && existing.getTripRequest().getEmployee() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    existing.getTripRequest().getEmployee().getId(),
                    "Itinerary Leg Updated",
                    "An itinerary leg (" + existing.getOrigin() + " to " + existing.getDestination() + ") has been updated on your trip.",
                    null,
                    "Travel Desk",
                    com.journeyplus.notification.entity.NotificationCategory.TripRequest
            ));
        }
        return savedLeg;
    }

    @Transactional
    public void deleteItineraryLeg(Long legId) {
        ItineraryLeg existing = getItineraryLeg(legId);
        if (existing.getTripRequest() == null || existing.getTripRequest().getStatus() != TripStatus.APPROVED) {
            throw new IllegalStateException("Itinerary details can only be managed for APPROVED trips");
        }
        itineraryLegRepository.delete(existing);
        if (existing.getTripRequest() != null && existing.getTripRequest().getEmployee() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    existing.getTripRequest().getEmployee().getId(),
                    "Itinerary Leg Deleted",
                    "An itinerary leg (" + existing.getOrigin() + " to " + existing.getDestination() + ") has been removed from your trip.",
                    null,
                    "Travel Desk",
                    com.journeyplus.notification.entity.NotificationCategory.TripRequest
            ));
        }
    }

    // ==========================================
    // VISA REQUIREMENT CRUD
    // ==========================================

    @Transactional
    public VisaRequirement addVisaRequirement(Long tripId, VisaRequirement visa) {
        TripRequest trip = getTripRequest(tripId);
        if (trip.getStatus() != TripStatus.APPROVED) {
            throw new IllegalStateException("Visa details can only be managed for APPROVED trips");
        }
        if (!"INTERNATIONAL".equalsIgnoreCase(trip.getTravelType())) {
            throw new IllegalArgumentException("Visa records are only allowed for INTERNATIONAL trips");
        }
        validateVisaRequirementBusinessRules(visa);
        visa.setTripRequest(trip);
        VisaRequirement savedVisa = visaRequirementRepository.save(visa);
        if (trip.getEmployee() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    trip.getEmployee().getId(),
                    "Visa Requirement Added",
                    "A visa requirement for " + visa.getCountry() + " has been added to your trip.",
                    null,
                    "Travel Desk",
                    com.journeyplus.notification.entity.NotificationCategory.TripRequest
            ));
        }
        return savedVisa;
    }

    public VisaRequirement getVisaRequirement(Long visaId) {
        return visaRequirementRepository.findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found"));
    }

    @Transactional
    public VisaRequirement updateVisaRequirement(Long visaId, VisaRequirement updatedData) {
        VisaRequirement existing = getVisaRequirement(visaId);
        if (existing.getTripRequest() == null || existing.getTripRequest().getStatus() != TripStatus.APPROVED) {
            throw new IllegalStateException("Visa details can only be managed for APPROVED trips");
        }
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

        VisaRequirement savedVisa = visaRequirementRepository.save(existing);
        if (existing.getTripRequest() != null && existing.getTripRequest().getEmployee() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    existing.getTripRequest().getEmployee().getId(),
                    "Visa Requirement Updated",
                    "A visa requirement for " + existing.getCountry() + " has been updated on your trip.",
                    null,
                    "Travel Desk",
                    com.journeyplus.notification.entity.NotificationCategory.TripRequest
            ));
        }
        return savedVisa;
    }

    @Transactional
    @AuditAction(module = "TRIP", action = "UPDATE_VISA")
    public VisaRequirement updateVisaRequirement(Long tripId, Long visaId, VisaRequest visaRequest) {
        VisaRequirement existing = getVisaRequirement(visaId);
        if (existing.getTripRequest() == null || !existing.getTripRequest().getId().equals(tripId)) {
            throw new IllegalArgumentException("Visa requirement does not belong to the specified trip");
        }
        if (existing.getTripRequest().getStatus() != TripStatus.APPROVED) {
            throw new IllegalStateException("Visa details can only be managed for APPROVED trips");
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

        VisaRequirement savedVisa = visaRequirementRepository.save(existing);
        if (existing.getTripRequest() != null && existing.getTripRequest().getEmployee() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    existing.getTripRequest().getEmployee().getId(),
                    "Visa Requirement Updated",
                    "A visa requirement for " + existing.getCountry() + " has been updated on your trip.",
                    null,
                    "Travel Desk",
                    com.journeyplus.notification.entity.NotificationCategory.TripRequest
            ));
        }
        return savedVisa;
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

        if (trip.getEmployee() != null && trip.getEmployee().getGrade() != null) {
            String gradeId = trip.getEmployee().getGrade().getId();
            String tType = trip.getTravelType();
            com.journeyplus.policy.entity.TravelType travelType = com.journeyplus.policy.entity.TravelType.DOMESTIC;
            if (tType != null && ("INTERNATIONAL".equalsIgnoreCase(tType) || "INTL".equalsIgnoreCase(tType))) {
                travelType = com.journeyplus.policy.entity.TravelType.INTERNATIONAL;
            }
            java.time.LocalDateTime tripDateTime = trip.getDepartureDate().atStartOfDay();
            List<com.journeyplus.policy.entity.TravelPolicy> policies = travelPolicyRepository.findListByGrade_IdAndTravelTypeAndStatus(gradeId, travelType, com.journeyplus.policy.entity.PolicyStatus.ACTIVE)
                    .stream()
                    .filter(p -> p.getEffectiveDate() != null && !p.getEffectiveDate().isAfter(tripDateTime))
                    .sorted(java.util.Comparator.comparing(com.journeyplus.policy.entity.TravelPolicy::getEffectiveDate).reversed())
                    .toList();
            if (!policies.isEmpty()) {
                com.journeyplus.policy.entity.TravelPolicy policy = policies.get(0);
                long days = java.time.temporal.ChronoUnit.DAYS.between(trip.getDepartureDate(), trip.getReturnDate());
                if (days <= 0) days = 1;
                BigDecimal perDiem = policy.getPerDiemRate();
                BigDecimal maxAllowed = perDiem.multiply(BigDecimal.valueOf(days));

                java.util.Optional<com.journeyplus.policy.entity.CityTier> tierOpt = cityTierRepository.findByCityNameIgnoreCase(trip.getDestination());
                if (tierOpt.isPresent()) {
                    BigDecimal tierPerDiem = tierOpt.get().getPerDiemRate();
                    if (tierPerDiem != null && tierPerDiem.compareTo(BigDecimal.ZERO) > 0) {
                        maxAllowed = tierPerDiem.multiply(BigDecimal.valueOf(days));
                    }
                }

                if (trip.getEstimatedCost().compareTo(maxAllowed.multiply(new BigDecimal("3.0"))) > 0) {
                    throw new IllegalArgumentException("Estimated cost exceeds the maximum allowable policy budget limit of " + maxAllowed.multiply(new BigDecimal("3.0")) + " USD for this trip duration and destination");
                }
            }
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
