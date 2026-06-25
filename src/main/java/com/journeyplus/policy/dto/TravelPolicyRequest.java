package com.journeyplus.policy.dto;

import com.journeyplus.policy.entity.FlightClass;
import com.journeyplus.policy.entity.HotelCategory;
import com.journeyplus.policy.entity.PolicyStatus;
import com.journeyplus.policy.entity.TravelType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TravelPolicyRequest {

    @NotBlank(message = "Policy name is required")
    @Size(max = 150, message = "Policy name must not exceed 150 characters")
    private String policyName;

    private String description;

    @NotBlank(message = "Grade ID is required")
    private String gradeId;

    @NotNull(message = "Travel type is required")
    private TravelType travelType;

    @NotNull(message = "Flight class is required")
    private FlightClass flightClass;

    @NotNull(message = "Hotel category is required")
    private HotelCategory hotelCategory;

    @NotNull(message = "Per diem rate is required")
    @DecimalMin(value = "0.01", message = "Per diem rate must be greater than zero")
    private BigDecimal perDiemRate;

    @NotNull(message = "Local conveyance limit is required")
    @DecimalMin(value = "0.01", message = "Local conveyance limit must be greater than zero")
    private BigDecimal localConveyanceLimit;

    private PolicyStatus status = PolicyStatus.ACTIVE;
}
