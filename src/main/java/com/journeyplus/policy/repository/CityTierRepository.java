package com.journeyplus.policy.repository;

import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.CityTierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityTierRepository extends JpaRepository<CityTier, Long> {
    Optional<CityTier> findByCityNameIgnoreCase(String cityName);

    Optional<CityTier> findByCityNameIgnoreCaseAndCountryIgnoreCase(String cityName, String country);

    List<CityTier> findByTierAndCountryIgnoreCase(CityTierType tier, String country);

    default List<CityTier> searchCityTiers(
            CityTierType tier,
            String country
    ) {
        return findByTierAndCountryIgnoreCase(tier, country);
    }
}





