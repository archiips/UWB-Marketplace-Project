# Dawgs Marketplace — Test Report

**Group:** Dawgs
**Members:** Archit Jaiswal, Shubh Malhotra, Maximo Avalone, Manas Raut, Izy Abudayeh
**Date:** 2026-03-09
**Backend:** Java 21 / Spring Boot 3.3.5
**Frontend:** React 19 / Vite
**Database:** Supabase PostgreSQL (H2 in-memory for unit tests)

---

## Summary

| Test Suite | Tool | Tests Run | Passed | Failed | Pass Rate |
|---|---|---|---|---|---|
| Model Unit Tests | JUnit 5 | 27 | 27 | 0 | 100% |
| Security Unit Tests | JUnit 5 | 14 | 14 | 0 | 100% |
| Controller Integration Tests | JUnit 5 + MockMvc | 36 | 36 | 0 | 100% |
| **JUnit Total** | | **77** | **77** | **0** | **100%** |
| End-to-End Tests | Selenium | 21 | 21 | 0 | 100% |
| Load Test — Normal (10 users) | Locust | — | — | 0% failures | PASS |
| Load Test — Moderate (20 users) | Locust | — | — | 0% failures | PASS |
| Load Test — Stress (35 users) | Locust | — | — | 0% failures | PASS |

**All performance targets met. Zero failures across all test types.**

---

## 1. JUnit Unit & Integration Tests

### Setup
- Spring Boot Test with H2 in-memory database (`application-test.properties`)
- No real PostgreSQL connection required — tests run fully offline
- Run with: `cd backend && mvn test`

### 1.1 Listing Model Tests — `ListingModelTest.java` (15 tests)

| Test | Description | Result |
|---|---|---|
| testCreateListingAndSetFields | Title, description, price, contactInfo set correctly | PASS |
| testNewListingDefaultSoldIsFalse | New listings start as not sold | PASS |
| testMarkListingAsSold | sold flag can be set to true | PASS |
| testUnmarkListingAsSold | sold flag can be set back to false | PASS |
| testSetPriceToZeroForFreeItem | Price of 0.0 allowed (free items) | PASS |
| testSetHighPrice | Price of 9999.99 stored correctly | PASS |
| testSetAndGetOwnerId | ownerId getter/setter works | PASS |
| testSetAndGetImageUrl | imageUrl getter/setter works | PASS |
| testImageUrlIsNullByDefault | imageUrl is null before being set | PASS |
| testOwnerIdIsNullByDefault | ownerId is null before being set | PASS |
| testIdIsNullBeforePersist | UUID id is null until saved to DB | PASS |
| testCreatedAtIsNullBeforePersist | createdAt is null until @PrePersist fires | PASS |
| testUpdateListingTitle | Title can be overwritten | PASS |
| testUpdateListingPrice | Price can be updated | PASS |
| testListingWithAllFieldsSet | Full listing creation with assertAll | PASS |

### 1.2 User Model Tests — `UserModelTest.java` (12 tests)

| Test | Description | Result |
|---|---|---|
| testCreateUserWithValidFields | id, email, name set correctly | PASS |
| testUserEmailEndsWithUwDomain | @uw.edu check passes | PASS |
| testNonUwEmailDoesNotPassDomainCheck | Gmail fails @uw.edu check | PASS |
| testUserIdIsGoogleSubjectId | id stores Google OAuth sub | PASS |
| testUpdateUserName | Name can be overwritten | PASS |
| testUpdateUserEmail | Email can be overwritten | PASS |
| testUserNameFallsBackToEmailWhenGoogleNameIsNull | Name defaults to email if Google returns null | PASS |
| testCreatedAtIsNullBeforePersist | createdAt null before persistence | PASS |
| testUserIdIsNullByDefault | id null before being set | PASS |
| testEmailIsNullByDefault | email null before being set | PASS |
| testUwEmailVariantsAreValid | Valid UW email formats pass check | PASS |
| testCommonNonUwEmailsAreRejected | Gmail, Yahoo, outlook, etc. all fail | PASS |

### 1.3 JWT Utility Tests — `JwtUtilTest.java` (14 tests)

