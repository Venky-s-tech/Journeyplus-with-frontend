package com.journeyplus.iam.dto;

import com.journeyplus.iam.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    private Role role;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{10,20}$", message = "Invalid phone format")
    private String phone;

    private String gradeId;

    private String departmentId;

    @Pattern(regexp = "^(Active|Inactive)$", message = "Status must be either 'Active' or 'Inactive'")
    private String status;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", 
             message = "Password must be at least 8 characters long, and contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password; // Optional password update
}
