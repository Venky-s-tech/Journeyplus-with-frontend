package com.journeyplus.iam.repository;

import com.journeyplus.iam.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
    Optional<Grade> findByGradeName(String gradeName);
    boolean existsByGradeName(String gradeName);
}
