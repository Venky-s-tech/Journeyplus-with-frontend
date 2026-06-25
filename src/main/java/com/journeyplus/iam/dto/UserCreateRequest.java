package com.journeyplus.iam.dto;

import com.journeyplus.iam.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", 
             message = "Password must be at least 8 characters long, and contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{10,20}$", message = "Invalid phone format")
    private String phone;

    @NotBlank(message = "Grade ID is required")
    private String gradeId;

    @NotBlank(message = "Department ID is required")
    private String departmentId;

    @Pattern(regexp = "^(Active|Inactive)$", message = "Status must be either 'Active' or 'Inactive'")
    private String status = "Active";
}