| Test | Description | Result |
|---|---|---|
| testGenerateTokenReturnsNonEmptyString | Token is non-null and non-blank | PASS |
| testGeneratedTokenHasThreeParts | Token has header.payload.signature format | PASS |
| testExtractUserIdFromToken | userId round-trips through token | PASS |
| testExtractEmailFromToken | email round-trips through token | PASS |
| testValidTokenPassesValidation | Fresh token passes isTokenValid() | PASS |
| testGarbageStringIsNotValidToken | Random string rejected | PASS |
| testEmptyStringIsNotValidToken | Empty string rejected | PASS |
| testTamperedSignatureIsInvalid | Modified signature rejected | PASS |
| testTokenSignedWithDifferentSecretIsInvalid | Token from wrong secret rejected | PASS |
| testExtractClaimsHasCorrectEmailClaim | Email claim extracted correctly | PASS |
| testExtractClaimsHasCorrectSubject | Subject claim extracted correctly | PASS |
| testExpiredTokenIsRejected | Token expired after 1ms is rejected | PASS |
| testDifferentUsersGetDifferentTokens | Two users get distinct tokens | PASS |
| testExtractUserIdMatchesForMultipleUsers | userId correct across multiple tokens | PASS |

### 1.4 Listing Controller Integration Tests — `ListingControllerTest.java` (28 tests)

Tests use real MockMvc requests against the full Spring Security filter chain with a real H2 database and real JWT tokens.

| Endpoint | Test | Result |
|---|---|---|
| GET /api/listings | Returns empty array when none exist | PASS |
| GET /api/listings | Returns all listings | PASS |
| GET /api/listings | Public — no auth required | PASS |
| GET /api/listings?search= | Search by title | PASS |
| GET /api/listings?search= | Search by description | PASS |
| GET /api/listings?search= | Case-insensitive search | PASS |
| GET /api/listings?search= | No match returns empty array | PASS |
| GET /api/listings?search= | Blank search returns all | PASS |
| GET /api/listings/{id} | Returns correct listing | PASS |
| GET /api/listings/{id} | 404 for nonexistent ID | PASS |
| GET /api/listings/{id} | Public — no auth required | PASS |
| GET /api/listings/my-listings | 401 without auth | PASS |
| GET /api/listings/my-listings | Returns only caller's listings | PASS |
| GET /api/listings/my-listings | Empty when user has no listings | PASS |
| POST /api/listings | 401 without auth | PASS |
| POST /api/listings | Creates listing successfully | PASS |
| POST /api/listings | ownerId set from JWT, not request body | PASS |
| POST /api/listings | New listing defaults sold=false | PASS |
| PUT /api/listings/{id} | 401 without auth | PASS |
| PUT /api/listings/{id} | Updates title, description, price | PASS |
| PUT /api/listings/{id} | 403 for non-owner | PASS |
| PUT /api/listings/{id} | Can mark listing as sold | PASS |
| PUT /api/listings/{id} | 404 for nonexistent ID | PASS |
| DELETE /api/listings/{id} | 401 without auth | PASS |
| DELETE /api/listings/{id} | Deletes listing successfully | PASS |
| DELETE /api/listings/{id} | 403 for non-owner | PASS |
| DELETE /api/listings/{id} | 404 for nonexistent ID | PASS |
| GET /api/health | Public — returns 200 | PASS |

### 1.5 Auth Controller Integration Tests — `AuthControllerTest.java` (8 tests)

Note: Full Google OAuth cannot be tested in unit tests as it requires a real token signed by Google's servers. These tests cover all controllable failure cases.

| Test | Description | Result |
|---|---|---|
| testGoogleAuthEndpointIsReachable | Endpoint exists and responds | PASS |
| testMissingIdTokenReturnsBadRequest | Empty body → 400 with error message | PASS |
| testBlankIdTokenReturnsBadRequest | idToken: "" → 400 | PASS |
| testWhitespaceOnlyIdTokenReturnsBadRequest | idToken: "   " → 400 | PASS |
| testFakeIdTokenReturnsUnauthorized | Fake JWT string → 401 | PASS |
| testRandomStringIdTokenReturnsUnauthorized | Random string → 401 | PASS |
| testAuthEndpointIsPublicNoJwtNeeded | Returns 400 not 401 (Spring Security let it through) | PASS |
| testAuthEndpointAcceptsJsonContentType | Accepts JSON, not 415 Unsupported Media Type | PASS |

---

## 2. Selenium End-to-End Tests

### Setup
- Chrome WebDriver via webdriver-manager
- Frontend running at `http://localhost:5173`
- Backend running at `http://localhost:8080`
- Authenticated tests used a real JWT from a logged-in `@uw.edu` session injected into localStorage
- Run with: `TEST_JWT="<token>" python3 tests/selenium/test_marketplace.py`

### 2.1 Login Page Tests (7 tests)

