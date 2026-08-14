# FuelFinder Project Notes

## 1. Project Goal

**FuelFinder** is a full-stack web application that helps drivers:

* Find nearby gas stations
* Search stations by ZIP code
* Compare fuel prices
* Filter fuel prices by fuel type
* Determine which station or stations have the cheapest gas
* Eventually calculate whether driving farther for cheaper gas actually saves money

FuelFinder is also a learning project designed to practice real software engineering concepts and prepare for technical interviews.

Concepts practiced include:

* Java
* Object-Oriented Programming
* Spring Boot
* REST APIs
* SQL and relational databases
* MySQL
* Spring Data JPA
* Hibernate
* Layered architecture
* DTOs
* JPQL
* Java Streams
* Testing
* Debugging
* Git and GitHub
* System design
* Full-stack development

---

# 2. Tech Stack

## Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate
* Maven

## Database

* MySQL
* MySQL Workbench

## Frontend

* React *(planned)*

## Development Tools

* VS Code
* Git
* GitHub
* PowerShell
* Browser/API testing

---

# 3. Main Concepts Practiced

* Java and Object-Oriented Programming
* Classes and objects
* Interfaces
* Enums
* `Optional`
* Java Streams
* Lambda expressions
* Method references
* Spring Boot
* Dependency Injection
* REST APIs
* HTTP GET and POST
* JSON
* `@RequestBody`
* `@PathVariable`
* `@RequestParam`
* Controller-Service-Repository architecture
* DTOs
* Spring Data JPA
* Derived query methods
* Custom JPQL queries
* Hibernate / ORM
* SQL
* Aggregate functions such as `MIN()`
* MySQL
* Primary keys
* Foreign keys
* One-to-many / many-to-one relationships
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
Client / Browser / PowerShell / React
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

Each layer has a different responsibility.

---

## Controller

The controller receives HTTP requests and sends responses.

Examples:

```text
GET /api/stations
POST /api/stations
GET /api/fuel-prices
```

The controller generally should **not directly communicate with the database**.

Instead:

```text
Controller
    ↓
Service
```

The controller is mainly responsible for HTTP-related work such as:

* Receiving requests
* Reading path variables
* Reading query parameters
* Reading JSON request bodies
* Calling the correct service method
* Returning results

---

## Service

The service contains the application's **business logic**.

Examples in FuelFinder:

* Finding a station before assigning it a fuel price
* Setting `lastUpdated`
* Creating a `FuelPrice` entity from a request DTO
* Finding cheapest prices
* Converting entities into response DTOs
* Eventually validating input
* Eventually calculating distance and savings

The service communicates with repositories.

```text
Controller
    ↓
Service
    ↓
Repository
```

---

## Repository

Repositories handle database access.

Example:

```java
public interface StationRepository
        extends JpaRepository<Station, Long> {
}
```

Spring Data JPA gives us built-in methods such as:

```java
findAll();
findById();
save();
deleteById();
count();
```

without requiring us to write their implementations ourselves.

Repositories can also contain custom methods such as:

```java
List<Station> findByZipCode(String zipCode);
```

or:

```java
List<FuelPrice> findByFuelType(FuelType fuelType);
```

---

## Hibernate / JPA

JPA and Hibernate sit between our Java application and MySQL.

Conceptually:

```text
Java objects
     ↓
Hibernate / JPA
     ↓
SQL
     ↓
MySQL
```

They handle much of the object-to-database translation automatically.

---

## Database

MySQL permanently stores application data.

MySQL Workbench is only a graphical program used to interact with the MySQL server.

Closing Workbench does **not** delete or shut down the actual database.

---

# 5. Running the Backend Locally

The backend is located inside:

```text
fuel-finder/backend
```

From the project folder:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The project uses the **Maven Wrapper**, so Maven does not need to be globally installed.

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

---

## Environment Variable

The database password is stored using:

```text
DB_PASSWORD
```

Instead of writing the real password directly in:

```text
application.properties
```

we use:

```properties
spring.datasource.password=${DB_PASSWORD}
```

This helps prevent database credentials from accidentally being committed to GitHub.

The development computer currently has `DB_PASSWORD` configured as a Windows environment variable.

---

# 7. First API Endpoint — Health Check

The first endpoint created was:

```text
GET /api/health
```

Its purpose was to verify that the Spring Boot backend was running.

Concepts learned:

* `@RestController` marks a class as an API controller.
* `@GetMapping` maps an HTTP GET request to a Java method.
* `localhost:8080` is the local Spring Boot server.

---

# 8. Station Entity

`Station.java` represents a gas station in the Java application.

It is also mapped to the MySQL `stations` table.

Important annotation:

```java
@Entity
```

This tells JPA:

> This Java class represents persistent database data.

The table is mapped using:

```java
@Table(name = "stations")
```

A station currently contains:

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

Java uses:

```java
zipCode
```

while MySQL uses:

```text
zip_code
```

The mapping is handled using:

```java
@Column(name = "zip_code")
private String zipCode;
```

---

# 9. Why ZIP Code Is a String

ZIP codes contain digits, but they are identifiers rather than numbers used for arithmetic.

Therefore:

```java
String zipCode;
```

is better than:

```java
int zipCode;
```

For example:

```text
02108
```

contains an important leading zero.

Storing it as a string preserves the full value.

This is a general lesson:

> Something containing only digits does not automatically mean it should use a numeric data type.

Examples include:

* ZIP codes
* Phone numbers
* Account numbers
* Some IDs

---

# 10. Primary Keys and Auto Increment

The station ID uses:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

`@Id` marks the primary key.

`GenerationType.IDENTITY` allows MySQL to automatically generate IDs.

Example:

```text
1 → Shell
2 → Exxon
3 → QuikTrip
4 → Chevron
```

When creating a station, the client does not manually need to provide an ID.

---

# 11. Station API

## Get All Stations

```text
GET /api/stations
```

Returns every station stored in MySQL.

The flow is:

```text
Client
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
Jackson
   ↓
JSON
```

---

## Search Stations by ZIP Code

FuelFinder can now filter stations by ZIP code.

Example:

```text
GET /api/stations?zipCode=76010
```

The controller uses:

```java
@RequestParam(required = false) String zipCode
```

If a ZIP code is provided:

```text
zipCode = "76010"
```

the controller calls:

```java
stationService.getStationsByZipCode(zipCode);
```

If no ZIP code is supplied:

```text
zipCode = null
```

the controller returns all stations.

Conceptually:

```text
GET /api/stations
        ↓
zipCode = null
        ↓
getAllStations()
```

versus:

```text
GET /api/stations?zipCode=76010
        ↓
zipCode = "76010"
        ↓
getStationsByZipCode("76010")
```

---

# 12. StationRepository ZIP Query

The repository contains:

```java
List<Station> findByZipCode(String zipCode);
```

Spring Data JPA examines the method name:

```text
findByZipCode
```

and recognizes the `zipCode` field in `Station`.

Conceptually:

```java
findByZipCode("76010");
```

becomes something similar to:

```sql
SELECT *
FROM stations
WHERE zip_code = '76010';
```

We do not manually implement the repository method.

---

# 13. Create a Station

Endpoint:

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

The current station POST controller still accepts a `Station` object directly:

```java
@PostMapping("/api/stations")
public Station createStation(@RequestBody Station station) {
    return stationService.createStation(station);
}
```

`@RequestBody` tells Spring/Jackson to convert JSON into a Java object.

Conceptually:

```text
JSON
 ↓
Jackson
 ↓
Station object
```

---

# 14. Testing POST Requests with PowerShell

PowerShell can act as a client and send HTTP requests to our API.

Example station request:

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

`ConvertTo-Json` converts the PowerShell hashtable into JSON.

Then:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/stations" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

---

## Meaning of the PowerShell Request

### `-Uri`

Specifies the API endpoint receiving the request.

### `-Method Post`

Tells the server this is an HTTP POST request.

### `-ContentType "application/json"`

Tells Spring that the request body contains JSON.

### `-Body $body`

Sends the JSON request body.

---

# 15. GET vs POST Testing

Our GET tests prove:

```text
Database
   ↓
Java
   ↓
JSON
   ↓
Client
```

Our POST tests prove:

```text
Client
   ↓
JSON
   ↓
Java
   ↓
Database
```

Together these prove that information can move through the complete backend in both directions.

---

# 16. Spring Data JPA — Repository Interfaces

One of the most important concepts in the project is that repositories are interfaces.

Example:

```java
public interface StationRepository
        extends JpaRepository<Station, Long> {
}
```

Normally an interface only defines behavior.

However, Spring Data JPA dynamically creates the implementation at runtime.

Conceptually:

```text
StationRepository interface
          ↓
Spring Data JPA detects it
          ↓
Spring creates an implementation
          ↓
Spring creates/manages the repository object
          ↓
StationService receives it
          ↓
We can call repository methods
```

This is why we can use:

```java
stationRepository.findAll();
stationRepository.findById();
stationRepository.save();
```

without implementing them ourselves.

This also applies to:

```java
FuelPriceRepository
```

---

# 17. Dependency Injection

Example:

