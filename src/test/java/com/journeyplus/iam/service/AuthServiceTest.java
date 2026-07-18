package com.journeyplus.iam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.journeyplus.config.JwtTokenProvider;
import com.journeyplus.iam.dto.AuthRequest;
import com.journeyplus.iam.dto.AuthResponse;
import com.journeyplus.iam.dto.RegisterRequest;
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_Success_EmployeeRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("empUser");
        request.setEmail("emp@journeyplus.com");
        request.setPassword("password123");
        request.setRole(Role.EMPLOYEE);
        request.setName("Employee Name");
        request.setPhone("1234567890");
        request.setDepartmentId("Engineering");

        Grade grade = new Grade("G1", "Junior Employee", "Junior Level", "Active");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(gradeRepository.findById("G1")).thenReturn(Optional.of(grade));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        
        User savedUser = new User(
                request.getUsername(),
                request.getEmail(),
                "encodedPassword",
                request.getRole(),
                request.getName(),
                request.getPhone(),
                request.getDepartmentId(),
                request.getDepartmentId(),
                grade
        );
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(request);

        assertNotNull(result);
        assertEquals("empUser", result.getUsername());
        assertTrue(result.isActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void register_Success_OtherRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("managerUser");
        request.setEmail("manager@journeyplus.com");
        request.setPassword("password123");
        request.setRole(Role.APPROVING_MANAGER);
        request.setName("Manager Name");
        request.setPhone("1234567890");
        request.setDepartmentId("Engineering");

        Grade grade = new Grade("G3", "Manager", "Manager Level", "Active");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(gradeRepository.findById("G3")).thenReturn(Optional.of(grade));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User(
                request.getUsername(),
                request.getEmail(),
                "encodedPassword",
                request.getRole(),
                request.getName(),
                request.getPhone(),
                request.getDepartmentId(),
                request.getDepartmentId(),
                grade
        );
        savedUser.setActive(false);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(request);

        assertNotNull(result);
        assertEquals("managerUser", result.getUsername());
        assertFalse(result.isActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void register_AutoAssignsGradeFromRole_IgnoringClientGrade() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mgrUser");
        request.setEmail("mgruser@journeyplus.com");
        request.setPassword("password123");
        request.setRole(Role.APPROVING_MANAGER);
        request.setName("Manager User");
        request.setPhone("1234567890");
        request.setDepartmentId("Engineering");

        Grade g3 = new Grade("G3", "Manager", "Manager Level", "Active");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(gradeRepository.findById("G3")).thenReturn(Optional.of(g3));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register(request);

        assertNotNull(result.getGrade());
        assertEquals("G3", result.getGrade().getId()); // mapped from APPROVING_MANAGER, not the client's G1
        verify(gradeRepository, times(1)).findById("G3");
        verify(gradeRepository, never()).findById("G1");
    }

    @Test
    public void register_ThrowsException_ForTravelAdmin() {
        RegisterRequest request = new RegisterRequest();
        request.setRole(Role.ADMIN);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        assertEquals("Registration as ADMIN is not allowed", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void register_ThrowsException_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setRole(Role.EMPLOYEE);

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void register_ThrowsException_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@journeyplus.com");
        request.setRole(Role.EMPLOYEE);

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void login_Success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testUser");
        request.setPassword("password123");

        User user = new User("testUser", "test@journeyplus.com", "encodedPassword", Role.EMPLOYEE, "Engineering");
        user.setActive(true);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("accessTokenVal");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refreshTokenVal");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessTokenVal", response.getAccessToken());
        assertEquals("refreshTokenVal", response.getRefreshToken());
        assertEquals("testUser", response.getUsername());
        assertEquals("EMPLOYEE", response.getRole());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void login_ThrowsException_PendingApproval() {
        AuthRequest request = new AuthRequest();
        request.setUsername("pendingUser");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Disabled"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.login(request);
        });

        assertEquals("Your account is pending admin approval. Please wait until an administrator approves your account.", exception.getMessage());
    }

    @Test
    public void login_ThrowsException_Rejected() {
        AuthRequest request = new AuthRequest();
        request.setUsername("rejectedUser");
        request.setPassword("password123");

        User rejected = new User("rejectedUser", "rejected@journeyplus.com", "encodedPassword", Role.APPROVING_MANAGER, "Engineering");
        rejected.setActive(false);
        rejected.setApprovalStatus("REJECTED");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Disabled"));
        when(userRepository.findByUsername("rejectedUser")).thenReturn(Optional.of(rejected));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.login(request);
        });

        assertEquals("Your account registration has been rejected. Please contact your administrator for assistance.", exception.getMessage());
    }

    @Test
    public void login_ThrowsException_InvalidCredentials() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testUser");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    public void login_ThrowsException_UserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setUsername("nonExistentUser");
        request.setPassword("password123");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void login_ThrowsException_DeactivatedUser() {
        AuthRequest request = new AuthRequest();
        request.setUsername("deactivatedUser");
        request.setPassword("password123");

        User user = new User("deactivatedUser", "test@journeyplus.com", "encodedPassword", Role.EMPLOYEE, "Engineering");
        user.setActive(false);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.login(request);
        });

        assertEquals("User account is deactivated", exception.getMessage());
    }

    @Test
    public void refresh_Success() {
        String refreshToken = "validRefreshToken";
        User user = new User("testUser", "test@journeyplus.com", "encodedPassword", Role.EMPLOYEE, "Engineering");

        when(jwtTokenProvider.extractUsername(refreshToken)).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.validateToken(refreshToken, user)).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("newAccessTokenVal");

        AuthResponse response = authService.refresh(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessTokenVal", response.getAccessToken());
        assertEquals("validRefreshToken", response.getRefreshToken());
        assertEquals("testUser", response.getUsername());
    }

    @Test
    public void refresh_ThrowsException_UserNotFound() {
        String refreshToken = "someToken";
        when(jwtTokenProvider.extractUsername(refreshToken)).thenReturn("unknownUser");
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.refresh(refreshToken);
        });

        assertEquals("User not found from refresh token", exception.getMessage());
    }

    @Test
    public void refresh_ThrowsException_InvalidToken() {
        String refreshToken = "invalidRefreshToken";
        User user = new User("testUser", "test@journeyplus.com", "encodedPassword", Role.EMPLOYEE, "Engineering");

        when(jwtTokenProvider.extractUsername(refreshToken)).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.validateToken(refreshToken, user)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.refresh(refreshToken);
        });

        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }
}
