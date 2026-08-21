# FuelFinder

A full-stack gas-price app for the Dallas–Fort Worth metro. Search stations by ZIP code and/or city, compare live crowd-reported prices, see them on a map, and optionally rank stations by *true* cost — pump price plus the estimated fuel it takes to actually drive there.

**Live app:** https://fuel-finder-beta.vercel.app
**Live API:** https://fuel-finder-production-5a19.up.railway.app/api/health

Station metadata (name, address, coordinates) comes from OpenStreetMap. Prices come from real users reporting what they see at the pump — there's no paid or scraped price feed (see [Design Decisions](#design-decisions) below for why).

---

## Features

- **Search by ZIP, city, or both** — e.g. `76010`, `Dallas`, or both together, plus a required fuel type (Regular/Midgrade/Premium/Diesel)
- **Interactive map** — every result plotted with Leaflet/OpenStreetMap tiles; clicking a result card pans the map to it and opens its popup
- **Best Value ranking** (opt-in) — grant location access and set how many gallons you plan to buy, and a second results column ranks stations by *price-per-gallon including the estimated drive there* — not just raw pump price
- **User-submitted prices** with guardrails: a station's first-ever reported price is capped at $10; every update after that must stay within $1.00 of the current price, both enforced client-side and independently on the backend
- **Stale-price warnings** — a price's "updated X ago" label turns amber past 12 hours old and red past 24
- **Per-IP rate limiting** on price submissions (10/minute) to protect the public write endpoint from abuse
- Color-coded map markers: green = best raw price, purple = best value (once location is granted), gray = everything else

---

## Tech Stack

**Backend** — Java 21, Spring Boot 4.1, Spring Data JPA/Hibernate, MySQL, Maven, JUnit 5 + Mockito (18 tests)
**Frontend** — React 19, Vite, plain CSS (a small custom design-token system, no framework), Leaflet/react-leaflet
**Infra** — Docker (multi-stage build), Railway (backend + MySQL), Vercel (frontend)
**Data** — OpenStreetMap / Overpass API for station metadata

---

## Architecture

```
Browser (React)
      ↓  fetch()
Spring Boot REST API
      ↓
Controller → Service → Repository
      ↓
Hibernate / Spring Data JPA
      ↓
MySQL
```

The frontend never talks to MySQL or OpenStreetMap directly — everything goes through the Spring Boot API. Station data is seeded independently via a one-time, opt-in Overpass API import (see below), completely decoupled from normal request handling.

---

## Data Model

**Station** — id, name, address, city, state, zipCode, latitude, longitude, osmType, osmId (the last two identify the source OSM element, used to make re-imports idempotent)

**FuelPrice** — id, fuelType, price, lastUpdated, station (FK). One row per (station, fuel type) — reporting a new price updates the existing row rather than inserting a new one, so there's always exactly one *current* price per station/fuel type, not a history.

**FuelType** — `REGULAR`, `MIDGRADE`, `PREMIUM`, `DIESEL`

---

## API Reference

### Stations
| Method | Endpoint | Notes |
|---|---|---|
| `GET` | `/api/stations` | Optional `zipCode` filter |
| `POST` | `/api/stations` | Create a station |
| `GET` | `/api/stations/search?zipCode=&city=&fuelType=` | `zipCode` and `city` are both optional, but at least one is required; fuel type is required. Results sorted cheapest-first. |
| `GET` | `/api/stations/cities` | Distinct list of cities that actually have stations — backs the frontend's city dropdown |

### Fuel prices
| Method | Endpoint | Notes |
|---|---|---|
| `GET` | `/api/fuel-prices` | Optional `fuelType`, `zipCode` |
| `GET` | `/api/stations/{stationId}/prices` | All prices for one station |
| `POST` | `/api/stations/{stationId}/prices` | Create/update a price — `{ "price": 2.79, "fuelType": "REGULAR" }`. Rate-limited to 10 requests/minute per IP. |
| `GET` | `/api/fuel-prices/cheapest?fuelType=&zipCode=` | `zipCode` optional; ties return multiple stations |

