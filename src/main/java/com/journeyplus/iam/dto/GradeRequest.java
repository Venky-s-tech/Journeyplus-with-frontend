package com.journeyplus.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeRequest {

    @Size(max = 10, message = "Grade ID must not exceed 10 characters")
    private String id; // Required for creation, ignored or optional for update depending on flow

    @NotBlank(message = "Grade name is required")
    @Size(max = 100, message = "Grade name must not exceed 100 characters")
    private String gradeName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(Active|Inactive)$", message = "Status must be either 'Active' or 'Inactive'")
    private String status = "Active";
}
