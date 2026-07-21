package com.journeyplus.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.repository.TripRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TripCompletionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRequestRepository tripRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User employee1;
    private User employee2;
    private User travelDesk;

    @BeforeEach
    public void setUp() {
        employee1 = userRepository.findByUsername("securityEmp1").orElseGet(() -> {
            User u = new User("securityEmp1", "sec1@example.com", "pass", Role.EMPLOYEE, "Sec Emp 1", "+123", "Eng", "Eng", null);
            u.setActive(true);
            return userRepository.save(u);
        });

        employee2 = userRepository.findByUsername("securityEmp2").orElseGet(() -> {
            User u = new User("securityEmp2", "sec2@example.com", "pass", Role.EMPLOYEE, "Sec Emp 2", "+123", "Eng", "Eng", null);
            u.setActive(true);
            return userRepository.save(u);
        });

        travelDesk = userRepository.findByUsername("securityTd").orElseGet(() -> {
            User u = new User("securityTd", "sectd@example.com", "pass", Role.TRAVEL_DESK, "Sec TD", "+123", "TD", "TD", null);
            u.setActive(true);
            return userRepository.save(u);
        });
    }

    @Test
    public void testTripCompletionAuthorizationAndWorkflow() throws Exception {
        // Create a BOOKED trip owned by employee1
        TripRequest trip = new TripRequest();
        trip.setEmployee(employee1);
        trip.setPurpose("Security Validation");
        trip.setDestination("London");
        trip.setTravelType("DOMESTIC");
        trip.setDepartureDate(LocalDate.now().plusDays(1));
        trip.setReturnDate(LocalDate.now().plusDays(5));
        trip.setEstimatedCost(new BigDecimal("1000.00"));
        trip.setStatus(TripStatus.BOOKED);
        TripRequest savedTrip = tripRequestRepository.save(trip);

        // 1. Non-owner employee cannot complete
        mockMvc.perform(post("/api/trips/" + savedTrip.getId() + "/complete")
                .with(SecurityMockMvcRequestPostProcessors.user(employee2)))
                .andExpect(status().isForbidden());

        // 2. Travel Desk cannot complete via POST /complete
        mockMvc.perform(post("/api/trips/" + savedTrip.getId() + "/complete")
                .with(SecurityMockMvcRequestPostProcessors.user(travelDesk)))
                .andExpect(status().isForbidden());

        // 3. Travel Desk cannot complete via PATCH /status
        mockMvc.perform(patch("/api/trips/" + savedTrip.getId() + "/status")
                .with(SecurityMockMvcRequestPostProcessors.user(travelDesk))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isForbidden());

        // 4. Trip Owner can complete BOOKED trip
        mockMvc.perform(post("/api/trips/" + savedTrip.getId() + "/complete")
                .with(SecurityMockMvcRequestPostProcessors.user(employee1)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCannotCompleteApprovedTripWithoutBooking() throws Exception {
        // Create an APPROVED trip (not yet BOOKED) owned by employee1
        TripRequest trip = new TripRequest();
        trip.setEmployee(employee1);
        trip.setPurpose("Approved Only Validation");
        trip.setDestination("Paris");
        trip.setTravelType("DOMESTIC");
        trip.setDepartureDate(LocalDate.now().plusDays(1));
        trip.setReturnDate(LocalDate.now().plusDays(5));
        trip.setEstimatedCost(new BigDecimal("1000.00"));
        trip.setStatus(TripStatus.APPROVED);
        TripRequest savedTrip = tripRequestRepository.save(trip);

        // Employee attempting to complete APPROVED trip should be rejected by workflow rule (409 Conflict)
        mockMvc.perform(post("/api/trips/" + savedTrip.getId() + "/complete")
                .with(SecurityMockMvcRequestPostProcessors.user(employee1)))
                .andExpect(status().isConflict());
    }
}