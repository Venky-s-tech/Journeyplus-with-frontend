package com.journeyplus.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.dto.TripRequestInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TravelDeskWorkflowProofTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void executeTravelDeskWorkflowSteps() throws Exception {
        System.out.println("==================================================");
        System.out.println("STARTING STEP-BY-STEP TRAVEL DESK WORKFLOW PROOF");
        System.out.println("==================================================");

        // Prepare users
        User emp = userRepository.findByUsername("emp1").orElseGet(() -> {
            User u = new User("emp1", "emp1@example.com", "pass", Role.EMPLOYEE, "Employee One", "+123", "Engineering", "Engineering", null);
            u.setActive(true);
            return userRepository.save(u);
        });

        User mgr = userRepository.findByUsername("mgr1").orElseGet(() -> {
            User u = new User("mgr1", "mgr1@example.com", "pass", Role.APPROVING_MANAGER, "Manager One", "+123", "Engineering", "Engineering", null);
            u.setActive(true);
            return userRepository.save(u);
        });

        User td = userRepository.findByUsername("td1").orElseGet(() -> {
            User u = new User("td1", "td1@example.com", "pass", Role.TRAVEL_DESK, "TravelDesk Officer", "+123", "Travel Desk", "Travel Desk", null);
            u.setActive(true);
            return userRepository.save(u);
        });

        // Step 1: Create Employee Trip
        TripRequestInput input = new TripRequestInput();
        input.setPurpose("Proof Workflow International Conference");
        input.setDestination("Tokyo, Japan");
        input.setTravelType("INTERNATIONAL");
        input.setDepartureDate(LocalDate.now().plusDays(10));
        input.setReturnDate(LocalDate.now().plusDays(15));
        input.setEstimatedCost(new BigDecimal("3500.00"));
        input.setApproverUsername(mgr.getUsername());
        input.setComments("Requesting international conference travel");

        MvcResult createResult = mockMvc.perform(post("/api/trips")
                .with(SecurityMockMvcRequestPostProcessors.user(emp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn();

        String createJson = createResult.getResponse().getContentAsString();
        Map<?, ?> tripCreated = objectMapper.readValue(createJson, Map.class);
        Long tripId = Long.valueOf(tripCreated.get("id").toString());
        System.out.println("\n--- STEP 1: CREATE TRIP RESPONSE ---");
        System.out.println(createJson);

        // Step 2: Submit Trip
        MvcResult submitResult = mockMvc.perform(post("/api/trips/" + tripId + "/submit")
                .with(SecurityMockMvcRequestPostProcessors.user(emp)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n--- STEP 2: SUBMIT TRIP RESPONSE ---");
        System.out.println(submitResult.getResponse().getContentAsString());

        // Step 3: Approve Trip as Manager
        MvcResult approveResult = mockMvc.perform(post("/api/trips/" + tripId + "/approve")
                .param("comments", "Approved for Tokyo trip")
                .with(SecurityMockMvcRequestPostProcessors.user(mgr)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("\n--- STEP 3: MANAGER APPROVE RESPONSE ---");
        System.out.println(approveResult.getResponse().getContentAsString());

        // Step 4: GET /api/trips?status=APPROVED
        MvcResult approvedTripsResult = mockMvc.perform(get("/api/trips")
                .param("status", "APPROVED")
                .with(SecurityMockMvcRequestPostProcessors.user(td)))
                .andExpect(status().isOk())
                .andReturn();
        String approvedTripsJson = approvedTripsResult.getResponse().getContentAsString();
        System.out.println("\n--- STEP 4: GET /api/trips?status=APPROVED JSON ---");
        System.out.println(approvedTripsJson);

        // Step 5: GET /api/dashboard/summary?role=TRAVEL_DESK
        MvcResult dashResult1 = mockMvc.perform(get("/api/dashboard/summary")
                .param("role", "TRAVEL_DESK")
                .with(SecurityMockMvcRequestPostProcessors.user(td)))
                .andExpect(status().isOk())
                .andReturn();
        String dashJson1 = dashResult1.getResponse().getContentAsString();
        System.out.println("\n--- STEP 5: GET /api/dashboard/summary?role=TRAVEL_DESK (BEFORE ITINERARY) JSON ---");
        System.out.println(dashJson1);

        // Step 6: POST /api/trips/{tripId}/itinerary
        Map<String, Object> legInput = new HashMap<>();
        legInput.put("origin", "New York");
        legInput.put("destination", "Tokyo");
        legInput.put("legType", "FLIGHT");
        legInput.put("travelDate", LocalDate.now().plusDays(10).toString());
        legInput.put("cost", 1200.00);
        legInput.put("originalCurrency", "USD");
        legInput.put("carrierDetails", "Japan Airlines JL005");
        legInput.put("bookingRef", "PNR-NRT-7788");

        MvcResult itineraryResult = mockMvc.perform(post("/api/trips/" + tripId + "/itinerary")
                .with(SecurityMockMvcRequestPostProcessors.user(td))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(legInput)))
                .andExpect(status().isOk())
                .andReturn();
        String itineraryJson = itineraryResult.getResponse().getContentAsString();
        System.out.println("\n--- STEP 6: POST /api/trips/" + tripId + "/itinerary RESPONSE ---");
        System.out.println(itineraryJson);

        // Step 7: GET /api/trips/{tripId}/travel-details
        MvcResult travelDetailsResult = mockMvc.perform(get("/api/trips/" + tripId + "/travel-details")
                .with(SecurityMockMvcRequestPostProcessors.user(emp)))
                .andExpect(status().isOk())
                .andReturn();
        String travelDetailsJson = travelDetailsResult.getResponse().getContentAsString();
        System.out.println("\n--- STEP 7: GET /api/trips/" + tripId + "/travel-details JSON ---");
        System.out.println(travelDetailsJson);

        // Step 8: GET /api/dashboard/summary?role=TRAVEL_DESK again
        MvcResult dashResult2 = mockMvc.perform(get("/api/dashboard/summary")
                .param("role", "TRAVEL_DESK")
                .with(SecurityMockMvcRequestPostProcessors.user(td)))
                .andExpect(status().isOk())
                .andReturn();
        String dashJson2 = dashResult2.getResponse().getContentAsString();
        System.out.println("\n--- STEP 8: GET /api/dashboard/summary?role=TRAVEL_DESK (AFTER ITINERARY) JSON ---");
        System.out.println(dashJson2);

        System.out.println("==================================================");
        System.out.println("COMPLETED STEP-BY-STEP PROOF EXECUTION");
        System.out.println("==================================================");
    }
}
