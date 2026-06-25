package com.journeyplus.policy.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "city_tiers", uniqueConstraints = {
    @UniqueConstraint(name = "uc_city_country", columnNames = {"city_name", "country"})
}, indexes = {
    @Index(name = "idx_city_tiers_name_country", columnList = "city_name, country"),
    @Index(name = "idx_city_tiers_tier", columnList = "tier")
})
@Getter
@Setter
public class CityTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    @Column(nullable = false, length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CityTierType tier;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "per_diem_rate", nullable = false, length = 255)
    private BigDecimal perDiemRate;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "hotel_cap_per_night", nullable = false, length = 255)
    private BigDecimal hotelCapPerNight;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ==========================================
    // Backward Compatibility Alias for Compliance Module
    // ==========================================
    public BigDecimal getDailyAllowanceLimit() {
        return this.perDiemRate;
    }

    public CityTier() {}

    public CityTier(String cityName, String country, CityTierType tier, BigDecimal perDiemRate, BigDecimal hotelCapPerNight) {
        this.cityName = cityName;
        this.country = country;
        this.tier = tier;
        this.perDiemRate = perDiemRate;
        this.hotelCapPerNight = hotelCapPerNight;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Constructor for backward compatibility in tests
    public CityTier(String cityName, String tierStr, BigDecimal dailyAllowanceLimit) {
        this.cityName = cityName;
        this.country = "India"; // Default
        try {
            // Map String tier (e.g. "TIER_1" or "TIER1") to CityTierType enum
            String cleanTier = tierStr.replace("_", "").toUpperCase();
            this.tier = CityTierType.valueOf(cleanTier);
        } catch (Exception e) {
            this.tier = CityTierType.TIER1;
        }
        this.perDiemRate = dailyAllowanceLimit;
        this.hotelCapPerNight = new BigDecimal("150.00"); // Default
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
}
