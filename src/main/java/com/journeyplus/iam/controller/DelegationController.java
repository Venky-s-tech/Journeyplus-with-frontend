package com.journeyplus.iam.controller;

import com.journeyplus.iam.dto.DelegateRequest;
import com.journeyplus.iam.dto.UserResponse;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delegations")
@PreAuthorize("isAuthenticated()")
public class DelegationController {

    private final UserService userService;

    public DelegationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> setDelegation(
            @Valid @RequestBody DelegateRequest request,
            @AuthenticationPrincipal User currentUser) {
        Long delegatorId = currentUser.getId();
        UserResponse updated = userService.setDelegation(delegatorId, request);
        return ResponseEntity.ok(updated);
    }
}
