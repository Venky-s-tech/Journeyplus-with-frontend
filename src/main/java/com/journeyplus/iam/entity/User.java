package com.journeyplus.iam.entity;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_role", columnList = "role")
})
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private Role role;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "department_id", nullable = false, length = 50)
    private String departmentId;

    @Column(nullable = false, length = 100)
    private String department; // Keep for backward compatibility with business modules

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grade_id", nullable = true)
    private Grade grade;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public User() {}

    public User(String username, String email, String password, Role role, String name, String phone, String departmentId, String department, Grade grade) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.phone = phone;
        this.departmentId = departmentId;
        this.department = department;
        this.grade = grade;
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Constructor for backward compatibility
    public User(String username, String email, String password, Role role, String department) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.department = department;
        this.name = username; // Default to username
        this.phone = "0000000000"; // Default
        this.departmentId = "DEPT-GEN"; // Default
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Status mapping (Active/Inactive)
    public String getStatus() {
        return active ? "Active" : "Inactive";
    }

    public void setStatus(String status) {
        if ("Active".equalsIgnoreCase(status)) {
            this.active = true;
        } else if ("Inactive".equalsIgnoreCase(status)) {
            this.active = false;
        } else {
            throw new IllegalArgumentException("Status must be Active or Inactive");
        }
    }

    // Compatibility getters for existing modules
    public LocalDateTime getCreatedAt() {
        return createdDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedDate;
    }

    // Spring Security UserDetails Implementations
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // If the user is an admin, grant all roles so admins can access any @PreAuthorize role-restricted endpoint.
        if (this.role == com.journeyplus.iam.entity.Role.ADMIN) {
            java.util.List<SimpleGrantedAuthority> auths = new java.util.ArrayList<>();
            for (com.journeyplus.iam.entity.Role r : com.journeyplus.iam.entity.Role.values()) {
                auths.add(new SimpleGrantedAuthority("ROLE_" + r.name()));
            }
            return auths;
        }

        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.active;
    }
}
