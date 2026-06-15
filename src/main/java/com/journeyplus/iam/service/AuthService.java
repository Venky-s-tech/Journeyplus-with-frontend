package com.journeyplus.iam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journeyplus.config.AuditAction;
import com.journeyplus.config.JwtTokenProvider;
import com.journeyplus.iam.dto.AuthRequest;
import com.journeyplus.iam.dto.AuthResponse;
import com.journeyplus.iam.dto.RegisterRequest;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    @AuditAction(module = "IAM", action = "USER_REGISTER")
    public User register(RegisterRequest request) {
        log.info("Attempting to register user with username: {}, role: {}", request.getUsername(), request.getRole());
        // Prevent creation of admin accounts via public registration
        if (request.getRole() == Role.TRAVEL_ADMIN) {
            log.warn("Registration failed: TRAVEL_ADMIN role is not allowed for public registration, username: {}", request.getUsername());
            throw new IllegalArgumentException("Registration as TRAVEL_ADMIN is not allowed");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username '{}' already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email '{}' already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), // BCrypt strength 12 encoded
                request.getRole(),
                request.getDepartment()
        );

        // Auto-approve EMPLOYEE registrations; other roles remain pending for admin approval
        if (request.getRole() == Role.EMPLOYEE) {
            user.setActive(true);
            log.info("User '{}' auto-approved (Role: EMPLOYEE)", request.getUsername());
        } else {
            user.setActive(false);
            log.info("User '{}' registered as inactive, pending admin approval (Role: {})", request.getUsername(), request.getRole());
        }

        User savedUser = userRepository.save(user);
        log.info("User '{}' successfully registered with ID: {}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @AuditAction(module = "IAM", action = "USER_LOGIN")
    public AuthResponse login(AuthRequest request) {
        log.info("Authentication attempt for username: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.DisabledException de) {
            log.warn("Authentication failed for username: {} - Account pending approval", request.getUsername());
            // The account exists but is disabled (pending approval)
            throw new IllegalStateException("Account pending approval. Waiting for admin approval.");
        } catch (Exception e) {
            log.warn("Authentication failed for username: {} - Invalid credentials", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("Authentication inconsistency: Authenticated user '{}' not found in database", request.getUsername());
                    return new IllegalArgumentException("User not found");
                });

        if (!user.isActive()) {
            log.warn("Login failed for username: {} - Account is deactivated", request.getUsername());
            throw new IllegalStateException("User account is deactivated");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("User '{}' successfully logged in. Role: {}", user.getUsername(), user.getRole());
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }

    public AuthResponse refresh(String refreshToken) {
        log.info("Token refresh attempt started");
        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: User '{}' not found from refresh token", username);
                    return new IllegalArgumentException("User not found from refresh token");
                });

        if (!jwtTokenProvider.validateToken(refreshToken, user)) {
            log.warn("Token refresh failed: Invalid or expired refresh token for user '{}'", username);
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        log.info("Token refresh successful for user '{}'", username);
        return new AuthResponse(
                newAccessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }
}
