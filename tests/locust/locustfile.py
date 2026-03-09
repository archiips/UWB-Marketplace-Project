"""
Dawgs Marketplace - Load Testing with Locust
=============================================
Tests performance of the backend API under different load conditions.

Usage:
    # Install: pip install locust
    # Run: locust -f locustfile.py --host=http://localhost:8080
    # Then open http://localhost:8089 in your browser to control the test

    # Or run headless:
    # locust -f locustfile.py --host=http://localhost:8080 \
    #        --users 20 --spawn-rate 2 --run-time 60s --headless

Load targets from test plan:
    - Normal usage:   5-10 concurrent users
    - Moderate usage: 15-25 concurrent users
    - Stress testing: 25-40 concurrent users

Performance targets:
    - Main page load: < 2 seconds
    - Search results: < 2 seconds
    - Create/Edit/Delete: < 2 seconds
    - API under 20 users: < 3 seconds response time
"""

import json
import random
from locust import HttpUser, task, between, events


# Sample data for creating listings - simulates real student posts
SAMPLE_TITLES = [
    "MacBook Pro 2021 - barely used",
    "MATH 308 Textbook",
    "Ergonomic Office Chair",
    "Sony WH-1000XM4 Headphones",
    "Standing Desk",
    "DSLR Camera Canon",
    "Electric Scooter",
    "Mini Fridge",
    "Calculus Early Transcendentals 8th Edition",
    "Arduino Starter Kit",
]

SAMPLE_DESCRIPTIONS = [
    "Great condition, barely used. Selling because I upgraded.",
    "Used for one quarter, has some highlighting but readable.",
    "Perfect for long study sessions. Adjustable height.",
    "Active noise cancellation, comes with case.",
    "Adjustable standing desk, 55 inch wide.",
    "Great for photography class. Comes with two lenses.",
    "Commute to campus easily. Max speed 15 mph.",
    "Perfect for a dorm room, runs quietly.",
    "Required for MATH 124/125. Good shape.",
    "Never used, still in box.",
]


