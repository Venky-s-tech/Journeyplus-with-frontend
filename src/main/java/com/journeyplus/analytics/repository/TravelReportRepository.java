package com.journeyplus.analytics.repository;

import com.journeyplus.analytics.entity.TravelReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelReportRepository extends JpaRepository<TravelReport, Long> {
    List<TravelReport> findByReportType(String reportType);
}
