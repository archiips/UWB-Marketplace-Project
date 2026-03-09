package com.dawgs.marketplace.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil - token generation and validation.
 *
 * We use ReflectionTestUtils to inject the @Value fields without needing
 * a Spring context, which makes these tests fast and simple.
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Has to be at least 32 characters for HMAC-SHA256
    private static final String TEST_SECRET = "this-is-a-test-secret-key-that-is-long-enough-for-hmac!!";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L); // 24 hours
    }

    @Test
    void testGenerateTokenReturnsNonEmptyString() {
        String token = jwtUtil.generateToken("user123", "student@uw.edu");
        assertNotNull(token);
        assertFalse(token.isBlank(), "Generated token should not be blank");
    }

    @Test
    void testGeneratedTokenHasThreeParts() {
        // JWTs always have the format: header.payload.signature
        String token = jwtUtil.generateToken("user123", "student@uw.edu");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts separated by dots");
    }

    @Test
    void testExtractUserIdFromToken() {
        String userId = "google-sub-12345";
        String token = jwtUtil.generateToken(userId, "student@uw.edu");

        String extractedId = jwtUtil.extractUserId(token);
        assertEquals(userId, extractedId, "Should be able to extract the same userId we put in");
    }

    @Test
    void testExtractEmailFromToken() {
        String email = "student@uw.edu";
        String token = jwtUtil.generateToken("user123", email);

        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals(email, extractedEmail, "Should be able to extract the same email we put in");
    }

    @Test
    void testValidTokenPassesValidation() {
        String token = jwtUtil.generateToken("user123", "student@uw.edu");
        assertTrue(jwtUtil.isTokenValid(token), "Freshly generated token should be valid");
    }

    @Test
    void testGarbageStringIsNotValidToken() {
        assertFalse(jwtUtil.isTokenValid("this.is.not.a.jwt"));
    }

    @Test
    void testEmptyStringIsNotValidToken() {
        assertFalse(jwtUtil.isTokenValid(""));
    }

    @Test
    void testTamperedSignatureIsInvalid() {
        String token = jwtUtil.generateToken("user123", "student@uw.edu");
        // Mess up the signature part (last section after the final dot)
        String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "INVALIDSIGNATURE";
        assertFalse(jwtUtil.isTokenValid(tamperedToken), "Token with bad signature should not be valid");
    }

    @Test
    void testTokenSignedWithDifferentSecretIsInvalid() {
        // Generate a token with a different secret
        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "secret", "a-completely-different-secret-key-for-testing!!");
        ReflectionTestUtils.setField(otherUtil, "expirationMs", 86400000L);

        String tokenFromOtherSecret = otherUtil.generateToken("user123", "student@uw.edu");

        // Our jwtUtil (with original secret) should reject it
        assertFalse(jwtUtil.isTokenValid(tokenFromOtherSecret),
                "Token signed with a different secret should be rejected");
    }

    @Test
    void testExtractClaimsHasCorrectEmailClaim() {
        String token = jwtUtil.generateToken("user123", "archit@uw.edu");
        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("archit@uw.edu", claims.get("email", String.class));
    }

    @Test
    void testExtractClaimsHasCorrectSubject() {
        String token = jwtUtil.generateToken("my-user-id", "test@uw.edu");
        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("my-user-id", claims.getSubject());
    }

    @Test
    void testExpiredTokenIsRejected() throws InterruptedException {
        // Override expiration to 1ms so it expires almost immediately
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 1L);
        String token = jwtUtil.generateToken("user123", "student@uw.edu");

        Thread.sleep(50); // wait 50ms to make sure it's expired

        assertFalse(jwtUtil.isTokenValid(token), "Expired token should not be valid");
    }

    @Test
    void testDifferentUsersGetDifferentTokens() {
        String token1 = jwtUtil.generateToken("user-aaa", "user1@uw.edu");
        String token2 = jwtUtil.generateToken("user-bbb", "user2@uw.edu");

        assertNotEquals(token1, token2, "Different users should get different tokens");
    }

    @Test
    void testExtractUserIdMatchesForMultipleUsers() {
        String[] userIds = {"user-001", "user-002", "user-003"};
        for (String userId : userIds) {
            String token = jwtUtil.generateToken(userId, "test@uw.edu");
            assertEquals(userId, jwtUtil.extractUserId(token));
        }
    }
}
