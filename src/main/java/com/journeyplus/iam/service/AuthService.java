package com.journeyplus.iam.service;

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
        // Prevent creation of admin accounts via public registration
        if (request.getRole() == Role.TRAVEL_ADMIN) {
            throw new IllegalArgumentException("Registration as TRAVEL_ADMIN is not allowed");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
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
        } else {
            user.setActive(false);
        }

        return userRepository.save(user);
    }

    @AuditAction(module = "IAM", action = "USER_LOGIN")
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.DisabledException de) {
            // The account exists but is disabled (pending approval)
            throw new IllegalStateException("Account pending approval. Waiting for admin approval.");
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is deactivated");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }

    public AuthResponse refresh(String refreshToken) {
        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found from refresh token"));

        if (!jwtTokenProvider.validateToken(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        return new AuthResponse(
                newAccessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }
}
