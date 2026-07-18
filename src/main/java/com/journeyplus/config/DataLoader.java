package com.journeyplus.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.iam.repository.UserRepository;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(
            UserRepository userRepository,
            GradeRepository gradeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed default Grade data
        seedGrade("G1", "Junior Employee", "Junior level staff");
        seedGrade("G2", "Senior Employee", "Senior level staff");
        seedGrade("G3", "Manager", "Mid-level manager");
        seedGrade("G4", "Senior Manager", "Senior level manager");
        seedGrade("G5", "Director", "Director level executive");
        seedGrade("G6", "Executive / VP", "Executive or Vice President level");

        Grade g6 = gradeRepository.findById("G6").orElse(null);

        // 2. Seed two admin accounts if they do not exist
        if (!userRepository.existsByUsername("admin1")) {
            User a1 = new User(
                    "admin1",
                    "admin1@example.com",
                    passwordEncoder.encode("Admin@123"),
                    Role.ADMIN,
                    "Admin One",
                    "+1-555-0100",
                    "DEPT-ADMIN",
                    "DEPT-ADMIN",
                    g6
            );
            a1.setActive(true);
            userRepository.save(a1);
        }

        if (!userRepository.existsByUsername("admin2")) {
            User a2 = new User(
                    "admin2",
                    "admin2@example.com",
                    passwordEncoder.encode("Admin@124"),
                    Role.ADMIN,
                    "Admin Two",
                    "+1-555-0101",
                    "DEPT-ADMIN",
                    "DEPT-ADMIN",
                    g6
            );
            a2.setActive(true);
            userRepository.save(a2);
        }
    }

    private void seedGrade(String id, String name, String desc) {
        if (!gradeRepository.existsById(id)) {
            Grade grade = new Grade(id, name, desc, "Active");
            gradeRepository.save(grade);
        }
    }
}