class BrowsingUser(HttpUser):
    """
    Simulates a typical user who browses listings and searches for items.
    This is the most common behavior on the marketplace.
    """
    # Wait 1-3 seconds between actions (simulates reading the page)
    wait_time = between(1, 3)

    def on_start(self):
        """Called when each simulated user starts."""
        # In a real test, you'd log in with a real UW Google account here.
        # Since we can't automate Google OAuth in load tests, we use a pre-generated
        # test JWT if available. Otherwise browsing tests still work for public endpoints.
        self.token = None  # Would be set after real auth

    @task(5)
    def browse_all_listings(self):
        """
        Browse the main listings page - the most common action.
        Weight=5 means this happens 5x more often than weight=1 tasks.
        """
        with self.client.get("/api/listings", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
                try:
                    listings = response.json()
                    # Sanity check - should always be a list
                    if not isinstance(listings, list):
                        response.failure("Expected a list of listings")
                except json.JSONDecodeError:
                    response.failure("Response was not valid JSON")
            else:
                response.failure(f"Got status {response.status_code}")

    @task(3)
    def search_for_item(self):
        """Search for listings - second most common action."""
        search_terms = ["laptop", "book", "chair", "camera", "desk", "textbook", "apple", "keyboard"]
        term = random.choice(search_terms)

        with self.client.get(f"/api/listings?search={term}", catch_response=True,
                             name="/api/listings?search=[term]") as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Search failed with status {response.status_code}")

    @task(2)
    def view_specific_listing(self):
        """Click on a listing to see its details."""
        # First, get all listings to pick a random one
        response = self.client.get("/api/listings", name="/api/listings (get for id)")
        if response.status_code == 200:
            listings = response.json()
            if listings:
                random_listing = random.choice(listings)
                listing_id = random_listing.get("id")
                if listing_id:
                    with self.client.get(f"/api/listings/{listing_id}",
                                         catch_response=True,
                                         name="/api/listings/[id]") as detail_response:
                        if detail_response.status_code == 200:
                            detail_response.success()
                        elif detail_response.status_code == 404:
                            # Listing was deleted between requests - that's OK
                            detail_response.success()
                        else:
                            detail_response.failure(f"Got status {detail_response.status_code}")

    @task(1)
    def check_health_endpoint(self):
        """Periodically check the health endpoint (simulates monitoring)."""
        with self.client.get("/api/health", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Health check failed: {response.status_code}")


class SellerUser(HttpUser):
    """
    Simulates a user who creates and manages listings.
    These users need to be authenticated.
    """
    wait_time = between(2, 5)  # Sellers take longer between actions (filling out forms, etc.)

    def on_start(self):
        """
        In a real load test against the live site, you would do the Google OAuth flow here
        and store the JWT. For now we document how that would work.
        """
        # TODO: Replace with real JWT from Google OAuth if testing against live server
        # Example of how to authenticate:
        # response = self.client.post("/api/auth/google", json={"idToken": "<real-google-token>"})
        # self.token = response.json().get("token")
        #
        # For local testing with a known JWT, you can hardcode one here:
        # self.token = "eyJ..."
        self.token = None  # No auth in load test until we have a test account setup

    def auth_headers(self):
        """Helper to generate Authorization header."""
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}

    @task(3)
    def create_listing(self):
        """
        Post a new listing - the main seller action.
        Only works if we have a valid JWT token.
        """
        if not self.token:
            # Skip if we don't have auth (can't test without a real account)
            return

        listing_data = {
            "title": random.choice(SAMPLE_TITLES),
            "description": random.choice(SAMPLE_DESCRIPTIONS),
            "price": round(random.uniform(5.0, 500.0), 2),
            "contactInfo": "seller@uw.edu"
        }

        with self.client.post("/api/listings",
                              json=listing_data,
                              headers=self.auth_headers(),
                              catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 401:
                response.failure("Authentication failed - check JWT token")
            else:
                response.failure(f"Create listing failed: {response.status_code} - {response.text}")

    @task(2)
    def view_my_listings(self):
        """Check the seller's own listings."""
        if not self.token:
            return

        with self.client.get("/api/listings/my-listings",
                             headers=self.auth_headers(),
                             catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"My listings failed: {response.status_code}")

    @task(1)
    def browse_all_listings(self):
        """Sellers also browse to check competition."""
        self.client.get("/api/listings")


class UnauthenticatedUser(HttpUser):
    """
    Simulates a casual visitor who hasn't logged in.
    Can only access public endpoints.
    """
    wait_time = between(1, 4)

    @task(4)
    def browse_listings(self):
        self.client.get("/api/listings")

    @task(2)
    def search_listings(self):
        terms = ["laptop", "book", "desk", "chair", "phone"]
        self.client.get(f"/api/listings?search={random.choice(terms)}",
                        name="/api/listings?search=[term]")

    @task(1)
    def try_access_protected_endpoint(self):
        """
        Unauthenticated users trying to access protected routes should get 401.
        This verifies our security is working under load.
        """
        with self.client.get("/api/listings/my-listings", catch_response=True) as response:
            if response.status_code == 401:
                # This is the EXPECTED behavior for unauthenticated access
                response.success()
            elif response.status_code == 200:
                response.failure("Unauthenticated user should NOT be able to see my-listings!")
            else:
                response.failure(f"Unexpected status: {response.status_code}")


# ===== Custom event hooks for logging =====

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    print("\n" + "="*60)
    print("Dawgs Marketplace Load Test Starting")
    print("="*60)
    print(f"Target host: {environment.host}")
    print("Performance targets:")
    print("  - All endpoints: < 2000ms (95th percentile)")
    print("  - Under 20 users: < 3000ms")
    print("="*60 + "\n")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    print("\n" + "="*60)
    print("Load Test Complete")
    print("="*60)
    stats = environment.runner.stats
    total = stats.total
    print(f"Total requests:  {total.num_requests}")
    print(f"Failed requests: {total.num_failures}")
    print(f"Avg response time: {total.avg_response_time:.0f}ms")
    print(f"95th percentile:   {total.get_response_time_percentile(0.95):.0f}ms")
    print(f"Max response time: {total.max_response_time:.0f}ms")

    # Check if we hit our performance targets
    avg_ms = total.avg_response_time
    p95_ms = total.get_response_time_percentile(0.95)
    failure_rate = (total.num_failures / total.num_requests * 100) if total.num_requests > 0 else 0

    print("\nPerformance Check:")
    print(f"  Avg < 2000ms: {'PASS' if avg_ms < 2000 else 'FAIL'} ({avg_ms:.0f}ms)")
    print(f"  P95 < 3000ms: {'PASS' if p95_ms < 3000 else 'FAIL'} ({p95_ms:.0f}ms)")
    print(f"  Failure rate < 1%: {'PASS' if failure_rate < 1 else 'FAIL'} ({failure_rate:.2f}%)")
    print("="*60 + "\n")
