package com.dawgs.marketplace.controller;

import com.dawgs.marketplace.model.Listing;
import com.dawgs.marketplace.repository.ListingRepository;
import com.dawgs.marketplace.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ListingController.
 *
 * We use @SpringBootTest + MockMvc so we can test the real controller logic
 * with a real (H2) database. This lets us verify that the API actually
 * returns the right data and status codes.
 *
 * For authenticated endpoints we generate a real JWT and pass it in the
 * Authorization header, the same way the React frontend does.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // Test user info
    private static final String TEST_USER_ID = "test-user-google-sub-111";
    private static final String TEST_USER_EMAIL = "testuser@uw.edu";
    private static final String OTHER_USER_ID = "other-user-google-sub-222";

    private String authHeader;
    private String otherAuthHeader;

    @BeforeEach
    void setUp() {
        // Clear the database before each test so tests don't interfere with each other
        listingRepository.deleteAll();

        // Generate JWT tokens for our test users
        authHeader = "Bearer " + jwtUtil.generateToken(TEST_USER_ID, TEST_USER_EMAIL);
        otherAuthHeader = "Bearer " + jwtUtil.generateToken(OTHER_USER_ID, "other@uw.edu");
    }

    // ===== Helper method =====

    private Listing createTestListing(String title, String description, double price, String ownerId) {
        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription(description);
        listing.setPrice(price);
        listing.setOwnerId(ownerId);
        listing.setContactInfo("seller@uw.edu");
        return listingRepository.save(listing);
    }

    // ===== GET /api/listings (public) =====

    @Test
    void testGetAllListingsReturnsEmptyArrayWhenNoneExist() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllListingsReturnsAllListings() throws Exception {
        createTestListing("Laptop", "Good condition", 500.0, TEST_USER_ID);
        createTestListing("Textbook", "Math 124", 30.0, TEST_USER_ID);

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetAllListingsIsPublicNoAuthRequired() throws Exception {
        // This endpoint should work without a JWT token
        createTestListing("Chair", "Blue desk chair", 25.0, TEST_USER_ID);

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Chair")));
    }

    @Test
    void testSearchListingsByTitle() throws Exception {
        createTestListing("MacBook Pro", "Great laptop", 800.0, TEST_USER_ID);
        createTestListing("Calculus Textbook", "Used once", 40.0, TEST_USER_ID);

        mockMvc.perform(get("/api/listings?search=MacBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("MacBook Pro")));
    }

    @Test
    void testSearchListingsByDescription() throws Exception {
        createTestListing("Chair", "Very comfy ergonomic seat", 50.0, TEST_USER_ID);
        createTestListing("Desk", "Standing desk, adjustable height", 200.0, TEST_USER_ID);

        // Search by a word in the description
        mockMvc.perform(get("/api/listings?search=ergonomic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Chair")));
    }

    @Test
    void testSearchIsCaseInsensitive() throws Exception {
        createTestListing("Guitar", "Acoustic guitar, barely played", 150.0, TEST_USER_ID);

        mockMvc.perform(get("/api/listings?search=GUITAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testSearchWithNoMatchReturnsEmptyArray() throws Exception {
        createTestListing("Keyboard", "Mechanical keyboard", 80.0, TEST_USER_ID);

        mockMvc.perform(get("/api/listings?search=something-that-doesnt-exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testBlankSearchReturnAllListings() throws Exception {
        createTestListing("Item 1", "desc", 10.0, TEST_USER_ID);
        createTestListing("Item 2", "desc", 20.0, TEST_USER_ID);

        // Empty search string should behave the same as no search param
        mockMvc.perform(get("/api/listings?search="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ===== GET /api/listings/{id} (public) =====

    @Test
    void testGetListingByIdReturnsCorrectListing() throws Exception {
        Listing listing = createTestListing("Bike", "Mountain bike", 300.0, TEST_USER_ID);

        mockMvc.perform(get("/api/listings/" + listing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Bike")))
                .andExpect(jsonPath("$.price", is(300.0)));
    }

    @Test
    void testGetListingByIdReturns404ForNonexistentId() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/listings/" + randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetListingByIdIsPublicNoAuthRequired() throws Exception {
        Listing listing = createTestListing("Headphones", "Sony WH-1000XM4", 200.0, TEST_USER_ID);

        // Should work without Authorization header
        mockMvc.perform(get("/api/listings/" + listing.getId()))
                .andExpect(status().isOk());
    }

    // ===== GET /api/listings/my-listings (requires JWT) =====

    @Test
    void testGetMyListingsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/listings/my-listings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyListingsReturnsOnlyMyListings() throws Exception {
        createTestListing("My Laptop", "My description", 500.0, TEST_USER_ID);
        createTestListing("Other Laptop", "Other description", 600.0, OTHER_USER_ID);

        mockMvc.perform(get("/api/listings/my-listings")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("My Laptop")));
    }

    @Test
    void testGetMyListingsReturnsEmptyWhenUserHasNoListings() throws Exception {
        mockMvc.perform(get("/api/listings/my-listings")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ===== POST /api/listings (requires JWT) =====

    @Test
    void testCreateListingRequiresAuth() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("Test Item");
        listing.setDescription("Test description");
        listing.setPrice(10.0);

        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateListingSuccessfully() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("Used Textbook");
        listing.setDescription("MATH 308 Linear Algebra textbook");
        listing.setPrice(45.00);
        listing.setContactInfo("archit@uw.edu");

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Used Textbook")))
                .andExpect(jsonPath("$.price", is(45.0)))
                .andExpect(jsonPath("$.ownerId", is(TEST_USER_ID)))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateListingSetsOwnerIdFromJwt() throws Exception {
        // The ownerId should be set by the controller from the JWT, not from the request body
        Listing listing = new Listing();
        listing.setTitle("Camera");
        listing.setDescription("Canon DSLR");
        listing.setPrice(350.0);
        listing.setOwnerId("this-should-be-overwritten-by-server"); // should be ignored

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId", is(TEST_USER_ID)));
    }

    @Test
    void testCreateListingDefaultSoldIsFalse() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("Monitor");
        listing.setDescription("27 inch 4K monitor");
        listing.setPrice(250.0);

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sold", is(false)));
    }

    // ===== PUT /api/listings/{id} (requires JWT, owner only) =====

    @Test
    void testUpdateListingRequiresAuth() throws Exception {
        Listing listing = createTestListing("Old Item", "Old desc", 100.0, TEST_USER_ID);

        Listing patch = new Listing();
        patch.setTitle("New Item");

        mockMvc.perform(put("/api/listings/" + listing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateListingSuccessfully() throws Exception {
        Listing listing = createTestListing("Old Title", "Old description", 100.0, TEST_USER_ID);

        Listing patch = new Listing();
        patch.setTitle("Updated Title");
        patch.setDescription("Updated description");
        patch.setPrice(90.0);

        mockMvc.perform(put("/api/listings/" + listing.getId())
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.price", is(90.0)));
    }

    @Test
    void testUpdateListingReturnsForbiddenForNonOwner() throws Exception {
        // TEST_USER_ID owns this listing, OTHER_USER_ID should not be able to update it
        Listing listing = createTestListing("Someone Elses Item", "desc", 50.0, TEST_USER_ID);

        Listing patch = new Listing();
        patch.setTitle("Hacked Title");

        mockMvc.perform(put("/api/listings/" + listing.getId())
                        .header("Authorization", otherAuthHeader)  // different user!
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateListingMarkAsSold() throws Exception {
        Listing listing = createTestListing("Sold Item", "desc", 75.0, TEST_USER_ID);

        Listing patch = new Listing();
        patch.setSold(true);

        mockMvc.perform(put("/api/listings/" + listing.getId())
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sold", is(true)));
    }

    @Test
    void testUpdateListingReturns404ForNonexistentId() throws Exception {
        Listing patch = new Listing();
        patch.setTitle("Doesnt Matter");

        mockMvc.perform(put("/api/listings/" + UUID.randomUUID())
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    // ===== DELETE /api/listings/{id} (requires JWT, owner only) =====

    @Test
    void testDeleteListingRequiresAuth() throws Exception {
        Listing listing = createTestListing("To Delete", "desc", 10.0, TEST_USER_ID);

        mockMvc.perform(delete("/api/listings/" + listing.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteListingSuccessfully() throws Exception {
        Listing listing = createTestListing("Delete Me", "desc", 10.0, TEST_USER_ID);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        // Verify it's actually gone from the database
        assertEquals(0, listingRepository.count());
    }

    @Test
    void testDeleteListingReturnsForbiddenForNonOwner() throws Exception {
        Listing listing = createTestListing("Not Yours", "desc", 30.0, TEST_USER_ID);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
                        .header("Authorization", otherAuthHeader))  // different user!
                .andExpect(status().isForbidden());

        // Make sure the listing is still there
        assertEquals(1, listingRepository.count());
    }

    @Test
    void testDeleteListingReturns404ForNonexistentId() throws Exception {
        mockMvc.perform(delete("/api/listings/" + UUID.randomUUID())
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    // ===== Health check =====

    @Test
    void testHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
}
