package com.journeyplus.iam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.journeyplus.config.AuditAction;
import com.journeyplus.iam.dto.UserCreateRequest;
import com.journeyplus.iam.dto.UserResponse;
import com.journeyplus.iam.dto.UserUpdateRequest;
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.iam.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            GradeRepository gradeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) {
        log.info("Fetching user details for username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public User getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    public UserResponse getUserResponseById(Long id) {
        return new UserResponse(getUserById(id));
    }

    public List<UserResponse> searchUsers(String email, String name, Role role, String gradeId, Boolean active) {
        log.info("Searching users with filters - email: {}, name: {}, role: {}, gradeId: {}, active: {}",
                email, name, role, gradeId, active);
        return userRepository.searchUsers(email, name, role, gradeId, active)
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    @AuditAction(module = "IAM", action = "CREATE_USER")
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Admin creating user with username: {}, role: {}", request.getUsername(), request.getRole());

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new IllegalArgumentException("Grade ID '" + request.getGradeId() + "' does not exist"));

        User user = new User(
                request.getUsername().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                request.getName().trim(),
                request.getPhone().trim(),
                request.getDepartmentId().trim(),
                request.getDepartmentId().trim(),
                grade
        );

        user.setStatus(request.getStatus());

        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    @Transactional
    @AuditAction(module = "IAM", action = "UPDATE_USER")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        User user = getUserById(id);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(newEmail);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            user.setDepartmentId(request.getDepartmentId().trim());
            user.setDepartment(request.getDepartmentId().trim());
        }

        if (request.getGradeId() != null && !request.getGradeId().isBlank()) {
            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new IllegalArgumentException("Grade ID '" + request.getGradeId() + "' does not exist"));
            user.setGrade(grade);
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    @Transactional
    @AuditAction(module = "IAM", action = "DEACTIVATE_USER")
    public UserResponse deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        User user = getUserById(id);
        user.setActive(false);
        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    @Transactional
    @AuditAction(module = "IAM", action = "SET_DELEGATION")
    public UserResponse setDelegation(Long userId, com.journeyplus.iam.dto.DelegateRequest request) {
        log.info("Setting delegation for user ID: {}", userId);
        User user = getUserById(userId);

        if (request.getDelegateApproverId() == null) {
            user.setDelegateApprover(null);
            user.setDelegationStart(null);
            user.setDelegationEnd(null);
        } else {
            User delegate = getUserById(request.getDelegateApproverId());
            if (delegate.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Cannot delegate approval authority to yourself");
            }
            if (request.getDelegationStart() == null || request.getDelegationEnd() == null) {
                throw new IllegalArgumentException("Start and end times are required for delegation");
            }
            if (request.getDelegationEnd().isBefore(request.getDelegationStart())) {
                throw new IllegalArgumentException("Delegation end time must be after start time");
            }
            user.setDelegateApprover(delegate);
            user.setDelegationStart(request.getDelegationStart());
            user.setDelegationEnd(request.getDelegationEnd());
        }

        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }
}
