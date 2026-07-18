package com.journeyplus.iam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journeyplus.iam.dto.AuthRequest;
import com.journeyplus.iam.dto.AuthResponse;
import com.journeyplus.iam.dto.RefreshRequest;
import com.journeyplus.iam.dto.RegisterRequest;
import com.journeyplus.iam.dto.UserResponse;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        // For any role other than EMPLOYEE, always return 'waiting for admin approval'
        if (request.getRole() != null && request.getRole() != com.journeyplus.iam.entity.Role.EMPLOYEE) {
            return ResponseEntity.status(202).body(
                    java.util.Map.of(
                            "message", "Registration received — waiting for admin approval",
                            "username", user.getUsername(),
                            "role", request.getRole().name()
                    )
            );
        }

        // Return secure UserResponse DTO to prevent password leakage
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
