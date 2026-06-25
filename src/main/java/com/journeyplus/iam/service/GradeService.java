package com.journeyplus.iam.service;

import com.journeyplus.config.AuditAction;
import com.journeyplus.iam.dto.GradeRequest;
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.repository.GradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GradeService {

    private static final Logger log = LoggerFactory.getLogger(GradeService.class);

    @Autowired
    private GradeRepository gradeRepository;

    public List<Grade> getAllGrades() {
        log.info("Fetching all grades");
        return gradeRepository.findAll();
    }

    public Grade getGradeById(String id) {
        log.info("Fetching grade by ID: {}", id);
        return gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + id));
    }

    @Transactional
    @AuditAction(module = "IAM", action = "CREATE_GRADE")
    public Grade createGrade(GradeRequest request) {
        log.info("Attempting to create grade with ID: {} and Name: {}", request.getId(), request.getGradeName());
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("Grade ID is required");
        }
        if (gradeRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Grade ID '" + request.getId() + "' already exists");
        }
        if (gradeRepository.existsByGradeName(request.getGradeName())) {
            throw new IllegalArgumentException("Grade name '" + request.getGradeName() + "' already exists");
        }

        Grade grade = new Grade(
                request.getId().toUpperCase().trim(),
                request.getGradeName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getStatus() != null ? request.getStatus() : "Active"
        );

        return gradeRepository.save(grade);
    }

    @Transactional
    @AuditAction(module = "IAM", action = "UPDATE_GRADE")
    public Grade updateGrade(String id, GradeRequest request) {
        log.info("Attempting to update grade with ID: {}", id);
        Grade grade = getGradeById(id);

        if (!grade.getGradeName().equalsIgnoreCase(request.getGradeName().trim()) &&
                gradeRepository.existsByGradeName(request.getGradeName().trim())) {
            throw new IllegalArgumentException("Grade name '" + request.getGradeName() + "' already exists");
        }

        grade.setGradeName(request.getGradeName().trim());
        grade.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        grade.setStatus(request.getStatus());

        return gradeRepository.save(grade);
    }

    @Transactional
    @AuditAction(module = "IAM", action = "DEACTIVATE_GRADE")
    public Grade deactivateGrade(String id) {
        log.info("Attempting to deactivate grade with ID: {}", id);
        Grade grade = getGradeById(id);
        grade.setStatus("Inactive");
        return gradeRepository.save(grade);
    }
}
