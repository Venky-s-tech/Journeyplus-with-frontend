package com.journeyplus.policy.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TravelAllowanceCalculationResponse {
    private String gradeId;
    private String travelType;
    private String cityName;
    private String country;
    
    // Combined cost rules
    private String tier;
    private String flightClass;
    private String hotelCategory;
    private BigDecimal perDiemRate; // Combined or from city tier
    private BigDecimal hotelCapPerNight; // From city tier
    private BigDecimal localConveyanceLimit; // From policy
}
