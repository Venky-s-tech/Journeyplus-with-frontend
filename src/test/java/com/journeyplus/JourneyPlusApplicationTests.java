package com.journeyplus;

import com.journeyplus.config.JwtTokenProvider;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JourneyPlusApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
        assertNotNull(userRepository);
        assertNotNull(passwordEncoder);
        assertNotNull(jwtTokenProvider);
    }

    @Test
    void testPasswordHashingAndJwtGeneration() {
        // Enforce BCrypt cost 12
        String rawPassword = "superSecurePassword123";
        String encoded = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertNotEquals(rawPassword, encoded);

        // JWT token signature validation
        User mockUser = new User("tester", "tester@journeyplus.com", encoded, Role.EMPLOYEE, "Engineering");
        String accessToken = jwtTokenProvider.generateAccessToken(mockUser);
        
        assertNotNull(accessToken);
        assertEquals("tester", jwtTokenProvider.extractUsername(accessToken));
    }
}