```java
private final StationRepository stationRepository;

public StationService(StationRepository stationRepository) {
    this.stationRepository = stationRepository;
}
```

We do not do:

```java
new StationRepository();
```

In fact, `StationRepository` is an interface, so we could not instantiate it directly anyway.

Spring creates the implementation and provides it to `StationService`.

This is **Dependency Injection**.

The same idea appears throughout the application:

```text
StationController
      ↓ receives
StationService

StationService
      ↓ receives
StationRepository
```

and:

```text
FuelPriceController
      ↓ receives
FuelPriceService
```

Constructor injection makes dependencies explicit and also makes code easier to test later.

---

# 18. Hibernate / JPA Entity-to-Table Mapping

`Station.java` uses:

```java
@Entity
```

and the application configuration contains:

```properties
spring.jpa.hibernate.ddl-auto=update
```

When Spring Boot starts, Hibernate examines our entities and compares them with the database schema.

With `ddl-auto=update`, Hibernate can create or modify tables needed to match the entities.

Conceptually:

```text
Station.java
    ↓
@Entity
    ↓
Hibernate / JPA
    ↓
stations table
```

This was demonstrated when FuelFinder was moved to another computer and connected to a newly created `fuel_finder` database.

Hibernate was able to recreate the required table structure from the entity classes.

### Important

This is convenient during development.

In larger production systems, database schema changes are often managed more carefully with migration tools rather than relying entirely on `ddl-auto=update`.

---

# 19. JPA vs Hibernate vs Spring Data JPA

These terms are related but not identical.

## JPA

JPA stands for **Java Persistence API**.

It is a specification that defines how Java applications can work with relational databases through objects.

Annotations such as:

```java
@Entity
@Id
@ManyToOne
```

come from the persistence model defined by JPA.

## Hibernate

Hibernate is an ORM framework and a common implementation of JPA.

Hibernate performs much of the actual work translating between:

```text
Java objects
↕
SQL/database rows
```

## Spring Data JPA

Spring Data JPA makes working with JPA easier.

It provides repository abstractions such as:

```java
JpaRepository
```

and features such as:

* Built-in CRUD methods
* Derived query methods
* Custom `@Query` methods

A useful mental model:

```text
Our code
   ↓
Spring Data JPA
   ↓
JPA
   ↓
Hibernate
   ↓
SQL
   ↓
MySQL
```

---

# 20. JPA vs Jackson

JPA/Hibernate and Jackson solve different problems.

## JPA / Hibernate

Handles:

```text
Java objects ↔ Database
```

Example:

```text
FuelPrice entity
       ↕
fuel_prices table
```

## Jackson

Handles:

```text
Java objects ↔ JSON
```

Example:

```text
FuelPriceResponse
       ↕
JSON
```

A previous example involved `zipCode`.

When `getZipCode()` was missing, Jackson did not expose the property correctly in JSON.

Adding:

```java
public String getZipCode() {
    return zipCode;
}
```

allowed Jackson to access it.

---

# 21. Fuel Types

Fuel types are represented with a Java enum:

```java
public enum FuelType {
    REGULAR,
    MIDGRADE,
    PREMIUM,
    DIESEL
}
```

An enum restricts the possible values.

Instead of allowing:

```java
String fuelType;
```

where a programmer might use values such as:

```text
"Regular"
"regular"
"REG"
"gas"
```

we restrict the code to:

```text
REGULAR
MIDGRADE
PREMIUM
DIESEL
```

This gives us stronger type safety.

---

# 22. Storing Enums in MySQL

Inside `FuelPrice`:

```java
@Enumerated(EnumType.STRING)
@Column(name = "fuel_type")
private FuelType fuelType;
```

`EnumType.STRING` tells Hibernate to store:

```java
FuelType.REGULAR
```

as:

```text
REGULAR
```

inside MySQL.

This is generally safer and easier to understand than storing enum positions such as:

```text
0
1
2
3
```

If the order of the enum changed later, ordinal values could become dangerous.

---

# 23. FuelPrice Entity

Fuel prices are stored separately from stations.

The `fuel_prices` table contains:

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

The Java entity contains information conceptually like:

```text
FuelPrice
├── id
├── price
├── fuelType
├── station
└── lastUpdated
```

---

# 24. Why Fuel Prices Have Their Own Table

A station can offer multiple fuel types:

```text
Shell
├── Regular
├── Midgrade
├── Premium
└── Diesel
```

Instead of storing:

```text
regularPrice
midgradePrice
premiumPrice
dieselPrice
```

directly inside `Station`, we created a separate `FuelPrice` entity/table.

This gives us a relational design:

```text
One Station
    ↓
Many FuelPrice records
```

It is more flexible and makes it easier to query prices independently.

---

# 25. Many-to-One Relationship

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
price | fuel_type | station_id
2.79  | REGULAR   | 1
3.15  | MIDGRADE  | 1
3.49  | PREMIUM   | 1
```

All three rows belong to station `1`.

From the opposite perspective:

```text
One Station
    ↓
Many FuelPrices
```

So:

* `Station → FuelPrice` is one-to-many conceptually.
* `FuelPrice → Station` is many-to-one.

---

# 26. Foreign Keys

The `station_id` column in `fuel_prices` is a foreign key.

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

might reference:

```text
stations.id = 1
```

which represents:

```text
Shell
```

Foreign keys allow us to store related information without copying all of the station information into every fuel-price row.

---

# 27. Java Objects vs SQL Relationships

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
JAVA

FuelPrice
   └── Station object

          ↓ Hibernate

MYSQL

fuel_prices
   └── station_id
          ↓
       stations.id
```

This is an example of **Object-Relational Mapping (ORM)**.

---

# 28. FuelPriceRepository

The repository extends:

```java
JpaRepository<FuelPrice, Long>
```

Conceptually:

```java
public interface FuelPriceRepository
        extends JpaRepository<FuelPrice, Long> {
}
```

This automatically provides methods such as:

```java
findAll();
findById();
save();
deleteById();
```

We have also added several custom repository methods.

---

# 29. Derived Query Methods

Spring Data JPA can generate queries by examining repository method names.

One example:

```java
List<FuelPrice> findByStation_Id(Long stationId);
```

Spring understands that:

```text
Station_Id
```

means:

```text
FuelPrice.station.id
```

Conceptually:

```java
findByStation_Id(1L);
```

becomes something similar to:

```sql
SELECT *
FROM fuel_prices
WHERE station_id = 1;
```

This is called a **derived query method**.

---

## Find By Fuel Type

We also created:

```java
List<FuelPrice> findByFuelType(FuelType fuelType);
```

Spring reads:

```text
findByFuelType
```

and recognizes the `fuelType` field in `FuelPrice`.

Conceptually:

```java
findByFuelType(FuelType.REGULAR);
```

becomes approximately:

```sql
SELECT *
FROM fuel_prices
WHERE fuel_type = 'REGULAR';
```

---

## Find Stations By ZIP Code

`StationRepository` contains:

```java
List<Station> findByZipCode(String zipCode);
```

Conceptually:

```java
findByZipCode("76010");
```

becomes approximately:

```sql
SELECT *
FROM stations
WHERE zip_code = '76010';
```

---

# 30. Optional and findById()

When creating a fuel price, the application first finds the station:

```java
stationRepository.findById(stationId)
```

`findById()` returns:

```java
Optional<Station>
```

because the requested station might not exist.

Example:

```text
Station 1 exists
→ Optional contains a Station

Station 999 does not exist
→ Optional.empty
```

We currently handle that using:

```java
.orElseThrow(() -> new RuntimeException("Station not found"));
```

This prevents a fuel price from being created for a station that does not exist.

Later we will improve this by replacing the generic `RuntimeException` with proper API exception handling.

---

# 31. Path Variables

FuelFinder uses URLs such as:

```text
POST /api/stations/1/prices
```

The `1` represents the station ID.

The controller route contains:

```java
@PostMapping("/api/stations/{stationId}/prices")
```

and Spring extracts it using:

```java
@PathVariable Long stationId
```

Example:

```text
/api/stations/3/prices
```

results in:

```text
stationId = 3
```

---

# 32. Query Parameters and @RequestParam

FuelFinder now also uses query parameters.

Example:

```text
GET /api/fuel-prices?fuelType=REGULAR
```

The part after `?` is the query string.

```text
fuelType=REGULAR
```

is a query parameter.

Spring reads it using:

```java
@RequestParam FuelType fuelType
```

Spring can convert:

```text
"REGULAR"
```

into:

```java
FuelType.REGULAR
```

---

## Optional Query Parameters

For some endpoints, the parameter is optional:

```java
@RequestParam(required = false) String zipCode
```

This means both of these requests are valid:

```text
GET /api/stations
```

and:

```text
GET /api/stations?zipCode=76010
```

When the parameter is missing:

```text
zipCode == null
```

---

# 33. Path Variable vs Query Parameter

A path variable is part of the URL path itself.

Example:

```text
/api/stations/1/prices
              ↑
          stationId
```

Handled by:

```java
@PathVariable
```

A query parameter comes after `?`.

Example:

```text
/api/fuel-prices?fuelType=REGULAR
                         ↑
                    query parameter
```

Handled by:

