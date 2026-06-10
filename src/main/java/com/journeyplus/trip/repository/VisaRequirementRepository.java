package com.journeyplus.trip.repository;

import com.journeyplus.trip.entity.VisaRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisaRequirementRepository extends JpaRepository<VisaRequirement, Long> {
    List<VisaRequirement> findByTripRequestId(Long tripRequestId);
}
