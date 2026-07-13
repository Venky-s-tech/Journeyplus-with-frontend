package com.journeyplus.compliance.repository;

import com.journeyplus.compliance.entity.PolicyException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyExceptionRepository extends JpaRepository<PolicyException, Long> {
    List<PolicyException> findByExpenseLineId(Long expenseLineId);
    List<PolicyException> findByApprovalStatus(String approvalStatus);

    List<PolicyException> findByExpenseLine_Id(Long expenseLineId);
}
