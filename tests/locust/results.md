# Dawgs Marketplace - Locust Load Test Results

**Host:** http://localhost:8080
**Date:** 2026-03-08

---

## Test 1: Normal Usage (10 users, ramp-up 2/s)

**Config:** 10 peak users, spawn rate 2 users/second
**Duration:** ~3 minutes
**Failures:** 0% (0 failures)
**Total Requests:** 658
**Aggregated RPS:** 3.3

### Per-Endpoint Results

| Endpoint | Requests | Fails | Median (ms) | 95th % (ms) | 99th % (ms) | Avg (ms) | Min (ms) | Max (ms) |
|---|---|---|---|---|---|---|---|---|
| GET /api/health | 33 | 0 | 6 | 13 | 14 | 6.35 | 3 | 14 |
| GET /api/listings | 296 | 0 | 310 | 410 | 650 | 309.18 | 165 | 821 |
| GET /api/listings (get for id) | 72 | 0 | 330 | 520 | 690 | 324.27 | 180 | 686 |
| GET /api/listings/[id] | 72 | 0 | 270 | 450 | 570 | 290.25 | 171 | 570 |
| GET /api/listings/my-listings | 36 | 0 | 5 | 14 | 17 | 6.34 | 2 | 17 |
| GET /api/listings?search=[term] | 149 | 0 | 200 | 310 | 470 | 208.58 | 86 | 736 |
| **Aggregated** | **658** | **0** | **270** | **400** | **620** | **254.23** | **2** | **821** |

### Performance Target Check (Normal Usage)

| Target | Threshold | Result | Pass? |
|---|---|---|---|
| Failure rate | < 1% | 0% | ✅ PASS |
| Avg response time | < 2000ms | 254ms | ✅ PASS |
| 95th percentile | < 3000ms | 400ms | ✅ PASS |
| Max response time | < 5000ms | 821ms | ✅ PASS |
| /api/listings median | < 2000ms | 310ms | ✅ PASS |
| Search median | < 2000ms | 200ms | ✅ PASS |

### Notes
- `/api/listings/my-listings` is extremely fast (median 5ms) because unauthenticated requests return 401 immediately without hitting the DB
- `/api/listings` (browse all) is the slowest endpoint at median 310ms - expected since it fetches all rows
- No failures at all under normal load - system is stable

---

## Test 2: Moderate Usage (20 users, ramp-up 3/s)

**Config:** 20 peak users, spawn rate 3 users/second
**Duration:** ~3 minutes
**Failures:** 0% (0 failures)
**Total Requests:** 922
**Aggregated RPS:** 6.5

### Per-Endpoint Results

| Endpoint | Requests | Fails | Median (ms) | 95th % (ms) | 99th % (ms) | Avg (ms) | Min (ms) | Max (ms) |
|---|---|---|---|---|---|---|---|---|
| GET /api/health | 31 | 0 | 4 | 25 | 490 | 20.04 | 1 | 487 |
| GET /api/listings | 455 | 0 | 260 | 370 | 440 | 262.12 | 162 | 980 |
| GET /api/listings (get for id) | 78 | 0 | 270 | 340 | 360 | 261.85 | 165 | 363 |
| GET /api/listings/[id] | 76 | 0 | 200 | 350 | 360 | 229.93 | 169 | 363 |
| GET /api/listings/my-listings | 51 | 0 | 5 | 14 | 20 | 5.62 | 2 | 20 |
| GET /api/listings?search=[term] | 231 | 0 | 170 | 270 | 290 | 170.46 | 84 | 789 |
| **Aggregated** | **922** | **0** | **210** | **350** | **400** | **214.15** | **1** | **980** |

### Performance Target Check (Moderate Usage)

| Target | Threshold | Result | Pass? |
|---|---|---|---|
| Failure rate | < 1% | 0% | ✅ PASS |
| Avg response time | < 2000ms | 214ms | ✅ PASS |
| 95th percentile | < 3000ms | 350ms | ✅ PASS |
| Max response time | < 5000ms | 980ms | ✅ PASS |
| /api/listings median | < 2000ms | 260ms | ✅ PASS |
| Search median | < 2000ms | 170ms | ✅ PASS |

