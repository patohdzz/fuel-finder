# FuelFinder Project Notes

## Project Goal

FuelFinder is a full-stack web application that helps drivers find nearby gas stations, compare user-submitted fuel prices, and calculate whether driving farther for cheaper gas actually saves money.

## Concepts Implemented
##

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

## My Notes
Project Structure:
- Controller: receives HTTP requests from the browser, does not talk directly to database, instead it calls the service
- Service: contains the application's business logic, eventually will sort station by price, calculate savings, validate user input
- Repository: communicates with MySQL, JpaRepository gives methods like findAll(), save(), findById(), etc.
- Database: MySQL permanently stores application data

-- SpringBoot connects to MySQL through settings in application.properties
-- We used an environment variable for the password so the real password would not be committed to GitHub.

We created a MySQL table for gas stations:
- id
- name
- address
- city
- state
- zip_code

-- The Station Java class represents a station inside the application, also maps it to the stations table


## Spring/JPA Behind-the-Scenes Behavior (returned)

1. Repository Interfaces
StationRepository is only an interface, but Spring Data JPA automatically creates
an implementation at runtime.

This is why we can call methods like:

stationRepository.findAll();
stationRepository.save(...);
stationRepository.findById(...);

without writing the implementation ourselves.

2. Entity-to-Table Mapping
Station.java is marked with @Entity, so Hibernate/JPA knows it represents
database data.

Because application.properties contains:

spring.jpa.hibernate.ddl-auto=update

Hibernate compares our entities to the database schema when the application starts.
If a required table does not exist, Hibernate can create it.

Example:

Station.java
    ↓
@Entity
    ↓
Hibernate / JPA
    ↓
stations table in MySQL

This is why the stations table was automatically recreated on the new computer
when Spring Boot connected to the new fuel_finder database.

## Current flow
Browser
  ↓ GET /api/stations
StationController
  ↓
StationService
  ↓
StationRepository.findAll()
  ↓
Hibernate / JPA
  ↓
MySQL stations table
  ↓
3 Station objects
  ↓
JSON response