package com.journeyplus.policy.repository;

import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.CityTierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityTierRepository extends JpaRepository<CityTier, Long> {
    Optional<CityTier> findByCityNameIgnoreCase(String cityName);

    Optional<CityTier> findByCityNameIgnoreCaseAndCountryIgnoreCase(String cityName, String country);

    @Query("SELECT c FROM CityTier c WHERE " +
           "(:tier IS NULL OR c.tier = :tier) AND " +
           "(:country IS NULL OR LOWER(c.country) = LOWER(:country))")
    List<CityTier> searchCityTiers(
            @Param("tier") CityTierType tier,
            @Param("country") String country
    );
}
