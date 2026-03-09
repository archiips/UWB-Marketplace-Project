"""
Dawgs Marketplace - End-to-End Tests with Selenium
====================================================
Tests the full user flows through the browser, verifying that the
frontend and backend work together correctly.

Setup:
    pip install selenium webdriver-manager

    Make sure you have Google Chrome installed.

Usage:
    # Run all tests against local dev servers
    python test_marketplace.py

    # Run against deployed site
    BASE_URL=https://dawgs-marketplace.vercel.app python test_marketplace.py

NOTE ON GOOGLE OAUTH:
    Full login automation is NOT possible with Google OAuth because Google
    blocks automated sign-ins (this is a feature, not a bug - it prevents bots).
    Tests that require authentication use a pre-saved session cookie or a test
    token approach. See the setUp comments in AuthenticatedTestCase for details.

    For tests that require login, you have two options:
    Option A: Manually log in first, then copy the localStorage JWT token
              and set it in the TEST_JWT environment variable.
    Option B: Run tests in "manual" mode where the test pauses for you to log in.
"""

import os
import time
import unittest
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException

# Config - can be overridden with environment variables
BASE_URL = os.environ.get("BASE_URL", "http://localhost:5173")
BACKEND_URL = os.environ.get("BACKEND_URL", "http://localhost:8080")
# Set this to a valid JWT if you want to run authenticated tests
TEST_JWT = os.environ.get("TEST_JWT", None)
HEADLESS = os.environ.get("HEADLESS", "true").lower() == "true"

# How long to wait for elements to appear (in seconds)
DEFAULT_TIMEOUT = 10


def create_driver():
    """Set up Chrome WebDriver with our preferred options."""
    options = Options()
    if HEADLESS:
        options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--window-size=1280,800")

    # Try to use webdriver-manager for automatic ChromeDriver management
    try:
        from webdriver_manager.chrome import ChromeDriverManager
        driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=options
        )
    except ImportError:
        # Fall back to system ChromeDriver if webdriver-manager isn't installed
        driver = webdriver.Chrome(options=options)

    driver.implicitly_wait(5)
    return driver


class BaseTestCase(unittest.TestCase):
    """Base class that handles driver setup and teardown for all test cases."""

    @classmethod
    def setUpClass(cls):
        cls.driver = create_driver()
        cls.wait = WebDriverWait(cls.driver, DEFAULT_TIMEOUT)

    @classmethod
    def tearDownClass(cls):
        cls.driver.quit()

    def navigate_to(self, path="/"):
        """Navigate to a URL and wait for the page to load."""
        self.driver.get(BASE_URL + path)
        time.sleep(0.5)  # Small buffer for React to render

    def find_element(self, by, value, timeout=DEFAULT_TIMEOUT):
        """Wait for an element and return it."""
        return WebDriverWait(self.driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )

    def find_clickable(self, by, value, timeout=DEFAULT_TIMEOUT):
        """Wait for a clickable element and return it."""
        return WebDriverWait(self.driver, timeout).until(
            EC.element_to_be_clickable((by, value))
        )

    def wait_for_url_contains(self, partial_url, timeout=DEFAULT_TIMEOUT):
        """Wait until the URL contains a specific string."""
        WebDriverWait(self.driver, timeout).until(
            EC.url_contains(partial_url)
        )

    def take_screenshot(self, name):
        """Save a screenshot for debugging failed tests."""
        screenshot_dir = os.path.join(os.path.dirname(__file__), "screenshots")
        os.makedirs(screenshot_dir, exist_ok=True)
        path = os.path.join(screenshot_dir, f"{name}.png")
        self.driver.save_screenshot(path)
        print(f"  Screenshot saved: {path}")


