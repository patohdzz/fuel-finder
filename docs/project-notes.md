# FuelFinder Project Notes

## Project Goal

FuelFinder is a full-stack web application that helps drivers find nearby gas stations, compare user-submitted fuel prices, and calculate whether driving farther for cheaper gas actually saves money.

## Concepts Implemented

- Core Java
- Spring Boot
- REST APIs
- SQL and databases
- React frontend development
- Testing
- Debugging
- Git and GitHub
- System design
- Problem solving
- Algorithms and data structures

## Tech Stack

- Backend: Java, Spring Boot
- Frontend: React
- Database: MySQL
- Version Control: Git and GitHub

## Running the Backend Locally

Every time I open a new terminal, I need to set the database password as an environment variable before running Spring Boot:

<!-- powershell -->
$env:DB_PASSWORD="my_mysql_password"
cd backend
.\mvnw spring-boot:run

## First API Endpoint

Created a health check endpoint:

GET /api/health

This endpoint returns a simple message to confirm that the Spring Boot backend is running.

Concepts learned:
- A controller handles HTTP requests.
- @RestController tells Spring this class returns API responses.
- @GetMapping maps a GET request URL to a Java method.
- localhost:8080 is where the backend runs locally.


## Station API

Created station endpoints:

GET /api/stations
- Returns all gas stations from the MySQL database.

POST /api/stations
- Accepts JSON data for a gas station.
- Converts the JSON request body into a Java Station object using @RequestBody.
- Saves the station to MySQL using stationRepository.save().

Concepts learned:
- Controller receives HTTP requests.
- Service contains business logic.
- Repository talks to the database.
- JpaRepository provides built-in methods like findAll() and save().
- Spring Boot converts Java objects into JSON responses.
- Hibernate/JPA turns repository methods into SQL queries.