### Notes
- Response times actually *improved* slightly vs normal (avg 254ms → 214ms) - likely due to JVM/DB warmup from the previous run
- RPS doubled from 3.3 → 6.5 as expected with 2x users
- Max response time crept up to 980ms (vs 821ms normal) but still well within targets
- The occasional 95th % spikes to ~800ms seen in the chart are brief and not reflected in the aggregate
- **Interesting:** Average response time actually *improved* from normal → moderate (254ms → 214ms). This is likely due to JVM JIT compilation warming up and the database connection pool being pre-established from the previous run. This is a known Java/Spring Boot behavior — the first run pays the warmup cost, subsequent runs benefit from it. In a cold-start production scenario (e.g. fresh Railway deploy), normal usage numbers are more representative.

---

## Test 3: Stress Test (35 users, ramp-up 5/s)

**Config:** 35 peak users, spawn rate 5 users/second
**Duration:** ~5 minutes
**Failures:** 0% (0 failures)
**Total Requests:** 2923
**Aggregated RPS:** 10.8

### Per-Endpoint Results

| Endpoint | Requests | Fails | Median (ms) | 95th % (ms) | 99th % (ms) | Avg (ms) | Min (ms) | Max (ms) |
|---|---|---|---|---|---|---|---|---|
| GET /api/health | 118 | 0 | 4 | 8 | 11 | 4.49 | 2 | 14 |
| GET /api/listings | 1425 | 0 | 240 | 360 | 390 | 250.47 | 162 | 800 |
| GET /api/listings (get for id) | 256 | 0 | 240 | 350 | 380 | 246.06 | 160 | 406 |
| GET /api/listings/[id] | 256 | 0 | 210 | 340 | 370 | 229.99 | 160 | 383 |
| GET /api/listings/my-listings | 154 | 0 | 4 | 9 | 13 | 4.77 | 1 | 13 |
| GET /api/listings?search=[term] | 714 | 0 | 130 | 260 | 300 | 149.56 | 81 | 499 |
| **Aggregated** | **2923** | **0** | **200** | **350** | **380** | **200.77** | **1** | **800** |

### Performance Target Check (Stress Test)

| Target | Threshold | Result | Pass? |
|---|---|---|---|
| Failure rate | < 1% | 0% | ✅ PASS |
| Avg response time | < 2000ms | 201ms | ✅ PASS |
| 95th percentile | < 3000ms | 350ms | ✅ PASS |
| Max response time | < 5000ms | 800ms | ✅ PASS |
| /api/listings median | < 2000ms | 240ms | ✅ PASS |
| Search median | < 2000ms | 130ms | ✅ PASS |

### Notes
- **Zero failures** even at 35 concurrent users — the backend handled stress testing without crashing or erroring
- Response times continued to improve across all three runs (avg 254ms → 214ms → 201ms), confirming the JVM warmup effect observed in moderate testing
- RPS scaled nearly linearly: 3.3 (10 users) → 6.5 (20 users) → 10.8 (35 users), which means the backend is not bottlenecking
- Max response time actually dropped vs normal (821ms → 800ms), further confirming warmup effect
- Search endpoint got faster under more load (median 200ms → 170ms → 130ms) — likely due to DB query plan caching
- **Interesting:** The system shows no signs of degradation under 3.5x the normal load. For a campus-sized app like UWB marketplace, this headroom is more than sufficient

---

## Summary

| Test | Users | RPS | Avg (ms) | 95th % (ms) | Failures | Pass? |
|---|---|---|---|---|---|---|
| Normal | 10 | 3.3 | 254 | 400 | 0% | ✅ |
| Moderate | 20 | 6.5 | 214 | 350 | 0% | ✅ |
| Stress | 35 | 10.8 | 201 | 350 | 0% | ✅ |