```java
@RequestParam
```

A useful mental model:

```text
Path Variable
→ usually identifies a particular resource

Query Parameter
→ usually filters or modifies the request
```

---

# 34. Fuel Price Filtering

FuelFinder supports:

```text
GET /api/fuel-prices
```

which returns all fuel prices.

It also supports:

```text
GET /api/fuel-prices?fuelType=REGULAR
```

which returns only regular fuel prices.

The controller behaves conceptually like:

```text
fuelType provided?
        ↓
       yes
        ↓
getFuelPricesByFuelType()

        OR

fuelType missing?
        ↓
getAllFuelPrices()
```

The full filtered flow is:

```text
GET /api/fuel-prices?fuelType=REGULAR
                ↓
          @RequestParam
                ↓
     FuelType.REGULAR
                ↓
      FuelPriceController
                ↓
       FuelPriceService
                ↓
FuelPriceRepository.findByFuelType()
                ↓
        Hibernate / JPA
                ↓
             MySQL
```

---

# 35. Cheapest Fuel Price Feature

FuelFinder can find the cheapest price for a selected fuel type.

Example:

```text
GET /api/fuel-prices/cheapest?fuelType=REGULAR
```

Suppose the database contains:

```text
Shell       REGULAR   $2.79
Exxon       REGULAR   $2.95
QuikTrip    REGULAR   $2.79
Chevron     REGULAR   $3.05
```

The minimum is:

```text
$2.79
```

Because Shell and QuikTrip are tied, FuelFinder should return **both**, not arbitrarily return only one.

This requirement led us to create a custom query.

---

# 36. Why We Did Not Just Use findFirst

We originally considered something similar to:

```java
findFirstByFuelTypeOrderByPriceAsc(...)
```

The logic would be:

```text
OrderByPriceAsc
→ put cheapest first

findFirst
→ return the first row
```

That would successfully identify a cheapest record.

However, it has a problem.

If two stations have:

```text
$2.79
```

only one would be returned.

FuelFinder should show every station tied for the cheapest price.

Therefore we use a query that:

```text
1. Calculates MIN(price)
2. Returns every row equal to that minimum
```

---

# 37. Custom JPQL Query

The cheapest-price logic uses a custom query similar to:

```java
@Query("""
    SELECT fp
    FROM FuelPrice fp
    WHERE fp.fuelType = :fuelType
    AND fp.price = (
        SELECT MIN(fp2.price)
        FROM FuelPrice fp2
        WHERE fp2.fuelType = :fuelType
    )
    """)
List<FuelPrice> findCheapestByFuelType(
        @Param("fuelType") FuelType fuelType);
```

This is **JPQL**, not normal SQL.

JPQL stands for:

```text
Java Persistence Query Language
```

---

# 38. JPQL vs SQL

SQL works with database tables and columns.

Example SQL:

```sql
SELECT *
FROM fuel_prices
WHERE fuel_type = 'REGULAR';
```

JPQL works with Java entities and their fields.

Example:

```java
SELECT fp
FROM FuelPrice fp
WHERE fp.fuelType = :fuelType
```

Notice the difference:

```text
SQL
fuel_prices
fuel_type
station_id

JPQL
FuelPrice
fuelType
station
```

Hibernate translates JPQL into SQL before the database executes it.

---

# 39. JPQL Aliases

In:

```java
SELECT fp
FROM FuelPrice fp
```

`fp` is simply an alias we chose.

It is a shorter name for `FuelPrice` inside the query.

We can then write:

```java
fp.price
fp.fuelType
fp.station
```

We could theoretically use a different name:

```java
SELECT fuelPrice
FROM FuelPrice fuelPrice
```

but `fp` is shorter.

The subquery uses:

```java
fp2
```

to distinguish it from the outer query.

Conceptually:

```text
fp
→ FuelPrice records that may be returned

fp2
→ FuelPrice records being inspected
  to determine the minimum price
```

---

# 40. MIN()

The custom query uses:

```java
MIN(fp2.price)
```

`MIN()` is an aggregate function.

It finds the smallest value.

For:

```text
2.79
2.95
2.79
3.05
```

we get:

```text
MIN(price) = 2.79
```

The outer query then asks:

```text
Which matching FuelPrice rows
have price = 2.79?
```

That allows multiple tied stations to be returned.

---

# 41. Cheapest Fuel by ZIP Code

FuelFinder can combine fuel-type filtering with ZIP-code filtering.

Example:

```text
GET /api/fuel-prices/cheapest?fuelType=REGULAR&zipCode=76010
```

This means:

> Find the cheapest regular fuel price only among stations in ZIP code 76010.

Conceptually:

```text
All FuelPrice records
        ↓
fuelType = REGULAR
        ↓
station.zipCode = 76010
        ↓
MIN(price)
        ↓
return every row tied at that minimum
```

---

# 42. Navigating Relationships in JPQL

The custom query can use:

```java
fp.station.zipCode
```

This is possible because `FuelPrice` contains:

```java
private Station station;
```

and `Station` contains:

```java
private String zipCode;
```

JPQL can navigate:

```text
FuelPrice
    ↓
station
    ↓
Station
    ↓
zipCode
```

Hibernate understands the `@ManyToOne` relationship and generates the required database join.

We do not manually need to write:

```text
station_id = stations.id
```

inside our Java code.

The entity relationship already contains that information.

---

# 43. Cheapest Fuel Query by ZIP Code

The repository query is conceptually:

```java
@Query("""
    SELECT fp
    FROM FuelPrice fp
    WHERE fp.fuelType = :fuelType
    AND fp.station.zipCode = :zipCode
    AND fp.price = (
        SELECT MIN(fp2.price)
        FROM FuelPrice fp2
        WHERE fp2.fuelType = :fuelType
        AND fp2.station.zipCode = :zipCode
    )
    """)
List<FuelPrice> findCheapestByFuelTypeAndZipCode(
        @Param("fuelType") FuelType fuelType,
        @Param("zipCode") String zipCode);
```

The inner query determines the minimum.

The outer query returns all rows tied at that minimum.

---

# 44. What @Query Does

`@Query` lets us define a custom JPQL query when the query logic is more complicated than a simple derived method.

Example:

```java
@Query("""
    SELECT fp
    FROM FuelPrice fp
    ...
    """)
```

Spring Data JPA still creates the repository implementation.

We are only telling it what query should be executed for this particular method.

---

# 45. What @Param Does

JPQL can use named parameters:

```java
:fuelType
```

and:

```java
:zipCode
```

The repository method connects Java parameters to these names using:

```java
@Param("fuelType") FuelType fuelType
```

and:

```java
@Param("zipCode") String zipCode
```

Conceptually:

```text
Java parameter
fuelType = FuelType.REGULAR

          ↓

JPQL parameter
:fuelType
```

---

# 46. DTOs — Data Transfer Objects

FuelFinder now uses DTOs for its fuel-price API.

DTO stands for:

```text
Data Transfer Object
```

A DTO represents data that is being transferred between different parts of an application.

We currently have:

```text
FuelPriceRequest
        ↓
what the client sends

FuelPrice
        ↓
database/JPA entity

FuelPriceResponse
        ↓
what the backend sends back
```

These objects have different responsibilities.

---

# 47. Entity vs DTO

An entity represents persistent database data.

Example:

```text
FuelPrice
├── id
├── price
├── fuelType
├── station
└── lastUpdated
```

A DTO represents the API contract.

For example:

```text
FuelPriceRequest
├── price
└── fuelType
```

and:

```text
FuelPriceResponse
├── stationId
├── stationName
├── address
├── city
├── state
├── zipCode
├── price
├── fuelType
└── lastUpdated
```

A useful mental model:

```text
ENTITY
→ how the application/database represents data internally

DTO
→ how data enters or leaves the API
```

---

# 48. Why Not Expose Entities Directly?

Returning entities directly can tightly connect the API to the database model.

For example, `FuelPrice` contains:

```java
private Station station;
```

Returning the entity directly could produce a nested response containing the entire station object.

Instead, the frontend may only need:

```json
{
  "stationId": 1,
  "stationName": "Shell",
  "address": "100 Main St",
  "city": "Arlington",
  "state": "TX",
  "zipCode": "76010",
  "price": 2.79,
  "fuelType": "REGULAR",
  "lastUpdated": "..."
}
```

A DTO lets us control exactly what the API exposes.

Benefits include:

* Cleaner responses
* Better API security/control
* Less coupling between database entities and API responses
* Easier frontend development
* Easier future changes

---

# 49. FuelPriceRequest DTO

When a client creates a fuel price, it sends:

```json
{
  "price": 3.15,
  "fuelType": "MIDGRADE"
}
```

Spring converts this into:

```java
FuelPriceRequest
```

The DTO contains only:

```text
price
fuelType
```

This is important because the client should **not** be allowed to control:

```text
id
station
lastUpdated
```

The backend determines those values.

---

## Empty Constructor in the Request DTO

`FuelPriceRequest` contains an empty constructor:

```java
public FuelPriceRequest() {
}
```

Jackson can conceptually do:

```java
FuelPriceRequest request = new FuelPriceRequest();

request.setPrice(3.15);
request.setFuelType(FuelType.MIDGRADE);
```

