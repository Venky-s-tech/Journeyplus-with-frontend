package com.journeyplus.iam.dto;

import com.journeyplus.iam.entity.User;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;

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
    private List<String> permissions = new ArrayList<>();

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
        this.permissions = derivePermissions(user.getRole());
    }

    private static List<String> derivePermissions(com.journeyplus.iam.entity.Role role) {
        List<String> perms = new ArrayList<>();
        if (role == null) return perms;
        switch (role) {
            case ADMIN:
                perms.add("*");
                perms.add("user:read"); perms.add("user:write");
                perms.add("policy:read"); perms.add("policy:write");
                perms.add("trip:read"); perms.add("trip:write");
                perms.add("advance:read"); perms.add("advance:write");
                perms.add("expense:read"); perms.add("expense:write");
                perms.add("compliance:read"); perms.add("compliance:write");
                break;
            case EMPLOYEE:
                perms.add("trip:create"); perms.add("trip:view_own");
                perms.add("advance:request"); perms.add("expense:create");
                perms.add("notification:view");
                break;
            case APPROVING_MANAGER:
                perms.add("trip:approve"); perms.add("advance:approve");
                perms.add("expense:approve"); perms.add("delegation:manage");
                perms.add("notification:view");
                break;
            case TRAVEL_DESK:
                perms.add("itinerary:manage"); perms.add("visa:manage");
                perms.add("trip:view"); perms.add("notification:view");
                break;
            case FINANCE:
                perms.add("advance:disburse"); perms.add("reimbursement:process");
                perms.add("settlement:review"); perms.add("reports:view");
                perms.add("notification:view");
                break;
            case COMPLIANCE:
                perms.add("exception:view"); perms.add("exception:resolve");
                perms.add("audit:create"); perms.add("reports:view");
                perms.add("notification:view");
                break;
        }
        return perms;
    }
}
