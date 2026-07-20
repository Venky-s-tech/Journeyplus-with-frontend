package com.journeyplus.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.repository.TripRequestRepository;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TripRequestRepository tripRequestRepository;

    public DataLoader(
            UserRepository userRepository,
            GradeRepository gradeRepository,
            PasswordEncoder passwordEncoder,
            TripRequestRepository tripRequestRepository) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.passwordEncoder = passwordEncoder;
        this.tripRequestRepository = tripRequestRepository;
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

        Grade g2 = gradeRepository.findById("G2").orElse(null);
        Grade g3 = gradeRepository.findById("G3").orElse(null);
        Grade g6 = gradeRepository.findById("G6").orElse(null);

        // 2. Seed admin accounts if they do not exist
        if (!userRepository.existsByUsername("admin1")) {
            User a1 = new User(
                    "admin1",
                    "admin1@example.com",
                    passwordEncoder.encode("Admin@123"),
                    Role.ADMIN,
                    "Admin One",
                    "+1-555-0100",
                    "Engineering",
                    "Engineering",
                    g6
            );
            a1.setActive(true);
            userRepository.save(a1);
        }

        // 3. Seed Manager account (mgr1)
        User manager = userRepository.findByUsername("mgr1").orElse(null);
        if (manager == null) {
            manager = new User(
                    "mgr1",
                    "mgr1@example.com",
                    passwordEncoder.encode("Password@123"),
                    Role.APPROVING_MANAGER,
                    "Sarah Manager",
                    "+1-555-0200",
                    "Engineering",
                    "Engineering",
                    g3
            );
            manager.setActive(true);
            manager = userRepository.save(manager);
        }

        // 4. Seed Employee account (emp1)
        User employee = userRepository.findByUsername("emp1").orElse(null);
        if (employee == null) {
            employee = new User(
                    "emp1",
                    "emp1@example.com",
                    passwordEncoder.encode("Password@123"),
                    Role.EMPLOYEE,
                    "John Employee",
                    "+1-555-0300",
                    "Engineering",
                    "Engineering",
                    g2
            );
            employee.setActive(true);
            employee = userRepository.save(employee);
        }

        // 5. Seed Travel Desk account (td1)
        if (!userRepository.existsByUsername("td1")) {
            User td = new User(
                    "td1",
                    "td1@example.com",
                    passwordEncoder.encode("Password@123"),
                    Role.TRAVEL_DESK,
                    "Travel Desk Officer",
                    "+1-555-0400",
                    "Travel Desk",
                    "Travel Desk",
                    g3
            );
            td.setActive(true);
            userRepository.save(td);
        }

        // 6. Seed Finance account (fin1)
        if (!userRepository.existsByUsername("fin1")) {
            User fin = new User(
                    "fin1",
                    "fin1@example.com",
                    passwordEncoder.encode("Password@123"),
                    Role.FINANCE,
                    "Finance Officer",
                    "+1-555-0500",
                    "Finance",
                    "Finance",
                    g3
            );
            fin.setActive(true);
            userRepository.save(fin);
        }

        // 7. Seed Compliance account (comp1)
        if (!userRepository.existsByUsername("comp1")) {
            User comp = new User(
                    "comp1",
                    "comp1@example.com",
                    passwordEncoder.encode("Password@123"),
                    Role.COMPLIANCE,
                    "Compliance Officer",
                    "+1-555-0600",
                    "Compliance",
                    "Compliance",
                    g3
            );
            comp.setActive(true);
            userRepository.save(comp);
        }

        // 8. Seed sample manager-approved trips for Travel Desk queue if database is empty
        if (tripRequestRepository.count() == 0 && employee != null && manager != null) {
            TripRequest t1 = new TripRequest();
            t1.setEmployee(employee);
            t1.setApprover(manager);
            t1.setDestination("New York, USA");
            t1.setPurpose("Annual Tech Conference & Executive Meeting");
            t1.setTravelType("INTERNATIONAL");
            t1.setDepartureDate(LocalDate.now().plusDays(7));
            t1.setReturnDate(LocalDate.now().plusDays(14));
            t1.setEstimatedCost(new BigDecimal("2500.00"));
            t1.setStatus(TripStatus.APPROVED);
            t1.setBookingStatus("PENDING_BOOKING");
            t1.setWorkflowStage("TRAVEL_DESK");
            t1.setTravelDeskStatus("QUEUED");
            t1.setComments("Manager approved. Approved for international conference.");
            tripRequestRepository.save(t1);

            TripRequest t2 = new TripRequest();
            t2.setEmployee(employee);
            t2.setApprover(manager);
            t2.setDestination("Chicago, IL");
            t2.setPurpose("Client Onsite Architecture Review");
            t2.setTravelType("DOMESTIC");
            t2.setDepartureDate(LocalDate.now().plusDays(3));
            t2.setReturnDate(LocalDate.now().plusDays(6));
            t2.setEstimatedCost(new BigDecimal("950.00"));
            t2.setStatus(TripStatus.APPROVED);
            t2.setBookingStatus("PENDING_BOOKING");
            t2.setWorkflowStage("TRAVEL_DESK");
            t2.setTravelDeskStatus("QUEUED");
            t2.setComments("Manager approved. Approved for domestic client review.");
            tripRequestRepository.save(t2);
        }
    }

    private void seedGrade(String id, String name, String desc) {
        if (!gradeRepository.existsById(id)) {
            Grade grade = new Grade(id, name, desc, "Active");
            gradeRepository.save(grade);
        }
    }
}
