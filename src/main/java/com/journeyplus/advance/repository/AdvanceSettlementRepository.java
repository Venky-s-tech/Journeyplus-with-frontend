package com.journeyplus.advance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.advance.entity.AdvanceSettlement;

@Repository
public interface AdvanceSettlementRepository extends JpaRepository<AdvanceSettlement, Long> {
    List<AdvanceSettlement> findByAdvanceRequest_Id(Long advanceRequestId);
}
