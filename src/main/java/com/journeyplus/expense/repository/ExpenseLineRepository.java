package com.journeyplus.expense.repository;

import com.journeyplus.expense.entity.ExpenseLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseLineRepository extends JpaRepository<ExpenseLine, Long> {
    List<ExpenseLine> findByExpenseClaimId(Long expenseClaimId);
}
