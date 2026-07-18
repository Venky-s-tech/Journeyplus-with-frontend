package com.journeyplus.iam.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserApprovalController {

    private final UserRepository userRepository;

    public AdminUserApprovalController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingUsers() {
        // Only users whose approval status is PENDING (rejected users are now retained but excluded here).
        List<User> pending = userRepository.findAll().stream()
                .filter(u -> "PENDING".equalsIgnoreCase(u.getApprovalStatus()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setActive(true);
        user.setApprovalStatus("APPROVED");
        userRepository.save(user);
        return ResponseEntity.ok("User approved");
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Retain the record so the user appears under "Rejected" and gets a rejection message on login.
        user.setActive(false);
        user.setApprovalStatus("REJECTED");
        userRepository.save(user);
        return ResponseEntity.ok("User rejected");
    }
}