| Test | Description | Result |
|---|---|---|
| test_login_page_loads | Login page loads without errors | PASS |
| test_login_page_has_google_sign_in_button | Google sign-in button visible | PASS |
| test_unauthenticated_redirect_to_login | Protected routes redirect to login | PASS |
| test_unauthenticated_cannot_reach_create_listing | /create inaccessible without login | PASS |
| test_unauthenticated_cannot_reach_my_listings | /my-listings inaccessible without login | PASS |
| test_page_title_is_set | Page has a non-empty title | PASS |
| test_login_page_has_no_console_errors | No SEVERE console errors on load | PASS |

### 2.2 Public Listings API Tests (3 tests)

| Test | Description | Result |
|---|---|---|
| test_api_health_endpoint | GET /api/health returns 200 | PASS |
| test_api_listings_returns_json | GET /api/listings returns JSON array | PASS |
| test_api_invalid_listing_id_returns_404 | Fake UUID returns 404 | PASS |

### 2.3 Authenticated Flow Tests (9 tests)

| Test | Description | Result |
|---|---|---|
| test_home_page_loads_after_login | Home page loads after JWT injection | PASS |
| test_listings_are_displayed_on_home_page | Listings visible on home page | PASS |
| test_search_functionality | Search input accepts text and doesn't break page | PASS |
| test_navigate_to_create_listing | /create accessible when authenticated | PASS |
| test_create_listing_form_has_required_fields | Form has Title, Description, Price fields | PASS |
| test_navigate_to_my_listings | /my-listings accessible when authenticated | PASS |
| test_my_listings_page_shows_correct_content | My Listings page renders without login redirect | PASS |
| test_create_and_verify_listing | Fills and submits create listing form end-to-end | PASS |
| test_logout_clears_session | Clearing JWT redirects back to login | PASS |

### 2.4 Performance Timing Tests (2 tests)

| Test | Threshold | Result | Pass? |
|---|---|---|---|
| test_login_page_loads_within_2_seconds | < 2000ms | 15–35ms | PASS |
| test_home_page_loads_within_2_seconds | < 2000ms | 24–27ms | PASS |

---

## 3. Locust Load Tests

### Setup
- Locust 2.43.3
- Backend running at `http://localhost:8080`
- Three user classes: `BrowsingUser`, `SellerUser`, `UnauthenticatedUser`
- Run with: `locust -f tests/locust/locustfile.py --host=http://localhost:8080`

### Results Summary

| Test | Users | Ramp-up | RPS | Avg (ms) | 95th % (ms) | Max (ms) | Failures | Pass? |
|---|---|---|---|---|---|---|---|---|
| Normal | 10 | 2/s | 3.3 | 254 | 400 | 821 | 0% | ✅ |
| Moderate | 20 | 3/s | 6.5 | 214 | 350 | 980 | 0% | ✅ |
| Stress | 35 | 5/s | 10.8 | 201 | 350 | 800 | 0% | ✅ |

### Performance Targets vs Actuals

| Target | Threshold | Normal | Moderate | Stress |
|---|---|---|---|---|
| Failure rate | < 1% | 0% ✅ | 0% ✅ | 0% ✅ |
| Avg response time | < 2000ms | 254ms ✅ | 214ms ✅ | 201ms ✅ |
| 95th percentile | < 3000ms | 400ms ✅ | 350ms ✅ | 350ms ✅ |
| Max response time | < 5000ms | 821ms ✅ | 980ms ✅ | 800ms ✅ |

### Key Observations

- **Zero failures** across all three load levels including stress test (35 concurrent users)
- **RPS scaled linearly** with user count: 3.3 → 6.5 → 10.8, indicating no backend bottleneck
- **Response times improved** across runs (254ms → 201ms avg) due to JVM JIT warmup and DB connection pool pre-establishment — a known Spring Boot characteristic. Cold-start production numbers will more closely resemble the normal usage run
- **Search endpoint got faster under load** (200ms → 130ms median) likely due to PostgreSQL query plan caching
- `/api/listings/my-listings` consistently fast (median ~5ms) because unauthenticated requests are rejected at the security filter before reaching the database

---

## 4. Exit Criteria Status

| Criterion | Status |
|---|---|
| All P0 functional requirements implemented and tested | ✅ |
| At least 95% of test cases pass | ✅ 100% |
| All automated backend (JUnit) tests pass | ✅ 77/77 |
| All end-to-end (Selenium) tests pass | ✅ 21/21 |
| No critical defects remain open | ✅ |
| Core user flows work correctly | ✅ |
| Performance targets met | ✅ All targets passed at all load levels |
| Access restricted to @uw.edu accounts | ✅ Verified in unit and integration tests |
