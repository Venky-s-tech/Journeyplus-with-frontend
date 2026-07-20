package com.journeyplus.trip.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.dto.ItineraryLegResponse;
import com.journeyplus.trip.dto.SimpleUserDTO;
import com.journeyplus.trip.dto.TripRequestInput;
import com.journeyplus.trip.dto.TripResponse;
import com.journeyplus.trip.dto.TripSummaryResponse;
import com.journeyplus.trip.dto.VisaRequirementResponse;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;
import com.journeyplus.trip.service.TripService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trips")
public class TripRequestController {

    private final TripService tripService;
    private final UserRepository userRepository;
    private final ItineraryLegRepository itineraryLegRepository;
    private final VisaRequirementRepository visaRequirementRepository;

    public TripRequestController(
            TripService tripService,
            UserRepository userRepository,
            ItineraryLegRepository itineraryLegRepository,
            VisaRequirementRepository visaRequirementRepository) {
        this.tripService = tripService;
        this.userRepository = userRepository;
        this.itineraryLegRepository = itineraryLegRepository;
        this.visaRequirementRepository = visaRequirementRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TripResponse> createTripRequest(
            @Valid @RequestBody TripRequestInput r, // Accept basic inputs directly
            @AuthenticationPrincipal User employee) {

        TripRequest tripEntity = new TripRequest();
        if (r != null) {
            tripEntity.setPurpose(r.getPurpose());
            tripEntity.setDestination(r.getDestination());
            tripEntity.setDepartureDate(r.getDepartureDate());
            tripEntity.setReturnDate(r.getReturnDate());
            tripEntity.setTravelType(r.getTravelType());
            tripEntity.setEstimatedCost(r.getEstimatedCost());
            tripEntity.setComments(r.getComments());

            if (r.getApproverUsername() != null && !r.getApproverUsername().isBlank()) {
                User mgr = userRepository.findByUsername(r.getApproverUsername())
                        .orElseThrow(() -> new IllegalArgumentException("Approving manager not found: " + r.getApproverUsername()));
                tripEntity.setApprover(mgr);
            }
        }

        tripEntity.setEmployee(employee);

        // Explicitly pass null or empty collections for legs and visas since
        // the employee shouldn't specify them when raising the initial request.
        TripRequest trip = tripService.createTripRequest(
                tripEntity,
                null,
                null
        );

        return ResponseEntity.ok(toTripResponse(trip));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TripResponse> updateTripRequest(
            @PathVariable Long id,
            @Valid @RequestBody TripRequestInput input,
            @AuthenticationPrincipal User employee) {

        TripRequest updatedData = new TripRequest();
        updatedData.setPurpose(input.getPurpose());
        updatedData.setDestination(input.getDestination());
        updatedData.setDepartureDate(input.getDepartureDate());
        updatedData.setReturnDate(input.getReturnDate());
        updatedData.setTravelType(input.getTravelType());
        updatedData.setEstimatedCost(input.getEstimatedCost());
        updatedData.setComments(input.getComments());
        if (input.getApproverUsername() != null && !input.getApproverUsername().isBlank()) {
            User mgr = userRepository.findByUsername(input.getApproverUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Approving manager not found: " + input.getApproverUsername()));
            updatedData.setApprover(mgr);
        }

        TripRequest saved = tripService.updateTripRequest(id, updatedData, employee);
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TripResponse> submitTripRequest(@PathVariable Long id, @AuthenticationPrincipal User employee) {
        TripRequest trip = tripService.getTripRequest(id);

        // Enforce ownership
        if (!trip.getEmployee().getId().equals(employee.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the Trip Owner can submit this trip request");
        }

        TripRequest saved = tripService.submitTripRequest(id);
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<TripResponse> approveTripRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User manager) {
        TripRequest saved = tripService.approveOrRejectTripRequest(id, TripStatus.APPROVED, comments, manager);
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<TripResponse> rejectTripRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User manager) {
        TripRequest saved = tripService.approveOrRejectTripRequest(id, TripStatus.REJECTED, comments, manager);
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TRAVEL_DESK')")
    public ResponseEntity<TripResponse> completeTripRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(id);

        // Ownership or Travel Desk check
        boolean isOwner = trip.getEmployee().getId().equals(user.getId());
        boolean isTravelDesk = user.getRole() == com.journeyplus.iam.entity.Role.TRAVEL_DESK || user.getRole() == com.journeyplus.iam.entity.Role.ADMIN;
        if (!isOwner && !isTravelDesk) {
            throw new org.springframework.security.access.AccessDeniedException("Only the Trip Owner or Travel Desk can complete this trip");
        }

        TripRequest saved = tripService.completeOrCancelTripRequest(id, TripStatus.COMPLETED);
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TRAVEL_DESK')")
    public ResponseEntity<TripResponse> cancelTripRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(id);

        // Ownership or Travel Desk check
        boolean isOwner = trip.getEmployee().getId().equals(user.getId());
        boolean isTravelDesk = user.getRole() == com.journeyplus.iam.entity.Role.TRAVEL_DESK || user.getRole() == com.journeyplus.iam.entity.Role.ADMIN;
        if (!isOwner && !isTravelDesk) {
            throw new org.springframework.security.access.AccessDeniedException("Only the Trip Owner or Travel Desk can cancel this trip");
        }

        TripRequest saved = tripService.completeOrCancelTripRequest(id, TripStatus.CANCELLED);
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TripResponse>> getMyTrips(@AuthenticationPrincipal User employee) {
        List<TripRequest> trips = tripService.getTripsByEmployee(employee.getId());
        List<TripResponse> dto = trips.stream().map(this::toTripResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<List<TripResponse>> getPendingApprovals(@AuthenticationPrincipal User manager) {
        List<TripRequest> trips = tripService.getPendingApprovalsForManager(manager.getId());
        List<TripResponse> dto = trips.stream().map(this::toTripResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponse> updateStatusPatch(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String statusStr = body.get("status");
        String remarks = body.get("remarks");
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        TripStatus newStatus = TripStatus.valueOf(statusStr.toUpperCase());
        TripRequest saved;
        if (newStatus == TripStatus.SUBMITTED) {
            saved = tripService.submitTripRequest(id);
        } else if (newStatus == TripStatus.APPROVED || newStatus == TripStatus.REJECTED) {
            saved = tripService.approveOrRejectTripRequest(id, newStatus, remarks, user);
        } else {
            saved = tripService.completeOrCancelTripRequest(id, newStatus);
        }
        return ResponseEntity.ok(toTripResponse(saved));
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<java.util.Map<String, Object>> getFullTripContext(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(id);
        TripResponse response = toTripResponse(trip);
        
        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(id);
        List<VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(id);
        
        java.util.Map<String, Object> fullContext = new java.util.HashMap<>();
        fullContext.put("trip", response);
        fullContext.put("itineraryLegs", legs);
        fullContext.put("visas", visas);
        fullContext.put("employeeTripHistoryCount", tripService.getTripsByEmployee(trip.getEmployee().getId()).size());
        
        return ResponseEntity.ok(fullContext);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(id);
        validateTripViewAuthorization(trip, user);
        return ResponseEntity.ok(toTripResponse(trip));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) String travelType,
            @RequestParam(required = false) String destination,
            @AuthenticationPrincipal User user) {

        // Non-admin/non-manager/non-traveldesk users can only view their own trips
        boolean isPrivileged = user.getRole() == com.journeyplus.iam.entity.Role.ADMIN ||
                user.getRole() == com.journeyplus.iam.entity.Role.TRAVEL_DESK ||
                user.getRole() == com.journeyplus.iam.entity.Role.APPROVING_MANAGER ||
                user.getRole() == com.journeyplus.iam.entity.Role.FINANCE ||
                user.getRole() == com.journeyplus.iam.entity.Role.COMPLIANCE;

        Long targetEmployeeId = employeeId;
        if (!isPrivileged) {
            targetEmployeeId = user.getId();
        }

        List<TripRequest> trips = tripService.filterTrips(targetEmployeeId, status, travelType, destination);
        List<TripResponse> dto = trips.stream().map(this::toTripResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<TripSummaryResponse> getTripSummary(@PathVariable Long id, @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(id);
        validateTripViewAuthorization(trip, user);

        TripResponse tripDetails = toTripResponse(trip);
        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(id);
        List<VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(id);

        List<ItineraryLegResponse> legResponses = legs.stream().map(this::toLegResponse).collect(Collectors.toList());
        List<VisaRequirementResponse> visaResponses = visas.stream().map(this::toVisaResponse).collect(Collectors.toList());

        BigDecimal totalCost = BigDecimal.ZERO;
        for (ItineraryLeg leg : legs) {
            if (leg.getCost() != null) {
                totalCost = totalCost.add(leg.getCost());
            }
        }

        TripSummaryResponse summary = new TripSummaryResponse(tripDetails, legResponses, visaResponses, totalCost);
        return ResponseEntity.ok(summary);
    }

    private void validateTripViewAuthorization(TripRequest trip, User user) {
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }

        // Admins, Compliance, Finance, Travel Desk can view all
        com.journeyplus.iam.entity.Role role = user.getRole();
        if (role == com.journeyplus.iam.entity.Role.ADMIN ||
                role == com.journeyplus.iam.entity.Role.COMPLIANCE ||
                role == com.journeyplus.iam.entity.Role.FINANCE ||
                role == com.journeyplus.iam.entity.Role.TRAVEL_DESK) {
            return;
        }

        // Owner can view
        if (trip.getEmployee() != null && trip.getEmployee().getId().equals(user.getId())) {
            return;
        }

        // Assigned Approving Manager can view
        if (trip.getApprover() != null && trip.getApprover().getId().equals(user.getId())) {
            return;
        }

        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this trip");
    }

    private SimpleUserDTO toSimpleUser(User u) {
        if (u == null) return null;
        SimpleUserDTO s = new SimpleUserDTO();
        try {
            if (u instanceof org.hibernate.proxy.HibernateProxy) {
                org.hibernate.proxy.HibernateProxy proxy = (org.hibernate.proxy.HibernateProxy) u;
                // Previously this checked isUninitialized() and returned early
                // with only the ID set, which is why username/email/role came
                // back null for every approver/employee - the lazy proxy is
                // essentially always uninitialized at this point since nothing
                // else in the request would have touched it yet. Forcing the
                // implementation here triggers Hibernate's normal lazy-load,
                // which works fine since we're still inside the request's
                // transaction/session. The outer catch below still provides a
                // safe ID-only fallback if the session really is closed.
                u = (User) proxy.getHibernateLazyInitializer().getImplementation();
            }

            s.setId(u.getId());
            s.setUsername(u.getUsername());
            s.setEmail(u.getEmail());
            s.setRole(u.getRole() != null ? u.getRole().name() : null);
        } catch (Exception e) {
            try {
                if (u.getId() != null) s.setId(u.getId());
            } catch (Exception ex) {
            }
        }
        return s;
    }

    private TripResponse toTripResponse(TripRequest t) {
        if (t == null) return null;
        TripResponse r = new TripResponse();
        r.setId(t.getId());
        r.setEmployee(toSimpleUser(t.getEmployee()));
        r.setPurpose(t.getPurpose());
        r.setDestination(t.getDestination());
        r.setDepartureDate(t.getDepartureDate());
        r.setReturnDate(t.getReturnDate());
        r.setTravelType(t.getTravelType());
        r.setEstimatedCost(t.getEstimatedCost());
        r.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
        r.setComments(t.getComments());
        r.setApprover(toSimpleUser(t.getApprover()));
        r.setCreatedDate(t.getCreatedDate());
        r.setUpdatedDate(t.getUpdatedDate());
        return r;
    }

    private ItineraryLegResponse toLegResponse(ItineraryLeg leg) {
        if (leg == null) return null;
        ItineraryLegResponse r = new ItineraryLegResponse();
        r.setId(leg.getId());
        r.setOrigin(leg.getOrigin());
        r.setDestination(leg.getDestination());
        r.setLegType(leg.getLegType() != null ? leg.getLegType().name() : null);
        r.setTravelDate(leg.getTravelDate());
        r.setDepartureDateTime(leg.getDepartureDateTime());
        r.setArrivalDateTime(leg.getArrivalDateTime());
        r.setCost(leg.getCost());
        r.setOriginalCurrency(leg.getOriginalCurrency());
        r.setUsdEquivalent(leg.getUsdEquivalent());
        r.setCarrierDetails(leg.getCarrierDetails());
        r.setBookingRef(leg.getBookingRef());
        r.setStatus(leg.getStatus() != null ? leg.getStatus().name() : null);
        return r;
    }

    private VisaRequirementResponse toVisaResponse(VisaRequirement v) {
        if (v == null) return null;
        VisaRequirementResponse r = new VisaRequirementResponse();
        r.setId(v.getId());
        r.setCountry(v.getCountry());
        r.setVisaType(v.getVisaType());
        r.setRequiresVisa(v.isRequiresVisa());
        r.setApplicationDate(v.getApplicationDate());
        r.setSubmittedDate(v.getSubmittedDate());
        r.setStatus(v.getStatus() != null ? v.getStatus().name() : null);
        r.setNotes(v.getNotes());
        r.setCreatedDate(v.getCreatedDate());
        r.setUpdatedDate(v.getUpdatedDate());
        return r;
    }
}