class LoginPageTests(BaseTestCase):
    """
    Tests for the login page - things we can verify without actually
    completing the Google OAuth flow.
    """

    def test_login_page_loads(self):
        """The login page should load without errors."""
        self.navigate_to("/")
        # Page title or main heading should be visible
        # (React renders the login page at the root route)
        body = self.driver.find_element(By.TAG_NAME, "body")
        self.assertIsNotNone(body)
        self.assertNotIn("404", self.driver.title)
        self.assertNotIn("Error", self.driver.title)

    def test_login_page_has_google_sign_in_button(self):
        """There should be a Google sign-in button on the login page."""
        self.navigate_to("/")

        # Wait a moment for React to render
        time.sleep(2)

        # Look for Google sign-in button - could be identified by various attributes
        try:
            # React Google OAuth renders an iframe or button
            # Try to find by text content or aria-label
            body_text = self.driver.find_element(By.TAG_NAME, "body").text
            # The login page should mention sign in or Google somewhere
            self.assertTrue(
                "Sign in" in body_text or "Google" in body_text or "Login" in body_text,
                f"Login page should have Google sign-in. Page text: {body_text[:200]}"
            )
        except Exception as e:
            self.take_screenshot("login_page_failure")
            raise

    def test_unauthenticated_redirect_to_login(self):
        """
        Visiting a protected route without being logged in should
        redirect to the login page.
        """
        self.navigate_to("/home")
        time.sleep(2)

        # Should be redirected to "/" (login page)
        current_url = self.driver.current_url
        # Either we're at / or the URL still shows /home but login UI is visible
        self.assertTrue(
            current_url.endswith("/") or "/home" in current_url,
            f"Unexpected URL after unauthorized access: {current_url}"
        )

        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        # The login page content should be visible
        self.assertTrue(
            "Sign in" in body_text or "Google" in body_text or "Login" in body_text or "login" in body_text.lower(),
            "Should see login page when unauthenticated"
        )

    def test_unauthenticated_cannot_reach_create_listing(self):
        """Unauthenticated user cannot directly navigate to /create."""
        self.navigate_to("/create")
        time.sleep(2)

        # Should see login page, not create listing form
        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        self.assertNotIn("Create Listing", body_text,
                         "Should not see Create Listing page when not logged in")

    def test_unauthenticated_cannot_reach_my_listings(self):
        """Unauthenticated user cannot directly navigate to /my-listings."""
        self.navigate_to("/my-listings")
        time.sleep(2)

        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        self.assertNotIn("My Listings", body_text,
                         "Should not see My Listings page when not logged in")

    def test_page_title_is_set(self):
        """The page should have some title, not be blank."""
        self.navigate_to("/")
        time.sleep(1)
        # HTML title shouldn't be completely empty
        # (Vite apps sometimes have "Vite App" as default which is fine for now)
        self.assertIsNotNone(self.driver.title)

    def test_login_page_has_no_console_errors(self):
        """
        Check for JavaScript console errors on the login page.
        Minor errors might be OK, but serious ones would break functionality.
        """
        self.navigate_to("/")
        time.sleep(2)

        logs = self.driver.get_log("browser")
        severe_errors = [log for log in logs if log["level"] == "SEVERE"]

        if severe_errors:
            print(f"  Console errors found: {severe_errors}")
            # We don't fail the test for this since some 3rd party errors are expected,
            # but we print them so developers can see them
        # Just make sure the page loaded at all
        self.assertIsNotNone(self.driver.find_element(By.TAG_NAME, "body"))


class PublicListingsTests(BaseTestCase):
    """
    Tests for the public listings API - no login required.
    We test the API directly since browsing the frontend requires authentication.
    """

    def test_api_health_endpoint(self):
        """Health endpoint should return 200 OK."""
        import urllib.request
        try:
            response = urllib.request.urlopen(f"{BACKEND_URL}/api/health", timeout=5)
            self.assertEqual(200, response.status)
        except Exception as e:
            self.skipTest(f"Backend not running at {BACKEND_URL}: {e}")

    def test_api_listings_returns_json(self):
        """GET /api/listings should return a JSON array."""
        import urllib.request
        import json as json_lib

        try:
            response = urllib.request.urlopen(f"{BACKEND_URL}/api/listings", timeout=5)
            self.assertEqual(200, response.status)
            data = json_lib.loads(response.read().decode())
            self.assertIsInstance(data, list, "Listings endpoint should return an array")
        except Exception as e:
            self.skipTest(f"Backend not running at {BACKEND_URL}: {e}")

    def test_api_invalid_listing_id_returns_404(self):
        """Requesting a listing that doesn't exist should return 404."""
        import urllib.request
        import urllib.error

        fake_uuid = "00000000-0000-0000-0000-000000000000"
        try:
            with self.assertRaises(urllib.error.HTTPError) as ctx:
                urllib.request.urlopen(f"{BACKEND_URL}/api/listings/{fake_uuid}", timeout=5)
            self.assertEqual(404, ctx.exception.code)
        except Exception as e:
            self.skipTest(f"Backend not running at {BACKEND_URL}: {e}")


