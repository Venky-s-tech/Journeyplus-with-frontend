package com.journeyplus.iam.controller;

import com.journeyplus.iam.dto.UserCreateRequest;
import com.journeyplus.iam.dto.UserResponse;
import com.journeyplus.iam.dto.UserUpdateRequest;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        UserResponse currentUser = userService.getUserResponseById(user.getId());
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You do not have permission to view this profile");
        }

        return ResponseEntity.ok(userService.getUserResponseById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String gradeId,
            @RequestParam(required = false) Boolean active
    ) {
        List<UserResponse> users = userService.searchUsers(email, name, role, gradeId, active);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You do not have permission to update this profile");
        }

        // If not admin, strip out privileged updates
        if (!isAdmin) {
            request.setRole(null);
            request.setGradeId(null);
            request.setStatus(null);
        }

        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        UserResponse deactivated = userService.deactivateUser(id);
        return ResponseEntity.ok(deactivated);
    }

    @PostMapping("/delegate")
    public ResponseEntity<UserResponse> setDelegation(
            @Valid @RequestBody com.journeyplus.iam.dto.DelegateRequest request,
            @AuthenticationPrincipal User currentUser) {
        UserResponse updated = userService.setDelegation(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }
}
