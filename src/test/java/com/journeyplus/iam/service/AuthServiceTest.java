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
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_Success_EmployeeRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("empUser");
        request.setEmail("emp@journeyplus.com");
        request.setPassword("password123");
        request.setRole(Role.EMPLOYEE);
        request.setDepartment("Engineering");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        
        User savedUser = new User(
                request.getUsername(),
                request.getEmail(),
                "encodedPassword",
                request.getRole(),
                request.getDepartment()
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
        request.setDepartment("Engineering");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User(
                request.getUsername(),
                request.getEmail(),
                "encodedPassword",
                request.getRole(),
                request.getDepartment()
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
    public void register_ThrowsException_ForTravelAdmin() {
        RegisterRequest request = new RegisterRequest();
        request.setRole(Role.TRAVEL_ADMIN);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        assertEquals("Registration as TRAVEL_ADMIN is not allowed", exception.getMessage());
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

        assertEquals("Account pending approval. Waiting for admin approval.", exception.getMessage());
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
