package com.journeyplus.expense.repository;

import com.journeyplus.expense.entity.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    Optional<Reimbursement> findByExpenseClaimId(Long expenseClaimId);
}
