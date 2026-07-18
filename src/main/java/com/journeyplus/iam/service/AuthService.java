package com.journeyplus.iam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import java.util.List;

import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.notification.entity.Notification;
import com.journeyplus.notification.entity.NotificationCategory;
import com.journeyplus.notification.repository.NotificationRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final NotificationRepository notificationRepository;

    public AuthService(
            UserRepository userRepository,
            GradeRepository gradeRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    @AuditAction(module = "IAM", action = "REGISTER")
    public User register(RegisterRequest request) {
        log.info("Attempting to register user with username: {}, role: {}", request.getUsername(), request.getRole());

        // Prevent creation of admin accounts via public registration
        if (request.getRole() == Role.ADMIN) {
            log.warn("Registration failed: ADMIN role is not allowed for public registration, username: {}", request.getUsername());
            throw new IllegalArgumentException("Registration as ADMIN is not allowed");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username '{}' already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email '{}' already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Bug #1: Grade is auto-assigned from the predefined role -> grade mapping and enforced
        // server-side. Any grade supplied by the client is ignored so the relationship cannot be tampered with.
        String mappedGradeId = request.getRole().getDefaultGradeId();
        Grade grade = gradeRepository.findById(mappedGradeId)
                .orElseThrow(() -> new IllegalArgumentException("Configured grade '" + mappedGradeId + "' for role " + request.getRole() + " does not exist"));

        // Create User entity
        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword()), // BCrypt strength 12 encoded
                request.getRole(),
                request.getName().trim(),
                request.getPhone().trim(),
                request.getDepartmentId().trim(),
                request.getDepartmentId().trim(), // Set department to departmentId for backward compatibility
                grade
        );

        // Auto-approve EMPLOYEE registrations; other roles remain pending for admin approval
        if (request.getRole() == Role.EMPLOYEE) {
            user.setActive(true);
            user.setApprovalStatus("APPROVED");
            log.info("User '{}' auto-approved (Role: EMPLOYEE)", request.getUsername());
        } else {
            user.setActive(false);
            user.setApprovalStatus("PENDING");
            log.info("User '{}' registered as inactive, pending admin approval (Role: {})", request.getUsername(), request.getRole());
        }

        User savedUser = userRepository.save(user);
        log.info("User '{}' successfully registered with ID: {}", savedUser.getUsername(), savedUser.getId());

        // Bug #3: notify administrators when a non-employee registration needs approval.
        if (request.getRole() != Role.EMPLOYEE) {
            notifyAdminsOfPendingApproval(savedUser);
        }
        return savedUser;
    }

    /**
     * Bug #3: creates a single in-app notification per administrator for a newly registered
     * user awaiting approval. Exactly one notification is created per admin, so no duplicates.
     */
    private void notifyAdminsOfPendingApproval(User pendingUser) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            String title = "New user pending approval";
            String message = pendingUser.getName() + " (" + pendingUser.getUsername() + ") registered as "
                    + pendingUser.getRole().name() + " and is awaiting your approval.";
            for (User admin : admins) {
                Notification notification = new Notification(
                        admin, title, message, pendingUser.getId(), pendingUser.getUsername());
                notification.setCategory(NotificationCategory.Compliance);
                notificationRepository.save(notification);
            }
            log.info("Created pending-approval notifications for {} admin(s) regarding user '{}'",
                    admins.size(), pendingUser.getUsername());
        } catch (Exception e) {
            // Notification failure must not roll back a successful registration.
            log.error("Failed to create admin pending-approval notifications for user '{}': {}",
                    pendingUser.getUsername(), e.getMessage());
        }
    }

    @AuditAction(module = "IAM", action = "LOGIN")
    public AuthResponse login(AuthRequest request) {
        log.info("Authentication attempt for username: {}", request.getUsername());
        try {
            UsernamePasswordAuthenticationToken upa = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            System.out.println(upa);
            authenticationManager.authenticate(upa);

        } catch (org.springframework.security.authentication.DisabledException de) {
            // Bug #2: distinguish pending vs rejected vs deactivated so the user sees a meaningful message.
            String approvalStatus = userRepository.findByUsername(request.getUsername())
                    .map(User::getApprovalStatus)
                    .orElse("PENDING");
            if ("REJECTED".equalsIgnoreCase(approvalStatus)) {
                log.warn("Authentication failed for username: {} - Account rejected", request.getUsername());
                throw new IllegalStateException("Your account registration has been rejected. Please contact your administrator for assistance.");
            }
            if ("PENDING".equalsIgnoreCase(approvalStatus)) {
                log.warn("Authentication failed for username: {} - Account pending approval", request.getUsername());
                throw new IllegalStateException("Your account is pending admin approval. Please wait until an administrator approves your account.");
            }
            log.warn("Authentication failed for username: {} - Account deactivated", request.getUsername());
            throw new IllegalStateException("Your account has been deactivated. Please contact your administrator.");
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
