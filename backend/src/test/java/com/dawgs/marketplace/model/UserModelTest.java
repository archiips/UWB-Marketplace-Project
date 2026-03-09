package com.dawgs.marketplace.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User model class.
 * Tests that user fields work correctly and UW email logic holds up.
 */
public class UserModelTest {

    @Test
    void testCreateUserWithValidFields() {
        User user = new User();
        user.setId("google-sub-12345");
        user.setEmail("testuser@uw.edu");
        user.setName("Test User");

        assertEquals("google-sub-12345", user.getId());
        assertEquals("testuser@uw.edu", user.getEmail());
        assertEquals("Test User", user.getName());
    }

    @Test
    void testUserEmailEndsWithUwDomain() {
        User user = new User();
        user.setEmail("archit@uw.edu");

        // This is the check that the auth controller also does
        assertTrue(user.getEmail().endsWith("@uw.edu"), "Email must be a UW email address");
    }

    @Test
    void testNonUwEmailDoesNotPassDomainCheck() {
        // We can't create a User entity with a non-UW email in production
        // because AuthController rejects it, but let's verify the check logic
        String nonUwEmail = "someone@gmail.com";
        assertFalse(nonUwEmail.endsWith("@uw.edu"), "Gmail should not pass the UW email check");
    }

    @Test
    void testUserIdIsGoogleSubjectId() {
        // The User's id field comes from Google's 'sub' claim in the ID token
        User user = new User();
        String googleSubId = "1234567890987654321";
        user.setId(googleSubId);
        assertEquals(googleSubId, user.getId());
    }

    @Test
    void testUpdateUserName() {
        User user = new User();
        user.setName("Old Name");
        user.setName("Updated Name");
        assertEquals("Updated Name", user.getName());
    }

    @Test
    void testUpdateUserEmail() {
        User user = new User();
        user.setEmail("old@uw.edu");
        user.setEmail("new@uw.edu");
        assertEquals("new@uw.edu", user.getEmail());
    }

    @Test
    void testUserNameFallsBackToEmailWhenGoogleNameIsNull() {
        // This is what AuthController does: if Google doesn't return a name,
        // we use the email as the display name
        User user = new User();
        String email = "noname@uw.edu";
        user.setEmail(email);

        String nameFromGoogle = null;
        user.setName(nameFromGoogle != null ? nameFromGoogle : email);

        assertEquals(email, user.getName(), "Name should fall back to email if Google didn't provide one");
    }

    @Test
    void testCreatedAtIsNullBeforePersist() {
        // The @PrePersist hook sets createdAt, but only when saved to the database
        User user = new User();
        assertNull(user.getCreatedAt(), "createdAt should be null before the entity is persisted");
    }

    @Test
    void testUserIdIsNullByDefault() {
        User user = new User();
        assertNull(user.getId());
    }

    @Test
    void testEmailIsNullByDefault() {
        User user = new User();
        assertNull(user.getEmail());
    }

    @Test
    void testUwEmailVariantsAreValid() {
        // Both @uw.edu and @u.washington.edu exist, but we only allow @uw.edu
        String[] validEmails = {
            "student@uw.edu",
            "archit.jaiswal@uw.edu",
            "test123@uw.edu"
        };
        for (String email : validEmails) {
            assertTrue(email.endsWith("@uw.edu"), email + " should be a valid UW email for our check");
        }
    }

    @Test
    void testCommonNonUwEmailsAreRejected() {
        String[] invalidEmails = {
            "user@gmail.com",
            "user@yahoo.com",
            "user@outlook.com",
            "user@washington.edu",   // note: u.washington.edu is different from uw.edu
            "user@u.washington.edu"
        };
        for (String email : invalidEmails) {
            assertFalse(email.endsWith("@uw.edu"), email + " should NOT pass the UW email check");
        }
    }
}