This allows JSON to be converted into the Java DTO.

---

# 50. FuelPriceResponse DTO

The API returns a flattened `FuelPriceResponse`.

Example:

```json
{
  "stationId": 1,
  "stationName": "Shell",
  "address": "100 Main St",
  "city": "Arlington",
  "state": "TX",
  "zipCode": "76010",
  "price": 3.15,
  "fuelType": "MIDGRADE",
  "lastUpdated": "2026-08-13T21:09:54.9710523"
}
```

This is easier for a React frontend to use than a deeply nested entity structure.

---

# 51. Converting an Entity to a DTO

`FuelPriceService` contains a helper method similar to:

```java
private FuelPriceResponse toResponse(FuelPrice fuelPrice) {

    Station station = fuelPrice.getStation();

    return new FuelPriceResponse(
            station.getId(),
            station.getName(),
            station.getAddress(),
            station.getCity(),
            station.getState(),
            station.getZipCode(),
            fuelPrice.getPrice(),
            fuelPrice.getFuelType(),
            fuelPrice.getLastUpdated()
    );
}
```

This converts:

```text
FuelPrice entity
       +
Station entity
       ↓
FuelPriceResponse
```

The method is:

```java
private
```

because it is only an internal helper used by `FuelPriceService`.

Other classes do not need to call it directly.

---

# 52. Java Streams

Our repositories return entities:

```java
List<FuelPrice>
```

but our GET services now return:

```java
List<FuelPriceResponse>
```

We convert the list using a Java Stream.

Example:

```java
return fuelPriceRepository.findAll()
        .stream()
        .map(this::toResponse)
        .toList();
```

The flow is:

```text
List<FuelPrice>
      ↓
.stream()
      ↓
.map(...)
      ↓
convert every FuelPrice
      ↓
FuelPriceResponse objects
      ↓
.toList()
      ↓
List<FuelPriceResponse>
```

---

# 53. What .map() Does

`map()` transforms each item in a Stream.

Example:

```java
.map(this::toResponse)
```

means:

> For every `FuelPrice`, convert it into a `FuelPriceResponse`.

Conceptually:

```text
FuelPrice #1
    ↓
toResponse()
    ↓
FuelPriceResponse #1

FuelPrice #2
    ↓
toResponse()
    ↓
FuelPriceResponse #2
```

---

# 54. Method References

This:

```java
.map(this::toResponse)
```

is a Java **method reference**.

It is shorthand for:

```java
.map(fuelPrice -> toResponse(fuelPrice))
```

Both mean:

> Take the current object and pass it into `toResponse()`.

---

# 55. What .toList() Does

After `map()` converts the objects, we need to collect them into a new list.

```java
.toList();
```

creates:

```java
List<FuelPriceResponse>
```

So:

```text
Repository
    ↓
List<FuelPrice>
    ↓
stream
    ↓
map
    ↓
List<FuelPriceResponse>
```

---

# 56. Current Fuel Price POST Flow

The fuel-price POST flow has been improved using DTOs.

Endpoint:

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
  "price": 3.15,
  "fuelType": "MIDGRADE"
}
```

The complete flow is now:

```text
Client / PowerShell
        ↓
HTTP POST + JSON
        ↓
FuelPriceController
        ↓

@PathVariable
stationId = 1

@RequestBody
JSON
  ↓
FuelPriceRequest
        ↓
FuelPriceService
        ↓
StationRepository.findById(1)
        ↓
Station object found
        ↓
new FuelPrice()
        ↓
copy:
request.getPrice()
request.getFuelType()
        ↓
backend sets:
FuelPrice.station
FuelPrice.lastUpdated
        ↓
FuelPriceRepository.save()
        ↓
Hibernate / JPA
        ↓
SQL INSERT
        ↓
MySQL
        ↓
saved FuelPrice entity
        ↓
toResponse()
        ↓
FuelPriceResponse
        ↓
Jackson
        ↓
JSON response
        ↓
Client
```

This is an important improvement over the original version.

Previously:

```text
JSON
 ↓
FuelPrice entity directly
```

Now:

```text
JSON
 ↓
FuelPriceRequest
 ↓
FuelPrice entity
 ↓
FuelPriceResponse
 ↓
JSON
```

---

# 57. Why the Backend Controls Station and Timestamp

The incoming request only contains:

```json
{
  "price": 3.15,
  "fuelType": "MIDGRADE"
}
```

The station comes from:

```text
/api/stations/{stationId}/prices
```

For:

```text
/api/stations/1/prices
```

the server determines:

```text
stationId = 1
```

The server also determines:

```java
LocalDateTime.now()
```

for `lastUpdated`.

This prevents the client from controlling internal fields that should be managed by the backend.

---

# 58. Fuel Price Business Logic

The service now does several steps when a new fuel price is created.

Conceptually:

```text
1. Receive FuelPriceRequest
2. Find requested Station
3. Create new FuelPrice
4. Copy price
5. Copy fuelType
6. Set Station
7. Set lastUpdated
8. Save entity
9. Convert saved entity to FuelPriceResponse
10. Return response
```

This is a strong example of why the service layer exists.

The controller should not contain all of this logic.

---

# 59. Current Fuel Price GET Responses

All current fuel-price GET endpoints return:

```java
FuelPriceResponse
```

rather than exposing `FuelPrice` entities directly.

These include:

```text
GET /api/fuel-prices
```

```text
GET /api/fuel-prices?fuelType=REGULAR
```

```text
GET /api/stations/{stationId}/prices
```

```text
GET /api/fuel-prices/cheapest?fuelType=REGULAR
```

```text
GET /api/fuel-prices/cheapest?fuelType=REGULAR&zipCode=76010
```

This gives the API a consistent response format.

---

# 60. Same URL, Different HTTP Methods

These are both valid:

```text
GET  /api/stations/1/prices
POST /api/stations/1/prices
```

The URL is the same, but the HTTP method changes the operation.

```text
GET
→ retrieve data

POST
→ create data
```

This is an important REST API concept.

---

# 61. Current FuelFinder API

## Health

### Check Backend

```text
GET /api/health
```

Purpose:

```text
Verify Spring Boot is running.
```

---

## Stations

### Get All Stations

```text
GET /api/stations
```

---

### Search Stations by ZIP Code

```text
GET /api/stations?zipCode=76010
```

---

### Create Station

```text
POST /api/stations
```

---

## Fuel Prices

### Get All Fuel Prices

```text
GET /api/fuel-prices
```

---

### Filter Fuel Prices by Fuel Type

```text
GET /api/fuel-prices?fuelType=REGULAR
```

Other valid enum values:

```text
MIDGRADE
PREMIUM
DIESEL
```

---

### Get Fuel Prices for One Station

```text
GET /api/stations/{stationId}/prices
```

Example:

```text
GET /api/stations/1/prices
```

---

### Create Fuel Price

```text
POST /api/stations/{stationId}/prices
```

Example:

```text
POST /api/stations/1/prices
```

Request:

```json
{
  "price": 3.15,
  "fuelType": "MIDGRADE"
}
```

---

### Find Cheapest Price by Fuel Type

```text
GET /api/fuel-prices/cheapest?fuelType=REGULAR
```

Returns all records tied for the minimum regular price.

---

### Find Cheapest Price by Fuel Type and ZIP Code

```text
GET /api/fuel-prices/cheapest?fuelType=REGULAR&zipCode=76010
```

Returns all records tied for the minimum regular price among stations in ZIP code `76010`.

---

# 62. Current Database Structure

```text
stations
────────────────────────
id           PRIMARY KEY
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
id           PRIMARY KEY
price
fuel_type
station_id   FOREIGN KEY → stations.id
last_updated
```

---

# 63. Why Latitude and Longitude Are Stored

Latitude and longitude are not yet necessary for basic ZIP-code and price filtering.

However, they will become useful for future FuelFinder features:

* Calculate distance from the user
* Find nearby stations
* Sort by distance
* Display stations on a map
* Provide directions
* Compare distance and price
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

Station B has a lower advertised price.

However, the extra driving may cost more fuel than the savings are worth.

Calculating that tradeoff is one of FuelFinder's eventual core features.

---

# 64. Important Spring Annotations

## `@RestController`

Marks a class as a REST API controller.

---

## `@GetMapping`

Maps an HTTP GET request to a Java method.

---

## `@PostMapping`

Maps an HTTP POST request to a Java method.

---

## `@RequestBody`

Tells Spring/Jackson to convert JSON request data into a Java object.

Example:

```java
@RequestBody FuelPriceRequest request
```

---

## `@PathVariable`

Reads a value embedded in the URL path.

Example:

```text
/api/stations/1/prices
```

```java
@PathVariable Long stationId
```

---

## `@RequestParam`

Reads a query parameter.

Example:

```text
/api/fuel-prices?fuelType=REGULAR
```

```java
@RequestParam FuelType fuelType
```

---

## `@Service`

Marks a class as part of the service/business-logic layer.

---

## `@Entity`

Marks a Java class as a JPA entity representing persistent database data.

---

## `@Table`

Specifies the table represented by an entity.

---

## `@Id`

Marks the entity's primary key.

---

## `@GeneratedValue`

Configures automatic primary-key generation.

---

## `@Column`

Maps a Java field to a database column or configures column behavior.

---

## `@Enumerated`

Controls how Java enums are persisted.

---

## `@ManyToOne`

Defines a many-to-one relationship.

---

## `@JoinColumn`

Defines the foreign-key column used for an entity relationship.

---

## `@Query`

Defines a custom JPQL query in a Spring Data JPA repository.

---

## `@Param`

Maps a Java method parameter to a named parameter inside a JPQL query.

---

# 65. Important Java Concepts Demonstrated

## Interface

Repositories such as:

```java
StationRepository
```

and:

```java
FuelPriceRepository
```

are interfaces.

Spring dynamically provides implementations.

---

## Enum

`FuelType` restricts fuel types to predefined values.

---

## Optional

Used when a value may or may not exist.

Example:

```java
Optional<Station>
```

from:

```java
findById()
```

---

## List

Used when an operation may return multiple objects.

Example:

```java
List<FuelPrice>
```

A ZIP code can contain multiple stations.

A fuel type can contain multiple prices.

Multiple stations can also tie for the cheapest price.

---

## Stream

Allows collections to be processed using a pipeline.

Example:

```java
.stream()
.map(...)
.toList()
```

---

## Lambda

Example:

```java
fuelPrice -> toResponse(fuelPrice)
```

---

## Method Reference

Shorter version:

```java
this::toResponse
```

---

# 66. Important SQL / Database Concepts Demonstrated

## Primary Key

Uniquely identifies a row.

Example:

```text
stations.id
```

---

## Foreign Key

References the primary key of another table.

Example:

```text
fuel_prices.station_id
        ↓
