# FuelFinder Project Notes

## 1. Project Goal

**FuelFinder** is a full-stack web application that helps drivers:

* Find nearby gas stations
* Compare fuel prices
* Filter stations by fuel type
* Determine which station has the cheapest gas
* Eventually calculate whether driving farther for cheaper gas actually saves money

The project is also designed as a learning project for practicing real software engineering concepts such as Java, Spring Boot, REST APIs, SQL, databases, system design, testing, debugging, and Git.

---

# 2. Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate
* Maven

### Database

* MySQL
* MySQL Workbench

### Frontend

* React *(planned)*

### Development Tools

* VS Code
* Git
* GitHub

---

# 3. Main Concepts Practiced

* Java and Object-Oriented Programming
* Spring Boot
* Dependency Injection
* REST APIs
* HTTP methods
* JSON
* Controller-Service-Repository architecture
* Spring Data JPA
* Hibernate / ORM
* SQL
* MySQL
* Primary keys
* Foreign keys
* Database relationships
* Enums
* `Optional`
* Environment variables
* Maven
* Git and GitHub
* Debugging
* API testing
* System design

---

# 4. Project Architecture

The backend follows a layered architecture:

```text
Client / Browser
       ↓
Controller
       ↓
Service
       ↓
Repository
       ↓
Hibernate / JPA
       ↓
MySQL
```

## Controller

The controller receives HTTP requests.

Examples:

```text
GET /api/stations
POST /api/stations
```

The controller generally should **not directly communicate with the database**.

Instead, it calls the service layer.

---

## Service

The service contains the application's **business logic**.

Examples of business logic:

* Finding a station before assigning a fuel price
* Setting the `lastUpdated` time
* Sorting stations by price
* Finding the cheapest station
* Calculating savings
* Validating input

The service communicates with repositories.

---

## Repository

The repository handles communication with the database.

Example:

```java
public interface StationRepository
        extends JpaRepository<Station, Long> {
}
```

Spring Data JPA gives us methods such as:

```java
findAll();
findById();
save();
deleteById();
count();
```

without requiring us to write their implementations.

---

## Database

MySQL permanently stores application data.

MySQL Workbench is only a graphical tool used to interact with the MySQL server.

Closing Workbench does **not** delete the database.

---

# 5. Running the Backend Locally

The backend is located inside:

```text
fuel-finder/backend
```

Run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The project uses the **Maven Wrapper**, which means Maven does not need to be installed globally.

The backend normally runs at:

```text
http://localhost:8080
```

---

# 6. Database Configuration

Spring Boot connects to MySQL using:

```text
src/main/resources/application.properties
```

Current configuration:

```properties
spring.application.name=backend

spring.datasource.url=jdbc:mysql://localhost:3306/fuel_finder
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## Environment Variable

The MySQL password is stored in an environment variable:

```text
DB_PASSWORD
```

Instead of putting the real password directly inside:

```properties
application.properties
```

we use:

```properties
spring.datasource.password=${DB_PASSWORD}
```

This prevents database credentials from accidentally being committed to GitHub.

`DB_PASSWORD` is configured as a permanent Windows environment variable on the development computer.

---

# 7. First API Endpoint — Health Check

The first endpoint created was:

```text
GET /api/health
```

Its purpose was simply to verify that Spring Boot was running correctly.

### Concepts Learned

* `@RestController` tells Spring that a class handles API requests.
* `@GetMapping` maps an HTTP GET request to a Java method.
* `localhost:8080` is the local Spring Boot server.

---

# 8. Station Entity

`Station.java` represents a gas station inside the Java application.

It is also mapped to the MySQL `stations` table.

Important annotation:

```java
@Entity
```

This tells JPA:

> This Java class represents persistent database data.

The table mapping is:

```java
@Table(name = "stations")
```

The station currently contains fields such as:

```text
id
name
address
city
state
zipCode
latitude
longitude
```

The MySQL table uses:

```text
zip_code
```

while Java uses:

```java
zipCode
```

The mapping is handled with:

```java
@Column(name = "zip_code")
private String zipCode;
```

---

# 9. Primary Keys and Auto Increment

The station ID uses:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

`@Id` tells JPA that the field is the primary key.

`GenerationType.IDENTITY` tells MySQL to automatically generate IDs.

Example:

```text
1 → Shell
2 → Exxon
3 → QuikTrip
```

When inserting a new station, we do not manually provide the ID.

---

# 10. Station API

## Get All Stations

```text
GET /api/stations
```

Returns every station stored in MySQL.

Controller:

```java
@GetMapping("/api/stations")
public List<Station> getAllStations() {
    return stationService.getAllStations();
}
```

The request flow is:

```text
Browser
   ↓
