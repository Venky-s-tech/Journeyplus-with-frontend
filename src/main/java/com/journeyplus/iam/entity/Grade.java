package com.journeyplus.iam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades", indexes = {
    @Index(name = "idx_grades_name", columnList = "grade_name"),
    @Index(name = "idx_grades_status", columnList = "status")
})
@Getter
@Setter
public class Grade {

    @Id
    @Column(name = "grade_id", length = 10)
    private String id; // E.g. "G1", "G2"

    @Column(name = "grade_name", nullable = false, unique = true, length = 100)
    private String gradeName;

    @Column(length = 255)
    private String description;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String status = "Active"; // "Active" or "Inactive"

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public Grade() {}

    public Grade(String id, String gradeName, String description, String status) {
        this.id = id;
        this.gradeName = gradeName;
        this.description = description;
        this.status = status;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
}