stations.id
```

---

## One-to-Many Relationship

One station can have many fuel-price records.

---

## Many-to-One Relationship

Many fuel-price records can belong to one station.

---

## SELECT

Used to retrieve data.

---

## INSERT

Used when saving new rows.

Hibernate generates these SQL statements when repository methods such as:

```java
save()
```

are used.

---

## WHERE

Used to filter results.

Example conceptually:

```sql
WHERE fuel_type = 'REGULAR'
```

---

## MIN()

Finds the smallest value.

Example:

```sql
MIN(price)
```

---

# 67. Current Repository Methods to Know

## StationRepository

```java
List<Station> findByZipCode(String zipCode);
```

Purpose:

```text
Find all stations in a ZIP code.
```

---

## FuelPriceRepository

```java
List<FuelPrice> findByStation_Id(Long stationId);
```

Purpose:

```text
Find all prices belonging to one station.
```

---

```java
List<FuelPrice> findByFuelType(FuelType fuelType);
```

Purpose:

```text
Find all prices belonging to one fuel type.
```

---

Custom JPQL queries are used to:

```text
Find all prices tied for the minimum
for a selected fuel type.
```

and:

```text
Find all prices tied for the minimum
for a selected fuel type and ZIP code.
```

---

# 68. Current Request and Response Model

A very important mental model is:

```text
REQUEST
FuelPriceRequest
        ↓
SERVICE
        ↓
ENTITY
FuelPrice
        ↓
DATABASE
```

Then:

```text
DATABASE
        ↓
ENTITY
FuelPrice
        ↓
SERVICE
        ↓
DTO
FuelPriceResponse
        ↓
CLIENT
```

This separation is more maintainable than using the same object for everything.

---

# 69. Full Fuel Price POST Mental Model

```text
CLIENT
PowerShell / future React frontend
               ↓
HTTP POST
               ↓
JSON
               ↓
CONTROLLER
FuelPriceController
               ↓
@RequestBody
               ↓
FuelPriceRequest
               ↓
SERVICE
FuelPriceService
               ↓
find Station
               ↓
create FuelPrice
               ↓
set price
set fuel type
set station
set timestamp
               ↓
REPOSITORY
FuelPriceRepository.save()
               ↓
HIBERNATE / JPA
               ↓
SQL INSERT
               ↓
MYSQL
               ↓
saved FuelPrice
               ↓
SERVICE
toResponse()
               ↓
FuelPriceResponse
               ↓
CONTROLLER
               ↓
JACKSON
               ↓
JSON
               ↓
CLIENT
```

---

# 70. Full Cheapest-Price Search Mental Model

Example request:

```text
GET /api/fuel-prices/cheapest
    ?fuelType=REGULAR
    &zipCode=76010
```

Flow:

```text
CLIENT
        ↓
HTTP GET
        ↓
FuelPriceController
        ↓
@RequestParam
        ↓

fuelType = REGULAR
zipCode = "76010"

        ↓
FuelPriceService
        ↓
FuelPriceRepository
        ↓
custom JPQL
        ↓
Filter:
fuelType = REGULAR
        ↓
Navigate:
FuelPrice → Station → zipCode
        ↓
Filter:
zipCode = 76010
        ↓
MIN(price)
        ↓
Find every row equal to minimum
        ↓
List<FuelPrice>
        ↓
Service
        ↓
.stream()
.map(this::toResponse)
.toList()
        ↓
List<FuelPriceResponse>
        ↓
Controller
        ↓
Jackson
        ↓
JSON
        ↓
CLIENT
```

This single feature combines many concepts learned in the project.

---

# 71. Interview Study Questions

Before an interview, I should be able to explain these concepts without looking at the code.

## Architecture

* What does the controller layer do?
* What does the service layer do?
* What does the repository layer do?
* What does the database layer do?
* Why shouldn't controllers directly contain database logic?
* What does separation of concerns mean?
* Why does business logic belong in the service layer?

---

## Spring

* What is Spring Boot?
* What is Dependency Injection?
* What is constructor injection?
* What does `@RestController` do?
* What does `@Service` do?
* What does `@GetMapping` do?
* What does `@PostMapping` do?
* What does `@RequestBody` do?
* What does `@PathVariable` do?
* What does `@RequestParam` do?
* What is the difference between `@PathVariable` and `@RequestParam`?

---

## Spring Data JPA

* What is Spring Data JPA?
* Why can `StationRepository` be an interface?
* Who implements the repository?
* How does `JpaRepository` provide `findAll()` and `save()`?
* What is a derived query method?
* How does `findByFuelType()` work without an implementation?
* What does `@Query` do?
* What does `@Param` do?

---

## JPA / Hibernate

* What is JPA?
* What is Hibernate?
* What is the difference between JPA and Hibernate?
* What is ORM?
* What does `@Entity` mean?
* What does `@ManyToOne` mean?
* What does `@JoinColumn` mean?
* How does Java's `Station station` become `station_id` in MySQL?
* What does `ddl-auto=update` do?

---

## JPQL

* What is JPQL?
* How is JPQL different from SQL?
* Why do we write `FuelPrice` instead of `fuel_prices`?
* What is the purpose of aliases such as `fp` and `fp2`?
* What does `MIN()` do?
* Why did we use a custom query for cheapest prices?
* How does `fp.station.zipCode` work?
* How does Hibernate know how to join the tables?

---

## SQL / Databases

* What is a primary key?
* What is a foreign key?
* Why is `station_id` stored in `fuel_prices`?
* Why are stations and prices in separate tables?
* What is a one-to-many relationship?
* What is a many-to-one relationship?
* What does `SELECT` do?
* What does `WHERE` do?
* What does `MIN()` do?
* How would you find the cheapest price in SQL?
* How would you return multiple rows tied for the minimum?

---

## REST APIs

* What is a REST API?
* What is JSON?
* What is an HTTP request?
* What is an HTTP response?
* What is the difference between GET and POST?
* Why can GET and POST use the same URL?
* What is a query parameter?
* What is a path variable?
* What happens when a POST request reaches Spring Boot?
* How does JSON become a Java object?

---

## Java

* What is an interface?
* What is an enum?
* Why use an enum instead of a string?
* What is `Optional`?
* Why does `findById()` return `Optional`?
* What is a `List`?
* Why do queries such as `findByFuelType()` return a list?
* What is a Stream?
* What does `.map()` do?
* What does `.toList()` do?
* What is a lambda?
* What is a method reference?
* What does `this::toResponse` mean?

---

## DTOs

* What does DTO stand for?
* What is the difference between an entity and a DTO?
* Why shouldn't an API always return database entities?
* What is `FuelPriceRequest` used for?
* What is `FuelPriceResponse` used for?
* Why does `FuelPriceRequest` not contain `station`?
* Why does the backend set `lastUpdated`?
* Why does the backend determine the station?
* Why is a response DTO easier for React to consume?

---

# 72. Current Project Status

## Completed

* Spring Boot backend setup
* Java 21 setup
* Maven Wrapper setup
* MySQL installation and database setup
* MySQL Workbench setup
* Spring Boot → MySQL connection
* Environment-variable database password
* Health-check endpoint
* Station entity
* Station repository
* Station service
* Station controller
* Station GET endpoint
* Station POST endpoint
* Station search by ZIP code
* `FuelType` enum
* `FuelPrice` entity
* `FuelPriceRepository`
* `FuelPriceService`
* `FuelPriceController`
* Station → FuelPrice database relationship
* `@ManyToOne`
* Foreign-key mapping
* Fuel-price timestamps
* Fuel-price POST endpoint
* GET all fuel prices
* GET fuel prices by station
* Filter fuel prices by fuel type
* Derived JPA query methods
* `@RequestParam`
* ZIP-code filtering
* Cheapest-price business logic
* Custom JPQL
* `@Query`
* `@Param`
* SQL/JPQL `MIN()`
* Tie handling for cheapest prices
* Cheapest-price filtering by ZIP code
* JPQL relationship navigation
* `FuelPriceRequest` DTO
* `FuelPriceResponse` DTO
* Entity → DTO conversion
* Java Streams
* `.map()`
* `.toList()`
* Method references
* Clean fuel-price GET responses
* Clean fuel-price POST response
* PowerShell API testing
* Browser API testing
* MySQL Workbench verification
* Foreign-key verification
* Git/GitHub project tracking

---

# 73. Next Steps

The next phase should focus on making the backend more robust before starting major frontend work.

## Immediate Backend Work

1. Add fuel-price input validation.
2. Validate that prices cannot be negative or zero.
3. Validate required fields.
4. Add proper exception handling.
5. Replace generic `RuntimeException` behavior.
6. Return meaningful HTTP status codes.
7. Handle nonexistent station IDs cleanly.
8. Handle invalid fuel-type input cleanly.
9. Validate ZIP-code input.
10. Create consistent API error responses.

## Testing

11. Add automated service tests.
12. Add repository tests where useful.
13. Add controller/API tests.
14. Test failure cases in addition to success cases.

## Business Logic

15. Decide how old/stale fuel-price reports should be handled.
16. Determine whether each station should have one current price per fuel type or a price history.
17. Potentially prevent duplicate station data.
18. Add price sorting beyond only returning the minimum.

## Frontend

19. Create the React frontend.
20. Connect React to Spring Boot.
21. Create a ZIP-code search form.
22. Allow users to select fuel type.
23. Display station cards.
24. Display prices and `lastUpdated`.
25. Display cheapest results.

## Future FuelFinder Features

26. Add real station/location data.
27. Use latitude and longitude.
28. Find nearby stations.
29. Calculate distance from the user.
30. Sort by distance.
31. Display stations on a map.
32. Add directions.
33. Calculate fuel consumed by driving farther.
34. Compare fuel savings with travel cost.
35. Answer FuelFinder's main question:

```text
Is driving farther for cheaper gas actually worth it?
```

---

# 74. Important Concepts to Be Able to Explain From This Project

FuelFinder currently demonstrates:

```text
Java
Object-Oriented Programming
Interfaces
Enums
Optional
Lists
Java Streams
Lambda expressions
Method references