GET /api/stations
   ↓
StationController
   ↓
StationService
   ↓
StationRepository.findAll()
   ↓
Hibernate / JPA
   ↓
MySQL
   ↓
Station objects
   ↓
JSON
```

---

## Create a Station

```text
POST /api/stations
```

Example JSON:

```json
{
  "name": "Chevron",
  "address": "400 Pioneer Pkwy",
  "city": "Arlington",
  "state": "TX",
  "zipCode": "76010",
  "latitude": 32.708,
  "longitude": -97.11
}
```

Controller:

```java
@PostMapping("/api/stations")
public Station createStation(@RequestBody Station station) {
    return stationService.createStation(station);
}
```

`@RequestBody` tells Spring/Jackson to convert the JSON request into a Java `Station` object.

Conceptually:

```text
JSON
 ↓
Jackson
 ↓
Station object
```

---

# 11. Testing POST Requests with PowerShell

We manually acted as a client by sending JSON into the API.

First, a PowerShell object was created:

```powershell
$body = @{
    name = "Chevron"
    address = "400 Pioneer Pkwy"
    city = "Arlington"
    state = "TX"
    zipCode = "76010"
    latitude = 32.7080
    longitude = -97.1100
} | ConvertTo-Json
```

`ConvertTo-Json` converts the PowerShell object into JSON.

Then the request was sent:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/stations" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Meaning of Each Part

```text
-Uri
```

Specifies which API endpoint receives the request.

```text
-Method Post
```

Specifies that we are creating/sending data.

```text
-ContentType "application/json"
```

Tells Spring that the request body contains JSON.

```text
-Body $body
```

Sends the JSON data.

The complete request flow is:

```text
PowerShell
    ↓
JSON
    ↓
POST /api/stations
    ↓
StationController
    ↓
@RequestBody
    ↓
Station object
    ↓
StationService
    ↓
StationRepository.save()
    ↓
Hibernate
    ↓
SQL INSERT
    ↓
MySQL
```

---

# 12. GET vs POST Testing

Our GET test proved:

```text
Database
   ↓
Java
   ↓
JSON
   ↓
Client
```

Our POST test proved:

```text
Client
   ↓
JSON
   ↓
Java
   ↓
Database
```

Together, these tests show that data can successfully travel through the application in both directions.

---

# 13. Spring Data JPA — Repository Interfaces

One important concept is that repositories such as:

```java
public interface StationRepository
        extends JpaRepository<Station, Long> {
}
```

are **interfaces**.

Normally, an interface cannot directly create objects.

However, Spring Data JPA dynamically creates an implementation for the repository at runtime.

Conceptually:

```text
StationRepository interface
          ↓
Spring Data JPA detects it
          ↓
Spring creates an implementation
          ↓
Spring creates/manages the object
          ↓
StationService receives it through dependency injection
```

This is why we can call:

```java
stationRepository.findAll();
stationRepository.findById();
stationRepository.save();
```

even though we never wrote those methods ourselves.

---

# 14. Dependency Injection

Example from the service:

```java
private final StationRepository stationRepository;

public StationService(StationRepository stationRepository) {
    this.stationRepository = stationRepository;
}
```

We do not manually create the repository with:

```java
new StationRepository();
```

Spring creates and manages the repository object and provides it to the service.

This is called **Dependency Injection**.

The same pattern is used between:

```text
Controller → Service
Service → Repository
```

---

# 15. Hibernate / JPA Entity-to-Table Mapping

`Station.java` contains:

```java
@Entity
```

and:

```properties
spring.jpa.hibernate.ddl-auto=update
```

When Spring Boot starts, Hibernate examines the entity classes and compares them to the existing database structure.

If a required table does not exist, Hibernate can create it.

Example:

```text
Station.java
    ↓