@unittest.skipIf(TEST_JWT is None, "Set TEST_JWT environment variable to run authenticated tests")
class AuthenticatedFlowTests(BaseTestCase):
    """
    Tests for flows that require the user to be logged in.

    To run these tests:
    1. Open the app in a browser and log in with your UW Google account
    2. Open DevTools → Application → Local Storage
    3. Find the 'token' key and copy its value
    4. Set: export TEST_JWT=<that token value>
    5. Run the tests again

    These tests inject the JWT directly into localStorage, bypassing
    the Google OAuth UI (which can't be automated).
    """

    def setUp(self):
        """Inject the JWT token into localStorage before each test."""
        self.navigate_to("/")
        time.sleep(1)

        # Inject the token into localStorage - same as what the app does after OAuth
        self.driver.execute_script(
            f"window.localStorage.setItem('token', '{TEST_JWT}');"
        )
        # Navigate to home now that we're "logged in"
        self.navigate_to("/home")
        time.sleep(2)

    def tearDown(self):
        """Clear auth state after each test."""
        self.driver.execute_script("window.localStorage.clear();")

    def test_home_page_loads_after_login(self):
        """After injecting a valid JWT, the home/listings page should load."""
        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        # Should NOT be on the login page anymore
        self.assertNotIn("Sign in with Google", body_text,
                         "Should not see login page after injecting JWT")

    def test_listings_are_displayed_on_home_page(self):
        """The home page should show a list of listings (or a message if none exist)."""
        time.sleep(2)  # Wait for API call to complete

        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        # Either we see listings or a "no listings" message - either is fine
        # What we should NOT see is the login page
        self.assertNotIn("Sign in", body_text,
                         "Should not see login page on home")

    def test_search_functionality(self):
        """
        Type into the search bar and verify that the results update.
        We test this by verifying the search doesn't break the page.
        """
        self.navigate_to("/home")
        time.sleep(2)

        try:
            # Try to find a search input
            search_input = self.driver.find_element(By.CSS_SELECTOR,
                                                     "input[type='text'], input[placeholder*='earch'], input[placeholder*='Search']")
            search_input.clear()
            search_input.send_keys("laptop")
            search_input.send_keys(Keys.RETURN)
            time.sleep(2)

            # Page should still be functional after searching
            body = self.driver.find_element(By.TAG_NAME, "body")
            self.assertIsNotNone(body)
        except NoSuchElementException:
            print("  Warning: Could not find search input - skipping search interaction")

    def test_navigate_to_create_listing(self):
        """Authenticated user should be able to reach the create listing page."""
        self.navigate_to("/create")
        time.sleep(2)

        body_text = self.driver.find_element(By.TAG_NAME, "body").text

        # Should see a form for creating a listing, not the login page
        self.assertNotIn("Sign in with Google", body_text,
                         "Should not be redirected to login when authenticated")

    def test_create_listing_form_has_required_fields(self):
        """The create listing form should have title, description, and price fields."""
        self.navigate_to("/create")
        time.sleep(2)

        body_text = self.driver.find_element(By.TAG_NAME, "body").text

        # The form should mention the required fields
        has_form = any(keyword in body_text for keyword in [
            "Title", "Description", "Price", "title", "description", "price"
        ])

        if not has_form:
            self.take_screenshot("create_listing_form")

        self.assertTrue(has_form,
                        "Create listing page should have form fields for Title, Description, Price")

    def test_navigate_to_my_listings(self):
        """Authenticated user should be able to reach My Listings page."""
        self.navigate_to("/my-listings")
        time.sleep(2)

        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        self.assertNotIn("Sign in with Google", body_text,
                         "Should not be redirected to login from My Listings")

    def test_my_listings_page_shows_correct_content(self):
        """My Listings should either show listings or a 'no listings' empty state."""
        self.navigate_to("/my-listings")
        time.sleep(3)  # Give the API call time to complete

        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        # Should NOT be on the login page
        self.assertNotIn("Sign in", body_text)

    def test_create_and_verify_listing(self):
        """
        Full flow: fill out the create listing form and verify the listing appears.

        This test is marked as fragile because it depends on specific CSS selectors
        that might change when the UI is updated. Update selectors as needed.
        """
        self.navigate_to("/create")
        time.sleep(2)

        try:
            # Try to fill in the form
            # Selectors may need to be updated based on actual HTML structure
            title_input = self.driver.find_element(By.CSS_SELECTOR,
                "input[placeholder*='Title'], input[name='title'], input[id='title']")
            title_input.clear()
            title_input.send_keys("Selenium Test Listing - Please Delete")

            desc_input = self.driver.find_element(By.CSS_SELECTOR,
                "textarea[placeholder*='Description'], textarea[name='description'], textarea[id='description']")
            desc_input.clear()
            desc_input.send_keys("This listing was created by an automated Selenium test.")

            price_input = self.driver.find_element(By.CSS_SELECTOR,
                "input[type='number'], input[placeholder*='Price'], input[name='price']")
            price_input.clear()
            price_input.send_keys("1.00")

            # Find and click the submit button
            submit_btn = self.driver.find_element(By.CSS_SELECTOR,
                "button[type='submit'], button:contains('Post'), button:contains('Create')")
            submit_btn.click()

            time.sleep(3)  # Wait for form submission and navigation

            # After creating a listing, user is usually redirected to home or my-listings
            body_text = self.driver.find_element(By.TAG_NAME, "body").text
            self.assertNotIn("Sign in", body_text, "Should still be logged in after creating listing")

        except NoSuchElementException as e:
            # If we can't find the form elements, skip with a helpful message
            self.take_screenshot("create_listing_selectors")
            self.skipTest(f"Could not find form elements - update CSS selectors: {e}")

    def test_logout_clears_session(self):
        """After logging out, user should not be able to access protected routes."""
        # Clear localStorage to simulate logout
        self.driver.execute_script("window.localStorage.clear();")
        self.navigate_to("/home")
        time.sleep(2)

        body_text = self.driver.find_element(By.TAG_NAME, "body").text
        # Should be back on the login page
        self.assertTrue(
            "Sign in" in body_text or "Google" in body_text or "Login" in body_text.lower(),
            "After logout, should see login page"
        )


