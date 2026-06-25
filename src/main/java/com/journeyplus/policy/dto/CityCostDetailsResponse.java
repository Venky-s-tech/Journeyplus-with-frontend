package com.journeyplus.policy.dto;

import com.journeyplus.policy.entity.CityTier;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CityCostDetailsResponse {
    private String tier;
    private BigDecimal perDiemRate;
    private BigDecimal hotelCapPerNight;

    public CityCostDetailsResponse() {}

    public CityCostDetailsResponse(CityTier cityTier) {
        this.tier = cityTier.getTier().name();
        this.perDiemRate = cityTier.getPerDiemRate();
        this.hotelCapPerNight = cityTier.getHotelCapPerNight();
    }
}