@Entity
    ↓
Hibernate / JPA
    ↓
stations table
```

This is why the `stations` table was automatically recreated when FuelFinder was moved to a new computer and connected to a new `fuel_finder` database.

---

# 16. JPA vs Jackson

These two technologies perform different jobs.

## JPA / Hibernate

Handles:

```text
Java objects ↔ Database
```

Example:

```text
Station object
     ↕
stations table
```

## Jackson

Handles:

```text
Java objects ↔ JSON
```

Example:

```text
Station object
     ↕
JSON response
```

A missing `getZipCode()` method originally caused `zipCode` not to appear in JSON.

Adding:

```java
public String getZipCode() {
    return zipCode;
}
```

allowed Jackson to expose the value correctly.

---

# 17. Fuel Types

Fuel types are represented using a Java enum:

```java
public enum FuelType {
    REGULAR,
    MIDGRADE,
    PREMIUM,
    DIESEL
}
```

An enum restricts a variable to a known set of allowed values.

This is safer than:

```java
String fuelType;
```

because arbitrary strings could be entered.

---

# 18. Storing Enums in MySQL

Inside `FuelPrice`:

```java
@Enumerated(EnumType.STRING)
@Column(name = "fuel_type")
private FuelType fuelType;
```

`EnumType.STRING` tells Hibernate to store the enum name as text.

Example:

```java
FuelType.REGULAR
```

becomes:

```text
REGULAR
```

inside MySQL.

Using strings is safer than storing enum positions such as:

```text
0
1
2
3
```

because changing the enum order could otherwise change the meaning of existing database values.

---

# 19. FuelPrice Entity

Fuel prices are stored separately from stations.

The `fuel_prices` table currently contains:

```text
id
price
fuel_type
station_id
last_updated
```

Example:

```text
id | price | fuel_type | station_id
1  | 2.89  | REGULAR   | 1
```

This means:

> Regular fuel at station 1 costs $2.89.

---

# 20. Why Fuel Prices Have Their Own Table

A station can have multiple prices:

```text
Shell
├── Regular
├── Midgrade
├── Premium
└── Diesel
```

Instead of storing fields such as:

```text
regularPrice
premiumPrice
dieselPrice
```

inside `Station`, prices are stored in their own table.

This gives us a relational design:

```text
Station
   ↓
multiple
   ↓
FuelPrice records
```

---

# 21. Many-to-One Relationship

Inside `FuelPrice`:

```java
@ManyToOne
@JoinColumn(name = "station_id")
private Station station;
```

This means:

```text
Many FuelPrice records
        ↓
One Station
```

Example:

```text
fuel_prices

price | fuel_type | station_id
2.89  | REGULAR   | 1
3.19  | MIDGRADE  | 1
3.49  | PREMIUM   | 1
```

All three records belong to station `1`.

---

# 22. Foreign Keys

`station_id` is a foreign key.

It connects:

```text
fuel_prices.station_id
         ↓
stations.id
```

Example:

```text
fuel_prices.station_id = 1
```

references:

```text
stations.id = 1
```

which may represent:

```text
Shell
```

Foreign keys allow related data to be stored separately without duplicating station information inside every fuel-price record.

---

# 23. Java Objects vs SQL Relationships

Java sees:

```java
private Station station;
```

MySQL sees:

```text
station_id
```

Hibernate handles the translation.

```text
Java

FuelPrice
   └── Station object

        ↓ Hibernate

MySQL

fuel_prices
   └── station_id
         ↓
      stations.id