### Health
`GET /api/health`

All errors return a consistent shape: `{ "status": 400, "error": "Bad Request", "message": "...", "timestamp": "..." }`

---

## Price Validation

Two rules, enforced independently on both the client (instant feedback) and the server (source of truth):

1. **First report for a station** (no existing price to compare against): capped at $10.
2. **Any update to an existing price**: must be within $1.00 of the current price. A price with a $2.80 pump reading can't be "corrected" to $0.80 by mistake — it gets rejected with a clear message instead.

---

## Seeding Starting Prices

Many stations don't have a price yet until a real user reports one — a completely normal state for a young crowdsourced app. To avoid a wall of "No price reported" cards, there's an optional one-time bootstrap that fills in a real, honestly-sourced starting price for Regular and Diesel: the U.S. Energy Information Administration's public weekly Gulf Coast (PADD 3) regional average (`https://www.eia.gov/petroleum/gasdiesel/`). It never overwrites a price that already exists, and Midgrade/Premium are deliberately left unseeded since EIA doesn't publish those at this regional granularity — guessing a number for them isn't something the app should do.

```bash
$env:PRICE_SEED_ENABLED="true"
./mvnw spring-boot:run
```

Same crash-proofing as the station import: if it fails, it's logged and skipped, never taking the app down.

---

## Running Locally

**Backend** (requires Java 21, a running MySQL instance, a `fuel_finder` database, and a `DB_PASSWORD` env var):
```bash
cd backend
./mvnw spring-boot:run
```
Runs at `http://localhost:8080`.

**Frontend**:
```bash
cd frontend
npm install
npm run dev
```
Runs at `http://localhost:5173`, reading the backend URL from `VITE_API_URL`.

**Tests**:
```bash
cd backend
./mvnw test
```

---

## Populating Station Data

Station data comes from a one-time, opt-in import against the OpenStreetMap Overpass API — it never runs during normal startup.

```bash
$env:OSM_IMPORT_ENABLED="true"
./mvnw spring-boot:run
```

The import queries Dallas, Fort Worth, and Arlington as three separate requests (a single combined DFW-wide query reliably gets rejected by Overpass's public server as too heavy). To re-import just one region without touching the others' data, also set `OSM_IMPORT_REGION` (e.g. `Arlington`). A failed import is logged and skipped — it can never crash the app or take a live deployment down.

---

## Deployment

- **Backend**: Dockerized (multi-stage Maven build → slim JRE runtime image) and deployed on Railway, alongside a Railway-managed MySQL instance.
- **Frontend**: Deployed on Vercel, built from `frontend/`.
- **CORS**: configured in `WebConfig.java` to explicitly allow the Vercel origin alongside local dev.

---

## Design Decisions

**Why no paid or scraped price API?** Two options were seriously evaluated: a data-aggregator API with a free tier that turned out to be a time-limited trial, and an Apify actor that scrapes GasBuddy's crowdsourced prices. Both were rejected — the first isn't durable for a project meant to stay live indefinitely, and the second isn't something to attach to a public resume repo (GasBuddy doesn't authorize scraping its data, and it would make the app's own user-reporting feature pointless). Prices stay 100% user-submitted, with the validation rules above keeping them honest.

**Why three separate Overpass regions instead of one DFW-wide box?** Tested directly: a single bounding box spanning all three cities reliably got rejected by Overpass's public server (a 504 in ~10 seconds, well under the request's own timeout — the server's dispatcher rejecting it as too heavy, not a slow response). City-sized boxes succeed individually, so the import queries each region separately, spaced out and retried with backoff, and merges the results.

**Why can't a failed import take the site down?** Early in the DFW expansion, it did — a `CommandLineRunner` throwing during Spring Boot startup fails the *entire* application context, meaning the web server itself never comes up. Fixed by catching and logging import failures instead of letting them propagate: the app always starts normally now, with or without fresh station data.

---

## Data Attribution

Station data © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors, available under the [ODbL](https://opendatacommons.org/licenses/odbl/).
