package com.journeyplus.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.trip.entity.VisaRequirement;

@Repository
public interface VisaRequirementRepository extends JpaRepository<VisaRequirement, Long> {
    List<VisaRequirement> findByTripRequest_Id(Long tripRequestId);
}