```

This is an example of **Object-Relational Mapping (ORM)**.

---

# 24. FuelPrice Repository

```java
public interface FuelPriceRepository
        extends JpaRepository<FuelPrice, Long> {
}
```

Like `StationRepository`, Spring automatically creates its implementation.

We receive built-in methods such as:

```java
findAll();
findById();
save();
deleteById();
```

---

# 25. Derived Query Methods

We created:

```java
List<FuelPrice> findByStation_Id(Long stationId);
```

Spring Data JPA examines the method name and generates the appropriate query.

Conceptually:

```java
findByStation_Id(1L)
```

becomes something similar to:

```sql
SELECT *
FROM fuel_prices
WHERE station_id = 1;
```

This is called a **derived query method**.

Spring derives the SQL query from the Java method name.

---

# 26. Optional and findById()

When creating a fuel price, we first find the station:

```java
stationRepository.findById(stationId)
```

`findById()` returns:

```java
Optional<Station>
```

because the station might not exist.

Example:

```text
Station 1 exists
→ Optional contains Station

Station 999 does not exist
→ Optional is empty
```

We handle this with:

```java
.orElseThrow(() -> new RuntimeException("Station not found"));
```

This prevents us from creating a fuel price for a station that does not exist.

---

# 27. Fuel Price Business Logic

When a price is created, the service:

1. Finds the station
2. Connects the station to the fuel price
3. Sets the timestamp
4. Saves the fuel price

Example:

```java
Station station = stationRepository.findById(stationId)
        .orElseThrow(() -> new RuntimeException("Station not found"));

fuelPrice.setStation(station);
fuelPrice.setLastUpdated(LocalDateTime.now());

return fuelPriceRepository.save(fuelPrice);
```

This is a good example of **business logic belonging in the service layer**.

---

# 28. Path Variables

Fuel prices use endpoints such as:

```text
POST /api/stations/1/prices
```

The `1` represents a station ID.

Controller:

```java
@PostMapping("/api/stations/{stationId}/prices")
```

Spring extracts the value with:

```java
@PathVariable Long stationId
```

For:

```text
/api/stations/3/prices
```

Spring gives us:

```text
stationId = 3
```

---

# 29. Fuel Price API

## Get All Fuel Prices

```text
GET /api/fuel-prices
```

Returns all fuel prices in the database.

---

## Create Fuel Price for Station

```text
POST /api/stations/{stationId}/prices
```

Example:

```text
POST /api/stations/1/prices
```

JSON:

```json
{
  "price": 2.89,
  "fuelType": "REGULAR"
}
```

The backend automatically determines:

```text
station_id
last_updated
```

The client does not need to send them.

---

## Get Prices for Specific Station

```text
GET /api/stations/{stationId}/prices
```

Example:

```text
GET /api/stations/1/prices
```

Returns only fuel prices belonging to station `1`.

---

# 30. Same URL, Different HTTP Methods

These are both valid:

```text
GET  /api/stations/1/prices
POST /api/stations/1/prices
```

They use the same URL but represent different operations.

```text
GET
→ retrieve data

POST
→ create data
```

This is an important REST API concept.

---

# 31. Current Fuel Price Flow

Creating a price:

```text
POST /api/stations/1/prices

JSON
{
  "price": 2.89,
  "fuelType": "REGULAR"
}

        ↓

FuelPriceController

        ↓

@PathVariable
stationId = 1

@RequestBody
JSON → FuelPrice

        ↓

FuelPriceService

        ↓

StationRepository.findById(1)

        ↓

FuelPrice.setStation()

        ↓

FuelPrice.setLastUpdated()

        ↓

FuelPriceRepository.save()

        ↓

Hibernate / JPA

        ↓

MySQL
```

Database result:

```text
id | price | fuel_type | station_id | last_updated
1  | 2.89  | REGULAR   | 1          | ...
```

---

# 32. Current Database Relationships

```text
stations
────────────────────────
id          PRIMARY KEY
name
address
city
state
zip_code
latitude
longitude


             ONE
              │
              │
              │
             MANY


fuel_prices
────────────────────────
id          PRIMARY KEY
price
fuel_type
station_id  FOREIGN KEY → stations.id
last_updated
```

---

# 33. Why Latitude and Longitude Are Stored

Latitude and longitude are not necessary for the basic price API, but they will become useful for future FuelFinder features.

Possible uses:

* Calculate distance from the user
* Find nearby stations
* Sort by distance
* Display stations on a map
* Provide directions
* Determine whether cheaper gas is worth driving farther for

Example:

```text
Station A
$2.89/gallon
0.7 miles away

