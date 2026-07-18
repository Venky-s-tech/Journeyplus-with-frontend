package com.journeyplus.iam.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journeyplus.iam.dto.RoleUpdateRequest;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
public class AdminRoleController {

    private final UserRepository userRepository;

    public AdminRoleController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setRole(@PathVariable Long id, @RequestBody RoleUpdateRequest body) {
        Optional<User> opt = userRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = opt.get();
        String roleStr = body != null ? body.getRole() : null;
        if (roleStr == null || roleStr.isBlank()) {
            return ResponseEntity.badRequest().body("Missing role");
        }

        try {
            Role r = Role.valueOf(roleStr);
            user.setRole(r);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid role: " + roleStr);
        }
    }

    @GetMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRole(@PathVariable Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(opt.get().getRole());
    }
}
