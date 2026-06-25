package com.journeyplus.iam.controller;

import com.journeyplus.iam.dto.GradeRequest;
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ResponseEntity<List<Grade>> getAllGrades() {
        return ResponseEntity.ok(gradeService.getAllGrades());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> getGradeById(@PathVariable String id) {
        return ResponseEntity.ok(gradeService.getGradeById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Grade> createGrade(@Valid @RequestBody GradeRequest request) {
        Grade created = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Grade> updateGrade(@PathVariable String id, @Valid @RequestBody GradeRequest request) {
        Grade updated = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Grade> deactivateGrade(@PathVariable String id) {
        Grade deactivated = gradeService.deactivateGrade(id);
        return ResponseEntity.ok(deactivated);
    }
}
