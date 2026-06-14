package com.journeyplus.advance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.advance.entity.AdvanceSettlement;

@Repository
public interface AdvanceSettlementRepository extends JpaRepository<AdvanceSettlement, Long> {
    Optional<AdvanceSettlement> findByAdvanceRequestId(Long advanceRequestId);
}
