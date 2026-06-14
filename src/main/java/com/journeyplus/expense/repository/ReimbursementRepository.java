package com.journeyplus.expense.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.expense.entity.Reimbursement;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    Optional<Reimbursement> findByExpenseClaim_Id(Long expenseClaimId);
}
