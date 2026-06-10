package com.journeyplus.policy.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "city_tiers")
public class CityTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false, unique = true, length = 100)
    private String cityName;

    @Column(nullable = false, length = 20)
    private String tier; // TIER_1, TIER_2, TIER_3

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "daily_allowance_limit", nullable = false, length = 255)
    private BigDecimal dailyAllowanceLimit;

    public CityTier() {}

    public CityTier(String cityName, String tier, BigDecimal dailyAllowanceLimit) {
        this.cityName = cityName;
        this.tier = tier;
        this.dailyAllowanceLimit = dailyAllowanceLimit;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public BigDecimal getDailyAllowanceLimit() {
        return dailyAllowanceLimit;
    }

    public void setDailyAllowanceLimit(BigDecimal dailyAllowanceLimit) {
        this.dailyAllowanceLimit = dailyAllowanceLimit;
    }
}
