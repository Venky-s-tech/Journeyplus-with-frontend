package com.journeyplus.iam.dto;

import com.journeyplus.iam.entity.User;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String name;
    private String phone;
    private String departmentId;
    private String gradeId;
    private String status;
    private String approvalStatus;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private Long delegateApproverId;
    private LocalDateTime delegationStart;
    private LocalDateTime delegationEnd;

    public UserResponse() {}

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.departmentId = user.getDepartmentId();
        this.gradeId = user.getGrade() != null ? user.getGrade().getId() : null;
        this.status = user.getStatus();
        this.approvalStatus = user.getApprovalStatus();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
        this.delegateApproverId = user.getDelegateApprover() != null ? user.getDelegateApprover().getId() : null;
        this.delegationStart = user.getDelegationStart();
        this.delegationEnd = user.getDelegationEnd();
    }
}