Spring Boot
Spring MVC
Dependency Injection
REST APIs
HTTP
JSON
Controllers
Services
Repositories

Spring Data JPA
JpaRepository
Derived queries
Custom queries
JPQL

JPA
Hibernate
ORM
Entities
Relationships

MySQL
Tables
Primary keys
Foreign keys
One-to-many
Many-to-one
SELECT
WHERE
MIN

DTOs
Request DTOs
Response DTOs
Entity-to-DTO mapping

PowerShell API testing
Debugging
Git
GitHub
System design
Layered architecture
```

---

# 75. Key Mental Model

The most important overall architecture to remember is:

```text
CLIENT
Browser / PowerShell / future React app
                  ↓
            HTTP Request
                  ↓
             CONTROLLER
        Handles HTTP/API details
                  ↓
              SERVICE
         Business/application logic
                  ↓
            REPOSITORY
            Database access
                  ↓
          HIBERNATE / JPA
       Java ↔ Database translation
                  ↓
               MYSQL
          Persistent storage
```

For fuel prices, DTOs now sit around the entity boundary:

```text
CLIENT
   ↓
JSON
   ↓
FuelPriceRequest
   ↓
Controller
   ↓
Service
   ↓
FuelPrice entity
   ↓
Repository
   ↓
Hibernate / JPA
   ↓
MySQL
```

Then on the way back:

```text
MySQL
   ↓
Hibernate / JPA
   ↓
FuelPrice entity
   ↓
Service
   ↓
toResponse()
   ↓
FuelPriceResponse
   ↓
Controller
   ↓
Jackson
   ↓
JSON
   ↓
Client
```

For the cheapest-price feature:

```text
Request
fuelType + optional zipCode
            ↓
Controller
            ↓
Service
            ↓
Repository
            ↓
JPQL
            ↓
filter fuel type
            ↓
optionally filter Station.zipCode
            ↓
MIN(price)
            ↓
return ALL rows tied at minimum
            ↓
FuelPrice entities
            ↓
DTO conversion
            ↓
FuelPriceResponse list
            ↓
JSON
```

If I can confidently explain these flows without looking at the code, then I understand many of the most important concepts FuelFinder has taught so far.


# 76. Latest-Price-Only Design

For the current FuelFinder MVP, we decided **not to keep fuel-price history yet**.

Instead, each station should have at most one current price for each fuel type.

The important combination is:

```text
station + fuelType
```

For example, these are all separate valid records:

```text
Shell + REGULAR
Shell + MIDGRADE
Shell + PREMIUM
Exxon + REGULAR
Exxon + PREMIUM
```

But this should not happen:

```text
Shell + REGULAR → $2.79
Shell + REGULAR → $2.85
Shell + REGULAR → $2.92
```

Instead, submitting a new regular price for Shell should update the existing Shell + REGULAR record.

The rule is:

```text
same station + same fuel type
→ UPDATE existing price
```

```text
same station + different fuel type
→ INSERT new record
```

```text
different station + same fuel type
→ INSERT new record
```

```text
different station + different fuel type
→ INSERT new record
```

This means the database stores the **latest/current price** for each station and fuel type.

---

# 77. Finding an Existing Station + Fuel Type

To support the latest-price-only design, `FuelPriceRepository` contains:

```java
Optional<FuelPrice> findByStation_IdAndFuelType(
        Long stationId,
        FuelType fuelType);
```

Spring Data JPA interprets:

```text
findByStation_IdAndFuelType
```

as:

```text
Find a FuelPrice where:

station.id = stationId
AND
fuelType = fuelType
```

Conceptually:

```java
findByStation_IdAndFuelType(
        1L,
        FuelType.REGULAR
);
```

becomes approximately:

```sql
SELECT *
FROM fuel_prices
WHERE station_id = 1
AND fuel_type = 'REGULAR';
```

The return type is:

```java
Optional<FuelPrice>
```

because the combination may already exist or may not exist.

```text
Record exists
→ Optional contains FuelPrice

Record does not exist
→ Optional.empty
```

---

# 78. Updating Instead of Always Inserting

Originally, every fuel-price POST created:

```java
new FuelPrice();
```

This meant every submission created another database row.

The updated service logic first searches for an existing record:

```java
FuelPrice fuelPrice = fuelPriceRepository
        .findByStation_IdAndFuelType(
                stationId,
                request.getFuelType()
        )
        .orElse(new FuelPrice());
```

This means:

```text
Search station + fuelType
          ↓
Does record exist?
       /       \
     YES       NO
      ↓         ↓
 use existing  new FuelPrice()
      \         /
          ↓
   set latest price
   set latest timestamp
          ↓
        save()
```

Then:

```java
fuelPrice.setPrice(request.getPrice());
fuelPrice.setFuelType(request.getFuelType());
fuelPrice.setStation(station);
fuelPrice.setLastUpdated(LocalDateTime.now());
```

Finally:

```java
fuelPriceRepository.save(fuelPrice);
```

---

# 79. How save() Can INSERT or UPDATE

The same JPA method:

```java
fuelPriceRepository.save(fuelPrice);
```

can result in different database operations.

If the entity is new and has no existing ID:

```text
id = null
```

Hibernate performs an operation conceptually like:

```sql
INSERT INTO fuel_prices (...);
```

If the entity already represents an existing row:

```text
id = existing database ID
```

Hibernate updates that record.

Conceptually:

```sql
UPDATE fuel_prices
SET price = ...,
    last_updated = ...
WHERE id = ...;
```

So:

```text
new entity
→ INSERT
```

while:

```text
existing entity
→ UPDATE
```

This behavior lets the service use the same `save()` method for both cases.

---

# 80. Testing INSERT vs UPDATE

We manually tested both branches.

## Existing Combination

A station already had a `MIDGRADE` price.

Submitting a new `MIDGRADE` price for that same station:

```text
same station
+
same fuel type
```

updated:

```text
price
last_updated
```

while keeping the same database row.

This proved the application performed an update rather than creating a duplicate.

## New Combination

We also submitted a fuel type that did not already exist for another station.

Because the:

```text
station + fuelType
```

combination did not exist, a new row was inserted.

This confirmed both branches:

```text
existing combination
→ UPDATE
```

```text
new combination
→ INSERT
```

---

# 81. Composite Unique Constraint

Application logic tries to ensure that each:

```text
station + fuelType
```

combination appears only once.

However, an important data-integrity rule should also be enforced by the database when possible.

We added a composite unique constraint:

```sql
ALTER TABLE fuel_prices
ADD CONSTRAINT uq_station_fuel_type
UNIQUE (station_id, fuel_type);
```

The constraint is called **composite** because uniqueness depends on multiple columns together.

Neither column needs to be unique individually.

This is valid:

```text
station_id | fuel_type
-----------|----------
1          | REGULAR
1          | PREMIUM
2          | REGULAR
```

But this is not valid:

```text
1 | REGULAR
1 | REGULAR
```

The combination:

```text
station_id + fuel_type
```

cannot appear twice.

---

# 82. Application Validation vs Database Constraints

FuelFinder now protects some rules at multiple layers.

## Application Layer

The service checks whether the record exists:

```java
findByStation_IdAndFuelType(...)
```

and decides whether to update or insert.

## Database Layer

MySQL enforces:

```text
UNIQUE(station_id, fuel_type)
```

Conceptually:

```text
APPLICATION
      ↓
