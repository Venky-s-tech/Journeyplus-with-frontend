package com.journeyplus.trip.service;

import com.journeyplus.trip.entity.TripStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class TripStatusTransitionService {

    private static final Map<TripStatus, Set<TripStatus>> ALLOWED_TRANSITIONS = Map.of(
            TripStatus.DRAFT, Set.of(TripStatus.SUBMITTED, TripStatus.CANCELLED),
            TripStatus.SUBMITTED, Set.of(TripStatus.APPROVED, TripStatus.REJECTED, TripStatus.CANCELLED),
            TripStatus.APPROVED, Set.of(TripStatus.BOOKED, TripStatus.CANCELLED),
            TripStatus.BOOKED, Set.of(TripStatus.COMPLETED, TripStatus.CANCELLED),
            TripStatus.REJECTED, EnumSet.noneOf(TripStatus.class),
            TripStatus.COMPLETED, EnumSet.noneOf(TripStatus.class),
            TripStatus.CANCELLED, EnumSet.noneOf(TripStatus.class)
    );

    public boolean isTransitionAllowed(TripStatus currentStatus, TripStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) return false;
        if (currentStatus == targetStatus) return true;
        Set<TripStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(TripStatus.class));
        return allowed.contains(targetStatus);
    }

    public void validateTransition(TripStatus currentStatus, TripStatus targetStatus) {
        if (currentStatus == targetStatus) return;
        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            if (targetStatus == TripStatus.COMPLETED && currentStatus != TripStatus.BOOKED) {
                throw new IllegalStateException("Trip must be BOOKED before it can be marked as COMPLETED.");
            }
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + targetStatus);
        }
    }
}
