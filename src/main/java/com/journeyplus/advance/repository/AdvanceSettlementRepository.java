package com.journeyplus.advance.repository;

import com.journeyplus.advance.entity.AdvanceSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvanceSettlementRepository extends JpaRepository<AdvanceSettlement, Long> {
    Optional<AdvanceSettlement> findByAdvanceRequestId(Long advanceRequestId);
}
