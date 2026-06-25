package com.journeyplus.policy.dto;

import com.journeyplus.policy.entity.TravelPolicy;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TravelPolicyResponse {
    private Long id;
    private String policyName;
    private String description;
    private String gradeId;
    private String travelType;
    private String flightClass;
    private String hotelCategory;
    private BigDecimal perDiemRate;
    private BigDecimal localConveyanceLimit;
    private LocalDateTime effectiveDate;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public TravelPolicyResponse() {}

    public TravelPolicyResponse(TravelPolicy policy) {
        this.id = policy.getId();
        this.policyName = policy.getPolicyName();
        this.description = policy.getDescription();
        this.gradeId = policy.getGrade() != null ? policy.getGrade().getId() : null;
        this.travelType = policy.getTravelType().name();
        this.flightClass = policy.getFlightClass().name();
        this.hotelCategory = policy.getHotelCategory().name();
        this.perDiemRate = policy.getPerDiemRate();
        this.localConveyanceLimit = policy.getLocalConveyanceLimit();
        this.effectiveDate = policy.getEffectiveDate();
        this.status = policy.getStatus().name();
        this.createdDate = policy.getCreatedDate();
        this.updatedDate = policy.getUpdatedDate();
    }
}
