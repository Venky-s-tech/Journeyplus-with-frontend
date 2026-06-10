package com.journeyplus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed two travel admin accounts if they do not exist
        if (!userRepository.existsByUsername("admin1")) {
            User a1 = new User("admin1", "admin1@example.com",
                    passwordEncoder.encode("Admin@123"), Role.TRAVEL_ADMIN, "Administration");
            a1.setActive(true);
            userRepository.save(a1);
        }

        if (!userRepository.existsByUsername("admin2")) {
            User a2 = new User("admin2", "admin2@example.com",
                    passwordEncoder.encode("Admin@124"), Role.TRAVEL_ADMIN, "Administration");
            a2.setActive(true);
            userRepository.save(a2);
        }
    }
}