tries to maintain correct data

DATABASE
      ↓
guarantees an important integrity rule
```

This is an important backend principle:

> Important data-integrity rules should not rely only on application behavior when the database can enforce them too.

---

# 83. Input Validation

FuelFinder now validates incoming fuel-price requests before allowing them to reach the service/database.

The project uses:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Validation rules are defined in:

```text
FuelPriceRequest
```

using Jakarta Validation annotations.

---

# 84. Fuel Price Validation Rules

`FuelPriceRequest` now contains rules similar to:

```java
@NotNull(message = "Price is required")
@Positive(message = "Price must be greater than 0")
private Double price;

@NotNull(message = "Fuel type is required")
private FuelType fuelType;
```

## `@NotNull`

Means the value must exist.

This is invalid:

```json
{
  "fuelType": "REGULAR"
}
```

because `price` is missing.

This is also invalid:

```json
{
  "price": 2.79
}
```

because `fuelType` is missing.

## `@Positive`

Means the numeric value must be greater than zero.

Invalid:

```json
{
  "price": -2.79,
  "fuelType": "REGULAR"
}
```

Invalid:

```json
{
  "price": 0,
  "fuelType": "REGULAR"
}
```

Valid:

```json
{
  "price": 2.79,
  "fuelType": "REGULAR"
}
```

---

# 85. @Valid

Adding validation annotations to a DTO defines the rules, but Spring must also be told to run those rules.

The POST controller now uses:

```java
@Valid @RequestBody FuelPriceRequest request
```

The complete flow is:

```text
Incoming JSON
      ↓
@RequestBody
      ↓
FuelPriceRequest
      ↓
@Valid
      ↓
@NotNull / @Positive
      ↓
Is request valid?
   /        \
 YES        NO
  ↓          ↓
Service    400 error
  ↓
Database
```

An invalid request is rejected **before the service modifies the database**.

---

# 86. Validation Testing

We deliberately tested invalid requests.

## Negative Price

```json
{
  "price": -2.95,
  "fuelType": "REGULAR"
}
```

Result:

```text
400 Bad Request
```

## Missing Price

```json
{
  "fuelType": "REGULAR"
}
```

Result:

```text
400 Bad Request
```

## Missing Fuel Type

```json
{
  "price": 2.95
}
```

Result:

```text
400 Bad Request
```

The existing database price remained unchanged.

This proved:

```text
invalid request
      ↓
validation fails
      ↓
request rejected
      ↓
service does not save
      ↓
database remains unchanged
```

---

# 87. Why Validation Helps the Frontend

Validation gives the frontend clear rules about what data the API accepts.

For example, React will eventually be able to submit:

```json
{
  "price": -3.00,
  "fuelType": "REGULAR"
}
```

and receive a predictable error instead of allowing invalid information into the database.

The frontend can then display something like:

```text
Price must be greater than 0
```

to the user.

Validation therefore protects both:

```text
DATABASE DATA
```

and:

```text
USER EXPERIENCE
```

---

# 88. API Error Response DTO

FuelFinder now has:

```text
ApiErrorResponse
```

to give API errors a consistent JSON format.

It contains:

```text
status
error
message
timestamp
```

Example:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Price must be greater than 0",
  "timestamp": "2026-08-13T22:05:53.6645949"
}
```

This gives the frontend a predictable API contract.

Successful fuel-price responses use:

```text
FuelPriceResponse
```

while failed requests can use:

```text
ApiErrorResponse
```

---

# 89. Why Consistent Error JSON Matters

Without a consistent response, the frontend might receive different structures depending on which backend error occurs.

With `ApiErrorResponse`, React can eventually do something conceptually like:

```javascript
if (!response.ok) {
    const error = await response.json();
    setError(error.message);
}
```

The frontend only needs to care about:

```text
error.message
```

It does not need to understand:

```text
Java exception classes
Spring internals
Hibernate exceptions
stack traces
```

This creates a cleaner separation between frontend and backend.

---

# 90. Global Exception Handling

Instead of placing error-handling code inside every controller, FuelFinder now has:

```text
exception/
└── GlobalExceptionHandler.java
```

The class uses:

```java
@RestControllerAdvice
```

`@RestControllerAdvice` allows exception-handling rules to apply across REST controllers.

Conceptually:

```text
StationController ───────┐
                         │
FuelPriceController ─────┼──→ GlobalExceptionHandler
                         │
Future Controllers ──────┘
```

This centralizes API error handling.

---

# 91. @ExceptionHandler

Inside `GlobalExceptionHandler`, methods use:

```java
@ExceptionHandler(...)
```

to specify which Java exception they handle.

Example:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
```

means:

> When this validation exception occurs, use this method to build the HTTP response.

This keeps controllers focused on normal request handling rather than filling them with repeated `try/catch` logic.

---

# 92. Validation Exception Handling

When `@Valid` rejects a request body, Spring produces:

```text
MethodArgumentNotValidException
```

The global handler catches it.

Conceptually:

```text
Invalid FuelPriceRequest
        ↓
@Valid fails
        ↓
MethodArgumentNotValidException
        ↓
GlobalExceptionHandler
        ↓
extract validation message
        ↓
ApiErrorResponse
        ↓
400 Bad Request
```

The handler extracts a message using the validation errors:

```java
String message = exception
        .getBindingResult()
        .getFieldErrors()
        .get(0)
        .getDefaultMessage();
```

For:

```java
@Positive(message = "Price must be greater than 0")
```

the response message becomes:

```text
Price must be greater than 0
```

For now, FuelFinder returns the first validation error.

---

# 93. ResponseEntity

The exception handler uses:

```java
ResponseEntity<ApiErrorResponse>
```

`ResponseEntity` allows the backend to control both:

```text
HTTP status
+
response body
```

Example:

```java
return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errorResponse);
```

This produces:

```text
HTTP Status:
400 Bad Request
```

along with:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "timestamp": "..."
}
```

---

# 94. Custom StationNotFoundException

Previously, the service used:

```java
.orElseThrow(() -> new RuntimeException("Station not found"));
```

That was too generic.

FuelFinder now has a custom exception:

```text
StationNotFoundException
```

Example implementation:

```java
public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(Long stationId) {
        super("Station with id " + stationId + " was not found");
    }
}
```

The service now uses:

```java
Station station = stationRepository.findById(stationId)
        .orElseThrow(
            () -> new StationNotFoundException(stationId)
        );
```

This gives the error a specific meaning.

---

# 95. Why Create Custom Exceptions?

A generic:

```text
RuntimeException
```

does not explain what kind of application error occurred.

A custom:

```text
StationNotFoundException
```

does.

That allows the global handler to distinguish:

```text
Validation problem
→ 400 Bad Request
```

from:

```text
Station does not exist
→ 404 Not Found
```

This produces more accurate HTTP behavior.

---

# 96. 400 vs 404

FuelFinder now uses different HTTP status codes for different categories of problems.

## 400 Bad Request

Means:

> The client sent invalid input.

Examples:

```text
negative price
missing price
missing fuel type
invalid fuel type
```

## 404 Not Found

Means:

> The requested resource does not exist.

Example:

```text
POST /api/stations/999/prices
```

when station `999` does not exist.

A clean response can look like:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Station with id 999 was not found",
  "timestamp": "..."
}
```

---

# 97. Station Not Found Flow

The flow is now:

```text
POST /api/stations/999/prices
        ↓
FuelPriceController
        ↓
FuelPriceService
        ↓
stationRepository.findById(999)
        ↓
Optional.empty
        ↓
StationNotFoundException
        ↓
GlobalExceptionHandler
        ↓
ApiErrorResponse
        ↓
404 Not Found
```

This is much better than exposing a generic backend exception.

---

# 98. Invalid Enum Input

`FuelType` is an enum:

```java
REGULAR
MIDGRADE
PREMIUM
DIESEL
```

A request such as:

```json
{
  "price": 2.95,
  "fuelType": "PIZZA"
}
```

has a different problem from:

```json
{
  "price": -2.95,
  "fuelType": "REGULAR"
}
```

The negative price successfully converts into a `FuelPriceRequest`, and then validation fails.

But `"PIZZA"` cannot be converted into:

```java
FuelType
```

at all.

Conceptually:

```text
Negative price:

JSON
 ↓
FuelPriceRequest created
 ↓
