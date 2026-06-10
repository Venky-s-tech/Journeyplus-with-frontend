package com.journeyplus.policy.repository;

import com.journeyplus.policy.entity.CityTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityTierRepository extends JpaRepository<CityTier, Long> {
    Optional<CityTier> findByCityNameIgnoreCase(String cityName);
}
