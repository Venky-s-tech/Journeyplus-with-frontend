package com.journeyplus.expense.repository;

import com.journeyplus.expense.entity.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {
    // Use nested property path to navigate the relation: employee.id and tripRequest.id
    List<ExpenseClaim> findByEmployee_Id(Long employeeId);
    List<ExpenseClaim> findByTripRequest_Id(Long tripRequestId);
}