@Valid
 ↓
fails


Invalid enum:

JSON
 ↓
Jackson attempts conversion
 ↓
"PIZZA" → FuelType
 ↓
conversion fails
 ↓
FuelPriceRequest cannot be fully created
```

---

# 99. HttpMessageNotReadableException

When Jackson cannot convert the request body into the required Java types, Spring can produce:

```text
HttpMessageNotReadableException
```

The global handler now contains a handler for this error.

Example response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request body. Check that the fuel type is valid.",
  "timestamp": "..."
}
```

This gives the frontend a clean error instead of exposing Jackson/Spring internals.

---

# 100. Current Error Handling Mental Model

FuelFinder now distinguishes several failure paths:

```text
REQUEST
   ↓
Can JSON be converted?
   │
   ├── NO
   │    ↓
   │ HttpMessageNotReadableException
   │    ↓
   │ 400 Bad Request
   │
   └── YES
        ↓
      @Valid
        ↓
Are DTO fields valid?
   │
   ├── NO
   │    ↓
   │ MethodArgumentNotValidException
   │    ↓
   │ 400 Bad Request
   │
   └── YES
        ↓
      Service
        ↓
Does station exist?
   │
   ├── NO
   │    ↓
   │ StationNotFoundException
   │    ↓
   │ 404 Not Found
   │
   └── YES
        ↓
      Business logic
        ↓
      Database
```

---

# 101. Why Global Error Handling Helps React

When the frontend is built, React will need to respond differently depending on what happened.

For example:

```text
400
→ show user that their form input is invalid

404
→ explain that a requested station could not be found

200
→ display successful result
```

Because the backend returns predictable JSON:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Price must be greater than 0",
  "timestamp": "..."
}
```

React will be able to easily display:

```text
Price must be greater than 0
```

This is an example of designing the backend API to make frontend development easier.

---

# 102. New Important Spring / Jakarta Annotations

## `@Valid`

Runs validation rules defined on an incoming object.

Example:

```java
@Valid @RequestBody FuelPriceRequest request
```

---

## `@NotNull`

Requires a field to contain a value.

Example:

```java
@NotNull(message = "Price is required")
```

---

## `@Positive`

Requires a numeric value to be greater than zero.

Example:

```java
@Positive(message = "Price must be greater than 0")
```

---

## `@RestControllerAdvice`

Defines centralized exception-handling behavior for REST controllers.

---

## `@ExceptionHandler`

Specifies which exception a handler method responds to.

Example:

```java
@ExceptionHandler(StationNotFoundException.class)
```

---

# 103. New Interview Concepts to Know

I should be able to explain:

## Validation

* What is input validation?
* Why should the backend validate data even if the frontend also validates it?
* What does `@Valid` do?
* What does `@NotNull` do?
* What does `@Positive` do?
* At what point does validation happen?
* Does invalid input reach the service/database?

## Error Handling

* What is exception handling?
* Why create custom exceptions?
* What is `@RestControllerAdvice`?
* What is `@ExceptionHandler`?
* Why centralize error handling?
* What is `ResponseEntity`?
* What is the difference between a `400` and a `404`?
* Why shouldn't stack traces or internal exception objects be exposed to the frontend?

## Data Integrity

* What is a unique constraint?
* What is a composite unique constraint?
* Why is `(station_id, fuel_type)` unique?
* Why enforce rules in the database if application logic already checks them?

## JPA

* How does `save()` know whether to insert or update?
* What happens when an entity already has an existing database identity?
* Why does `findByStation_IdAndFuelType()` return `Optional<FuelPrice>`?

---

# 104. Updated Current Project Status

## Completed

### Environment / Setup

* Java 21
* Spring Boot
* Maven Wrapper
* MySQL
* MySQL Workbench
* Spring Boot → MySQL connection
* Environment-variable database credentials
* Git / GitHub project tracking

### Architecture

* Controller layer
* Service layer
* Repository layer
* JPA / Hibernate layer
* MySQL persistence
* Dependency Injection
* DTO-based fuel-price API

### Station Features

* `Station` entity
* `StationRepository`
* `StationService`
* `StationController`
* `GET /api/stations`
* `POST /api/stations`
* ZIP-code filtering
* `GET /api/stations?zipCode=...`

### Fuel Price Features

* `FuelType` enum
* `FuelPrice` entity
* Station → FuelPrice relationship
* Foreign key
* Fuel-price timestamps
* `FuelPriceRequest`
* `FuelPriceResponse`
* `GET /api/fuel-prices`
* `GET /api/fuel-prices?fuelType=...`
* `GET /api/stations/{stationId}/prices`
* `POST /api/stations/{stationId}/prices`
* Cheapest-price search
* ZIP + fuel-type cheapest search
* Tie handling
* Custom JPQL
* `MIN()`
* Entity-to-DTO mapping
* Java Stream mapping

### Latest-Price Logic

* Latest-price-only MVP decision
* Lookup by station + fuel type
* Existing price UPDATE behavior
* New combination INSERT behavior
* `findByStation_IdAndFuelType()`
* Composite unique `(station_id, fuel_type)` constraint
* Manual testing of both UPDATE and INSERT paths

### Validation

* Spring Boot validation dependency
* `@Valid`
* `@NotNull`
* `@Positive`
* Price-required validation
* Positive-price validation
* Fuel-type-required validation
* Manual `400 Bad Request` testing
* Verified invalid data does not modify database data

### Error Handling

* `ApiErrorResponse`
* `GlobalExceptionHandler`
* `@RestControllerAdvice`
* `@ExceptionHandler`
* Validation exception handling
* Clean `400 Bad Request` JSON
* `StationNotFoundException`
* Clean `404 Not Found` handling
* Invalid request-body / enum exception handling
* `ResponseEntity`
* Consistent API error structure

---

# 105. Where I Stopped

The backend is now at a strong pre-frontend checkpoint.

The last area worked on was:

```text
validation
+
global exception handling
```

The invalid enum/request-body handler has been implemented.

At the next session, first manually confirm the invalid enum test if it has not already been tested:

```json
{
  "price": 2.95,
  "fuelType": "PIZZA"
}
```

Expected:

```text
400 Bad Request
```

with a clean `ApiErrorResponse`.

---

# 106. Next Steps Before Frontend

The goal is to stop immediately before React.

## First

1. Confirm invalid-enum error handling.
2. Consider basic validation for `Station` input.
3. Consider ZIP-code format validation.
4. Review any remaining generic exceptions.
5. Make sure API error responses are consistent.

## Automated Testing

6. Add JUnit tests.
7. Learn Mockito.
8. Test `FuelPriceService`.
9. Test creation of a new fuel price.
10. Test updating an existing fuel price.
11. Test invalid station IDs.
12. Test fuel-price filtering.
13. Test ZIP filtering.
14. Test cheapest-price behavior.
15. Test tied cheapest prices.
16. Test validation failures.
17. Add controller/API tests.

## Final Backend Cleanup

18. Remove unused imports.
19. Review comments.
20. Review package structure.
21. Confirm database constraints.
22. Confirm no secrets are tracked by Git.
23. Review all API endpoints.
24. Update README/documentation.
25. Commit and push the completed backend checkpoint.

Then stop.

The next untouched phase will be:

```text
REACT FRONTEND
```

---

# 107. Backend MVP Finish Line

The backend will be considered ready to pause once it has:

```text
Spring Boot REST API
        ↓
Controller
        ↓
Request DTOs
        ↓
Validation
        ↓
Service
        ↓
Business logic
        ↓
Repository
        ↓
JPA / Hibernate
        ↓
MySQL

PLUS

Response DTOs
Custom JPQL
Latest-price rules
Database constraints
Global error handling
Correct HTTP status codes
Automated tests
Documentation
```

At that point, FuelFinder will demonstrate much more than simple CRUD.

It will demonstrate:

```text
API design
layered architecture
business rules
data integrity
validation
exception handling
HTTP semantics
database relationships
custom querying
DTO architecture
testing
```

That is the point where the backend can be considered a strong learning/interview-ready MVP before beginning React.

---

# 108. Updated Full POST Mental Model

A successful fuel-price submission now follows:

```text
CLIENT
PowerShell / future React
        ↓
HTTP POST + JSON
        ↓
CONTROLLER
        ↓
Jackson converts JSON
        ↓
FuelPriceRequest
        ↓
@Valid
        ↓
Validation passes
        ↓
SERVICE
        ↓
Find Station
        ↓
Find existing
station + fuelType
        ↓
Exists?
   /         \
 YES         NO
  ↓           ↓
update       create
existing     FuelPrice
  \           /
       ↓
set latest price
set lastUpdated
       ↓
REPOSITORY.save()
       ↓
Hibernate
       ↓
UPDATE or INSERT
       ↓
MySQL
       ↓
FuelPrice entity
       ↓
toResponse()
       ↓
FuelPriceResponse
       ↓
Jackson
       ↓
JSON
       ↓
CLIENT
```

A failed submission can branch earlier:

```text
Bad JSON / invalid enum
        ↓
400

Invalid DTO fields
        ↓
400

Station missing
        ↓
404
```

This is the most complete FuelFinder backend mental model so far.
