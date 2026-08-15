# FuelFinder

FuelFinder is a full-stack web application for finding nearby gas stations, comparing fuel prices, and eventually determining whether driving farther for cheaper fuel actually saves money.

The project is built with a Java/Spring Boot backend, MySQL database, and a planned React frontend.

## Current Status

The backend MVP is complete and supports:

- Creating and retrieving gas stations

- Filtering stations by ZIP code

- Storing current prices for multiple fuel types

- Updating an existing station's latest fuel price

- Filtering prices by fuel type

- Filtering prices by fuel type and ZIP code

- Sorting matching prices from cheapest to most expensive

- Finding the cheapest fuel price

- Returning multiple stations when there is a cheapest-price tie

- Input validation

- Consistent API error responses

- Automated unit testing with JUnit and Mockito

The next phase is building the React frontend.

---

## Tech Stack

### Backend

- Java 21

- Spring Boot

- Spring MVC

- Spring Data JPA

- Hibernate

- Maven

### Database

- MySQL

### Testing

- JUnit 5

- Mockito

- Spring Boot Test

### Frontend

- React — planned

### Tools

- Git

- GitHub

- VS Code

- MySQL Workbench

---

## Architecture

\`\`\`text

Client / React

      ↓

Controller

      ↓

Service

      ↓

Repository

      ↓

Spring Data JPA / Hibernate

      ↓

MySQL

The backend follows a layered architecture that separates HTTP handling, business logic, and database access.

---

## Data Model

### Station

```
id
```

name

address

city

state

zipCode

latitude

longitude

### FuelPrice

```
id
```

price

fuelType

station

lastUpdated

Supported fuel types:

```
REGULAR
```

MIDGRADE

PREMIUM

DIESEL

A station can have multiple fuel types, while each station/fuel-type combination stores one current price.

---

## API Endpoints

### Stations

```
GET /api/stations
```

Get all stations.

```
GET /api/stations?zipCode=76010
```

Filter stations by ZIP code.

```
POST /api/stations
```

Create a station.

Example:

```
{
```

  "name": "Shell",

  "address": "100 Main St",

  "city": "Arlington",

  "state": "TX",

  "zipCode": "76010",

  "latitude": 32.708,

  "longitude": -97.11

}

### Fuel Prices

```
GET /api/fuel-prices
```

Get all current fuel prices.

```
GET /api/fuel-prices?fuelType=REGULAR
```

Filter by fuel type.

```
GET /api/fuel-prices?fuelType=REGULAR&zipCode=76010
```

Return matching stations sorted from cheapest to most expensive.

```
GET /api/stations/{stationId}/prices
```

Get prices for one station.

```
POST /api/stations/{stationId}/prices
```

Create or update a station's current fuel price.

Example:

```
{
```

  "price": 2.79,

  "fuelType": "REGULAR"

}

```
GET /api/fuel-prices/cheapest?fuelType=REGULAR
```

Find the cheapest stations for a fuel type.

```
GET /api/fuel-prices/cheapest?fuelType=REGULAR&zipCode=76010
```

Find the cheapest stations for a fuel type within a ZIP code.

---

## Validation and Error Handling

Incoming requests are validated using Jakarta Bean Validation.

Examples include:

-  Required station fields 
-  Five-digit ZIP codes 
-  Required fuel types 
-  Positive fuel prices 

Errors use a consistent JSON structure:

```
{
```

  "status": 400,

  "error": "Bad Request",

  "message": "Price must be greater than 0",

  "timestamp": "..."

}

FuelFinder also includes custom handling for missing stations and invalid request bodies.

---

## Testing

The backend currently includes automated tests using JUnit and Mockito.

Tested behaviors include:

-  Creating a new fuel price 
-  Updating an existing price 
-  Missing-station handling 
-  Cheapest-price ties 
-  Cheapest-price filtering by ZIP 
-  Spring application startup 

Run the tests with:

```
cd backend
```

.\mvnw\.cmd test

Current test suite:

```
6 tests
```

0 failures

0 errors

---

## Running the Backend Locally

### Requirements

-  Java 21 
-  MySQL 
-  A `fuel_finder` MySQL database 
- `DB_PASSWORD` environment variable containing the MySQL password 

Clone the repository and navigate to the backend:

```
cd backend
```

Run the Spring Boot application:

```
.\mvnw.cmd spring-boot:run
```

The backend runs at:

```
http://localhost:8080
```

---

## Database Configuration

FuelFinder keeps credentials outside source control.

`application.properties` references the database password through:

```
spring.datasource.password=${DB_PASSWORD}
```

This prevents the real password from being committed to GitHub.

---

## Roadmap

### Backend MVP

-  Spring Boot REST API 
-  MySQL persistence 
-  Station management 
-  Fuel-price management 
-  ZIP filtering 
-  Fuel-type filtering 
-  Sorted price search 
-  Cheapest-price search 
-  DTO architecture 
-  Validation 
-  Error handling 
-  Unit testing 

### Frontend

-  React application 
-  ZIP-code search 
-  Fuel-type selector 
-  Station results 
-  Fuel-price submission 
-  Responsive UI 

### Future Features

-  Maps and location services 
-  Real gas-station data 
-  Distance calculations 
-  Price freshness 
-  Authentication 
-  Deployment 
-  Personalized savings calculations 

---

## Long-Term Goal

FuelFinder eventually aims to answer more than:

> Which station has the cheapest gas?

The goal is to determine:

> Is driving farther for that cheaper gas actually worth it?

A future savings calculator can account for fuel-price differences, driving distance, vehicle fuel economy, and estimated fuel consumption to determine the true savings of choosing another station.

```

```