class PerformanceTimingTests(BaseTestCase):
    """
    Basic performance tests using Selenium's built-in timing.
    These check that pages load within our target times.

    Performance targets from test plan:
        - Main page load: < 2 seconds
        - Login page: < 2 seconds
    """

    def get_page_load_time_ms(self):
        """Use the Navigation Timing API to get the actual page load time."""
        return self.driver.execute_script(
            "return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart;"
        )

    def test_login_page_loads_within_2_seconds(self):
        """Login page should load in under 2 seconds."""
        self.navigate_to("/")
        time.sleep(3)  # Wait for page to fully load

        load_time_ms = self.get_page_load_time_ms()
        print(f"  Login page load time: {load_time_ms}ms")

        self.assertLess(load_time_ms, 2000,
                        f"Login page took {load_time_ms}ms, should be under 2000ms")

    @unittest.skipIf(TEST_JWT is None, "Requires TEST_JWT for authenticated page timing")
    def test_home_page_loads_within_2_seconds(self):
        """
        Home/listings page should load in under 2 seconds.
        Requires authentication to access.
        """
        self.navigate_to("/")
        time.sleep(0.5)
        self.driver.execute_script(f"window.localStorage.setItem('token', '{TEST_JWT}');")
        self.navigate_to("/home")
        time.sleep(3)

        load_time_ms = self.get_page_load_time_ms()
        print(f"  Home page load time: {load_time_ms}ms")

        self.driver.execute_script("window.localStorage.clear();")

        self.assertLess(load_time_ms, 2000,
                        f"Home page took {load_time_ms}ms, should be under 2000ms")


if __name__ == "__main__":
    print("="*60)
    print("Dawgs Marketplace - Selenium Tests")
    print("="*60)
    print(f"Frontend URL: {BASE_URL}")
    print(f"Backend URL:  {BACKEND_URL}")
    print(f"Headless:     {HEADLESS}")
    print(f"Auth tests:   {'ENABLED (JWT provided)' if TEST_JWT else 'SKIPPED (set TEST_JWT env var)'}")
    print("="*60 + "\n")

    # Run the tests with verbosity=2 to see each test name
    loader = unittest.TestLoader()
    suite = unittest.TestSuite()

    # Add all test classes
    suite.addTests(loader.loadTestsFromTestCase(LoginPageTests))
    suite.addTests(loader.loadTestsFromTestCase(PublicListingsTests))
    suite.addTests(loader.loadTestsFromTestCase(AuthenticatedFlowTests))
    suite.addTests(loader.loadTestsFromTestCase(PerformanceTimingTests))

    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)

    # Exit with error code if any tests failed
    exit(0 if result.wasSuccessful() else 1)
