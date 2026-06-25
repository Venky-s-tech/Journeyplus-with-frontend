package com.journeyplus.policy.dto;

import com.journeyplus.policy.entity.CityTierType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CityTierRequest {

    @NotBlank(message = "City name is required")
    @Size(max = 100, message = "City name must not exceed 100 characters")
    private String cityName;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotNull(message = "Tier is required")
    private CityTierType tier;

    @NotNull(message = "Per diem rate is required")
    @DecimalMin(value = "0.01", message = "Per diem rate must be greater than zero")
    private BigDecimal perDiemRate;

    @NotNull(message = "Hotel cap per night is required")
    @DecimalMin(value = "0.01", message = "Hotel cap per night must be greater than zero")
    private BigDecimal hotelCapPerNight;
}
