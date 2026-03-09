package com.dawgs.marketplace.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 *
 * NOTE: We can't test the full Google OAuth flow in unit tests because it
 * requires a real token signed by Google's servers. Instead, we test the
 * cases we CAN control:
 *   - Missing or blank idToken → 400 Bad Request
 *   - Invalid/fake idToken string → 401 Unauthorized
 *
 * The full login flow is covered in Selenium end-to-end tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGoogleAuthEndpointIsReachable() throws Exception {
        // Just make sure the endpoint exists and responds (even if it rejects the request)
        // An empty body should give us a 400, not a 404 or 500
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingIdTokenReturnsBadRequest() throws Exception {
        // Body has no idToken field at all
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("idToken is required"));
    }

    @Test
    void testBlankIdTokenReturnsBadRequest() throws Exception {
        // Body has idToken but it's empty
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("idToken is required"));
    }

    @Test
    void testWhitespaceOnlyIdTokenReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("idToken is required"));
    }

    @Test
    void testFakeIdTokenReturnsUnauthorized() throws Exception {
        // A made-up string that looks vaguely like a JWT but isn't a real Google token
        String fakeToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImZha2UifQ.eyJzdWIiOiIxMjM0In0.fakesignature";

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"" + fakeToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid Google token"));
    }

    @Test
    void testRandomStringIdTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"not-a-real-google-token-just-a-random-string\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid Google token"));
    }

    @Test
    void testAuthEndpointIsPublicNoJwtNeeded() throws Exception {
        // The /api/auth/google endpoint must be accessible without a JWT
        // because it's how users GET a JWT in the first place
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                // We expect 400 (bad request) not 401 (unauthorized) -
                // meaning Spring Security let us through and the controller handled it
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthEndpointAcceptsJsonContentType() throws Exception {
        // Make sure the endpoint accepts JSON - this would fail with 415 if content type is wrong
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"test\"}"))
                .andExpect(status().isUnauthorized()); // 401, not 415 Unsupported Media Type
    }
}