Station B
$2.82/gallon
9.4 miles away
```

Station B is cheaper, but driving farther may cost more fuel than the savings are worth.

This is eventually one of FuelFinder's main goals.

---

# 34. Important Spring Annotations to Remember

### `@RestController`

Marks a class as a REST API controller.

### `@GetMapping`

Handles HTTP GET requests.

### `@PostMapping`

Handles HTTP POST requests.

### `@RequestBody`

Converts JSON request data into a Java object.

### `@PathVariable`

Reads values embedded inside the URL.

### `@Service`

Marks a class as part of the service/business logic layer.

### `@Entity`

Marks a Java class as a JPA database entity.

### `@Table`

Specifies the database table represented by an entity.

### `@Id`

Marks the primary key.

### `@GeneratedValue`

Configures automatic ID generation.

### `@Column`

Maps a Java field to a database column.

### `@Enumerated`

Controls how Java enums are stored.

### `@ManyToOne`

Defines a many-to-one entity relationship.

### `@JoinColumn`

Defines the foreign-key column used for a relationship.

---

# 35. Interview Study Questions

Before an interview, I should be able to explain these without looking at the code:

### Architecture

* What does the controller layer do?
* What does the service layer do?
* What does the repository layer do?
* Why shouldn't controllers directly contain database logic?

### Spring

* What is dependency injection?
* What does `@RestController` do?
* What does `@RequestBody` do?
* What does `@PathVariable` do?
* What does `@Service` do?

### JPA / Hibernate

* What is JPA?
* What is Hibernate?
* What is an ORM?
* Why can `StationRepository` be an interface?
* How does `JpaRepository` provide methods like `findAll()`?
* What does `ddl-auto=update` do?
* What does `@Entity` mean?
* What does `@ManyToOne` mean?

### SQL / Databases

* What is a primary key?
* What is a foreign key?
* Why is `station_id` stored in `fuel_prices`?
* Why are stations and fuel prices stored in separate tables?
* What is a one-to-many relationship?
* What is a many-to-one relationship?

### APIs

* What is a REST API?
* What is JSON?
* What is the difference between GET and POST?
* Why can GET and POST use the same URL?
* What happens when a POST request reaches Spring Boot?

### Java

* What is an interface?
* What is an enum?
* Why use an enum instead of a string?
* What is `Optional`?
* Why does `findById()` return an `Optional`?

---

# 36. Current Project Status

Completed:

* Spring Boot backend setup
* Java 21 setup
* Maven Wrapper setup
* MySQL database setup
* Spring Boot → MySQL connection
* Station entity
* Station repository
* Station service
* Station controller
* GET station endpoint
* POST station endpoint
* FuelType enum
* FuelPrice entity
* FuelPrice repository
* FuelPrice service
* FuelPrice controller
* Station → FuelPrice database relationship
* Foreign-key mapping
* Fuel-price timestamps
* POST fuel-price endpoint
* GET all fuel prices
* GET fuel prices by station
* Manual API testing
* MySQL verification

---

# 37. Next Steps

Possible next features:

1. Find cheapest fuel price
2. Filter by fuel type
3. Sort stations by price
4. Search stations by ZIP code
5. Validate fuel-price input
6. Improve API error handling
7. Add automated tests
8. Build the React frontend
9. Add real station/location data
10. Calculate distance between user and stations
11. Calculate whether driving farther actually saves money

---

# Key Mental Model

The most important overall flow to remember is:

```text
CLIENT
Browser / React / PowerShell
          ↓
HTTP Request + JSON
          ↓
CONTROLLER
Receives API request
          ↓
SERVICE
Business logic
          ↓
REPOSITORY
Database access
          ↓
HIBERNATE / JPA
Java ↔ SQL translation
          ↓
MYSQL
Persistent data
```

And on the way back:

```text
MySQL
  ↓
Hibernate / JPA
  ↓
Java objects
  ↓
Service
  ↓
Controller
  ↓
Jackson converts object to JSON
  ↓
Client
```

That full flow is one of the most important concepts demonstrated by FuelFinder.
