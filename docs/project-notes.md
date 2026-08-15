FuelFinder Project Documentation


## Project Goal

FuelFinder is a full-stack web application designed to help drivers find nearby gas stations, compare fuel prices, and eventually determine whether driving farther for cheaper gas actually saves money.

The project is also being used as a hands-on software engineering learning project. Instead of only studying Java, Spring Boot, SQL, APIs, testing, and frontend development individually, FuelFinder applies those concepts together inside a real application.

---

# Tech Stack

## Backend

- Java 21

- Spring Boot

- Spring MVC

- Spring Data JPA

- Hibernate

- Maven

## Database

- MySQL

- MySQL Workbench

## Testing

- JUnit 5

- Mockito

- Spring Boot Test

## Frontend

- React planned for the next phase

## Development Tools

- Visual Studio Code

- PowerShell

- Git

- GitHub

---

# Current Project Architecture

FuelFinder uses a layered backend architecture:

Client

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

Eventually the client will be the React frontend.

For now, requests can come from:

- Browser

- PowerShell

- API testing tools

- Future React frontend

---

# Why Use Layers?

Each layer has a separate responsibility.

## Controller

The Controller handles HTTP requests.

Even though this interface contains almost no implementation code, methods such as these work:


stationRepository.findAll();


stationRepository.findById(id);

stationRepository.save(station);

This happens because Spring Data JPA dynamically creates the implementation at runtime.

---

# Important Spring Data JPA Behind-the-Scenes Behavior

One of the most important concepts in this project is that Spring performs a large amount of work automatically.

## Repository Interfaces

When we write:


public interface StationRepository


extends JpaRepository\<Station, Long> {

}

we are only defining an interface.

We are NOT manually creating a class such as:


StationRepositoryImpl


Spring Data JPA automatically generates the repository implementation when the application starts.

That is why we can immediately use methods like:


findAll()


findById()

save()

deleteById()

without implementing them ourselves.

Conceptually:

StationRepository interface
 ↓
 Spring Data JPA
 ↓
 Generated implementation
 ↓
 Hibernate
 ↓
 SQL
 ↓
 MySQL

---

# Hibernate and Entity-to-Table Mapping

Java classes marked with:


@Entity


represent database entities.

For example:


@Entity


@Table(name = "stations")

public class Station {

}

tells Hibernate that the Station class corresponds to the stations database table.

Because application.properties contains:


spring.jpa.hibernate.ddl-auto=update


Hibernate compares the Java entities with the database schema when Spring Boot starts.

If required database structures are missing, Hibernate can create or update them.

Conceptually:

Station.java
 ↓
 @Entity
 ↓
 Hibernate / JPA
 ↓
 stations table in MySQL

This is why the stations table was automatically created when the application started on the new development machine.

---

# Database Configuration

FuelFinder connects to MySQL through:

src/main/resources/application.properties

Example configuration:


spring.datasource.url=jdbc:mysql://localhost:3306/fuel_finder


spring.datasource.username=root

spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true

spring.jpa.properties.hibernate.format_sql=true

The actual MySQL password is NOT stored inside the Git repository.

Instead:


${DB_PASSWORD}


references a Windows environment variable.

This prevents database credentials from accidentally being committed to GitHub.

---

# Maven Wrapper

FuelFinder includes the Maven Wrapper.

Instead of requiring every developer to manually install exactly the same Maven version, the project can use:


.\mvnw.cmd


Example:


.\mvnw.cmd spring-boot


and:


.\mvnw.cmd test


---

# Running the Backend

From the backend directory:


cd backend


.\mvnw\.cmd spring-boot:run

The database password is already stored as a persistent environment variable on the current development machine.

A successful startup includes messages similar to:


Tomcat started on port 8080


Started BackendApplication

The API is then available at:

[http://localhost:8080](http://localhost:8080)

---

# Health Check Endpoint

Endpoint:


GET /api/health


Purpose:

Confirm that the Spring Boot application is running.

Important concepts:

-  @RestController 
-  @GetMapping 
-  HTTP GET 
-  API response 
-  localhost 
-  port 8080 

---

# Station Model

The Station entity represents a gas station.

Current fields:


id


name

address

city

state

zipCode

latitude

longitude

Example concept:


@Entity


@Table(name = "stations")

public class Station {

}

The ID is generated automatically.

Example:


@Id


@GeneratedValue(strategy = GenerationType.IDENTITY)

private Long id;

MySQL handles the auto-incrementing ID.

---

# Why ZIP Code Is a String

ZIP codes are stored as:


String zipCode;


instead of:


int zipCode;


because ZIP codes are identifiers, not mathematical values.

For example:


00501


is a valid ZIP code.

If it were stored as an integer, the leading zeros could disappear.

---

# Station Request DTO

FuelFinder does not allow clients to directly send a Station entity anymore.

Instead, incoming station requests use:


StationRequest


Fields:


name


address

city

state

zipCode

latitude

longitude

The ID is intentionally excluded.

The client should not control the database ID.

Example request:


{


  "name": "Valero",

  "address": "500 Center St",

  "city": "Arlington",

  "state": "TX",

  "zipCode": "76010",

  "latitude": 32.7357,

  "longitude": -97.1081

}

---

# Station Response DTO

Outgoing Station API responses use:


StationResponse


Fields:


id


name

address

city

state

zipCode

latitude

longitude

Example:


{


  "id": 5,

  "name": "Valero",

  "address": "500 Center St",

  "city": "Arlington",

  "state": "TX",

  "zipCode": "76010",

  "latitude": 32.7357,

  "longitude": -97.1081

}

---

# Why Use DTOs?

DTO stands for:

Data Transfer Object

DTOs separate API data from database entities.

Without DTOs:

Client
 ↓
 Station entity
 ↓
 Database

With DTOs:

Client
 ↓
 StationRequest
 ↓
 Service
 ↓
 Station entity
 ↓
 Database
 ↓
 StationResponse
 ↓
 Client

Benefits:

-  Prevents exposing database entities directly 
-  Controls which fields clients may send 
-  Controls which fields clients receive 
-  Makes validation easier 
-  Makes API responses easier for the frontend to use 
-  Reduces coupling between the database and API 

---

# Station Validation

StationRequest uses Jakarta Bean Validation.

Examples:


@NotBlank(message = "Station name is required")


private String name;


@Pattern(


regexp = "\\\d{5}",

message = "ZIP code must contain exactly 5 digits"

)

private String zipCode;

The Controller uses:


@Valid


Example:


public StationResponse createStation(


        @Valid @RequestBody StationRequest request)

Validation occurs before the Service runs.

Example invalid request:


{


  "name": "Valero",

  "address": "500 Center St",

  "city": "Arlington",

  "state": "TX",

  "zipCode": "760"

}

Result:


{


  "status": 400,

  "error": "Bad Request",

  "message": "ZIP code must contain exactly 5 digits",

  "timestamp": "..."

}

This behavior was manually tested successfully.

---

# Station Endpoints

## Get All Stations


GET /api/stations


Returns all stations.

---

## Filter Stations by ZIP Code


GET /api/stations?zipCode=76010


Uses:


@RequestParam(required = false)


If zipCode exists:


Controller


↓

getStationsByZipCode()

Otherwise:


Controller


↓

getAllStations()

---

## Create Station


POST /api/stations


Incoming JSON becomes:


StationRequest


The Service creates a Station entity and saves it.

The API then returns:


StationResponse


---

# Spring Data Derived Query Methods

Spring Data JPA can create queries based on repository method names.

Example:


List<Station> findByZipCode(String zipCode);


Spring interprets:


findBy


\+

ZipCode

and generates the required query.

We do not manually write the SQL.

---

# FuelType Enum

FuelFinder uses an enum to represent supported fuel types.


public enum FuelType {


REGULAR,

MIDGRADE,

PREMIUM,

DIESEL

}

Using an enum provides type safety.

Instead of allowing arbitrary strings such as:


"regular"


"reg"

"normal"

"pizza"

the application only accepts defined values.

---

# FuelPrice Model

FuelPrice represents the current fuel price for one station and one fuel type.

Fields:


id


price

fuelType

station

lastUpdated

---

# Station and FuelPrice Relationship

A station can have multiple fuel types.

Example:

Shell
 ├── REGULAR
 ├── MIDGRADE
 ├── PREMIUM
 └── DIESEL

Therefore:


@ManyToOne


@JoinColumn(name = "station_id")

private Station station;

is used inside FuelPrice.

Many FuelPrice records can belong to one Station.

In Java:


FuelPrice.station


In MySQL:


fuel_prices.station_id


The foreign key references:


stations.id


---

# Foreign Keys

A foreign key connects records between tables.

Example:

fuel_prices


station_id = 1


means that FuelPrice belongs to the Station whose:


stations.id = 1


This creates a relational connection between the two tables.

---

# FuelPrice Request DTO

Incoming fuel-price requests use:


FuelPriceRequest


Fields:


price


fuelType

Example:


{


  "price": 2.95,

  "fuelType": "REGULAR"

}

The station ID is part of the URL:


POST /api/stations/1/prices


Therefore it does not need to be duplicated inside the JSON.

---

# FuelPrice Validation

FuelPriceRequest contains validation rules.

Example:


@NotNull(message = "Price is required")


@Positive(message = "Price must be greater than 0")

private Double price;

and:


@NotNull(message = "Fuel type is required")


private FuelType fuelType;

Examples rejected by the API:


{


  "price": -2.95,

  "fuelType": "REGULAR"

}

Missing price:


{


  "fuelType": "REGULAR"

}

Missing fuel type:


{


  "price": 2.95

}

---

# FuelPrice Response DTO

FuelPriceResponse returns flattened data.

Instead of returning:


{


  "station": {

    ...

  }

}

the response contains fields such as:


stationId


stationName

address

city

state

zipCode

price

fuelType

lastUpdated

Example:


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

This makes the future React frontend easier to build.

---

# Entity-to-DTO Conversion

FuelPriceService contains a helper method similar to:


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

This converts:


FuelPrice entity


into:


FuelPriceResponse DTO


---

# Java Streams in FuelFinder

The project uses Java Streams to convert collections.

Example:


return fuelPriceRepository.findAll()


.stream()

.map(this::toResponse)

.toList();

Step by step:


findAll()


↓

List\<FuelPrice>

stream()

↓

process elements

map(this::toResponse)

↓

convert every FuelPrice to FuelPriceResponse

toList()

↓

List\<FuelPriceResponse>

This:


.map(this::toResponse)


is equivalent to:


.map(fuelPrice -> toResponse(fuelPrice))


---

# Creating a Fuel Price

Endpoint:


POST /api/stations/{stationId}/prices


Example:


POST /api/stations/1/prices


Body:


{


  "price": 2.95,

  "fuelType": "REGULAR"

}

---

# Full POST Request Flow

This PowerShell example was used during testing:


$body = @{


price = 2.95

fuelType = "REGULAR"

} | ConvertTo-Json

Invoke-RestMethod \`

-Uri "http://localhost:8080/api/stations/1/prices" \`

-Method Post \`

-ContentType "application/json" \`

-Body $body

Step by step:

## 1. PowerShell Hashtable


$body = @{


price = 2.95

fuelType = "REGULAR"

}

creates a PowerShell object.

## 2. ConvertTo-Json


| ConvertTo-Json


turns the PowerShell object into JSON.

## 3. Invoke-RestMethod

PowerShell sends an HTTP POST request.


POST /api/stations/1/prices


Content-Type:


application/json


## 4. @RequestBody

Spring receives the JSON.


@RequestBody FuelPriceRequest request


Spring/Jackson converts JSON into a Java FuelPriceRequest object.

## 5. Controller

The Controller receives:


stationId


FuelPriceRequest

and calls the Service.

## 6. Service

The Service applies application logic.

## 7. Repository

The Service calls:


fuelPriceRepository.save(...)


## 8. Hibernate / JPA

Hibernate translates the operation into SQL.

## 9. MySQL

The data is inserted or updated.

The complete flow is:

PowerShell
 ↓
 JSON
 ↓
 HTTP POST
 ↓
 Controller
 ↓
 Service
 ↓
 Repository.save()
 ↓
 Hibernate/JPA
 ↓
 SQL
 ↓
 MySQL

This POST test proved that information could travel from a client into the database.

The GET tests proved that information could travel back out of the database.

Together:

Client
 ⇄
 Backend
 ⇄
 Database

---

# Latest-Price Business Rule

For the current MVP, FuelFinder stores only the latest known price for each:


station + fuel type


Example:

Shell + REGULAR

should only have one current record.

If Shell REGULAR already exists:


POST new Shell REGULAR price


↓

update existing record

If Shell DIESEL does not exist:


POST Shell DIESEL


↓

create new record

---

# Optional and Latest-Price Logic

Repository method:


Optional<FuelPrice> findByStation_IdAndFuelType(


Long stationId,

FuelType fuelType);

Optional means:


the value may exist


or

the value may not exist

Service logic:


FuelPrice fuelPrice = fuelPriceRepository


.findByStation_IdAndFuelType(

stationId,

request.getFuelType()

        )

.orElse(new FuelPrice());

If a record exists:


Optional contains FuelPrice


↓

reuse existing object

↓

update it

If no record exists:


Optional.empty()


↓

new FuelPrice()

↓

insert it

---

# How save() Knows INSERT vs UPDATE

JPA entities have IDs.

New object:


id = null


When saved:


INSERT


Existing database entity:


id = existing value


When saved:


UPDATE


This allows the same:


repository.save(entity)


method to support both operations.

---

# Database Composite Unique Constraint

FuelFinder also protects the latest-price rule at the database level.

The combination:


station_id + fuel_type


must be unique.

Conceptually:


UNIQUE (station_id, fuel_type)


This does NOT mean station_id must be unique by itself.

A station can still have:


Station 1 + REGULAR


Station 1 + MIDGRADE

Station 1 + PREMIUM

But it cannot have:


Station 1 + REGULAR


Station 1 + REGULAR

twice.

This provides two protection layers:

Application logic
 \+
 Database constraint

---

# Fuel Price Filtering

Endpoint:


GET /api/fuel-prices?fuelType=REGULAR


returns prices matching the requested fuel type.

Spring converts:


REGULAR


into:


FuelType.REGULAR


---

# Sorted Fuel Search by Fuel Type and ZIP

FuelFinder now supports:


GET /api/fuel-prices?fuelType=REGULAR&zipCode=76010


This returns all matching prices in that ZIP code sorted from cheapest to most expensive.

Repository method:


List<FuelPrice>


findByFuelTypeAndStation_ZipCodeOrderByPriceAsc(

FuelType fuelType,

String zipCode

);

Spring interprets the method name.

Conceptually:


findBy


FuelType

AND

Station.ZipCode

OrderBy

Price

Ascending

Example result:


Shell       $2.79


Chevron     $3.05

instead of:


Chevron     $3.05


Shell       $2.79

This endpoint will be especially useful for the React search-results page.

---

# Cheapest Fuel Price

Endpoint:


GET /api/fuel-prices/cheapest?fuelType=REGULAR


returns the cheapest price for a fuel type.

However, there can be ties.

Example:


Shell      $2.79


QuikTrip   $2.79

Both should be returned.

Therefore the API returns:


List<FuelPriceResponse>


instead of only one FuelPriceResponse.

---

# Custom JPQL Query

The cheapest-price feature uses a custom JPQL query.

Example:


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

JPQL works with Java entities and fields.

It uses:


FuelPrice


fuelType

price

instead of database names such as:


fuel_prices


fuel_type

---

# JPQL Aliases

In:


SELECT fp


FROM FuelPrice fp

fp is simply an alias.

It could technically be named something else.

Likewise:


fp2


is another alias used inside the subquery.

---

# MIN()

The subquery:


SELECT MIN(fp2.price)


finds the lowest matching price.

Example:


2.95


2.79

3.05

MIN returns:


2.79


The outer query then returns every FuelPrice whose price equals:


2.79


This is why tied stations are supported.

---

# Cheapest Price by ZIP Code

Endpoint:


GET /api/fuel-prices/cheapest


    ?fuelType=REGULAR

    &zipCode=76010

The query additionally checks:


fp.station.zipCode


JPQL can navigate Java object relationships.

Conceptually:


FuelPrice


↓

station

↓

zipCode

Hibernate converts that relationship into the appropriate SQL JOIN.

---

# @PathVariable vs @RequestParam

FuelFinder uses both.

## @PathVariable

Used when a value is part of the URL path.

Example:


/api/stations/1/prices



@PathVariable Long stationId


stationId becomes:


1


---

## @RequestParam

Used for filters or options after:


?


Example:


/api/fuel-prices?fuelType=REGULAR



@RequestParam FuelType fuelType


Another example:


/api/fuel-prices?fuelType=REGULAR&zipCode=76010


---

# Optional Request Parameters

Example:


@RequestParam(required = false)


means the client is not required to provide that query parameter.

This allows one endpoint to support:


/api/fuel-prices


and:


/api/fuel-prices?fuelType=REGULAR


and:


/api/fuel-prices?fuelType=REGULAR&zipCode=76010


---

# Error Handling

FuelFinder uses centralized error handling.

Class:


GlobalExceptionHandler


Annotation:


@RestControllerAdvice


This allows one class to handle exceptions from multiple controllers.

---

# ApiErrorResponse

Errors use a consistent structure.

Fields:


status


error

message

timestamp

Example:


{


  "status": 400,

  "error": "Bad Request",

  "message": "Price must be greater than 0",

  "timestamp": "..."

}

---

# Validation Errors

When:


@Valid


fails, Spring throws:


MethodArgumentNotValidException


GlobalExceptionHandler catches the exception.

Then it creates:


ApiErrorResponse


and returns:


HTTP 400 Bad Request


---

# StationNotFoundException

FuelFinder defines a custom exception:


StationNotFoundException


Example:


throw new StationNotFoundException(stationId);


The GlobalExceptionHandler converts that into:


HTTP 404 Not Found


Example message:


Station with id 999 was not found


---

# Why 400 and 404 Are Different

400 Bad Request means:


The client sent invalid data.


Examples:


negative price


missing price

invalid ZIP

invalid fuel type

404 Not Found means:


The requested resource does not exist.


Example:


station ID 999 does not exist


---

# Invalid Enum Handling

A request such as:


{


  "price": 2.95,

  "fuelType": "PIZZA"

}

cannot be converted into:


FuelType


because PIZZA is not a valid enum constant.

This fails during JSON deserialization before normal @Valid validation.

Spring throws:


HttpMessageNotReadableException


FuelFinder handles this globally and returns a clean 400 response.

Example:


{


  "status": 400,

  "error": "Bad Request",

  "message": "Invalid request body. Check that the fuel type is valid.",

  "timestamp": "..."

}

This behavior was manually tested successfully.

---

# Current FuelPrice Endpoints

## Get All Fuel Prices


GET /api/fuel-prices


---

## Filter by Fuel Type


GET /api/fuel-prices?fuelType=REGULAR


---

## Filter by Fuel Type and ZIP


GET /api/fuel-prices?fuelType=REGULAR&zipCode=76010


Returns matching prices sorted cheapest to most expensive.

---

## Get Prices for One Station


GET /api/stations/{stationId}/prices


Example:


GET /api/stations/1/prices


---

## Create or Update Fuel Price


POST /api/stations/{stationId}/prices


---

## Cheapest Price by Fuel Type


GET /api/fuel-prices/cheapest?fuelType=REGULAR


---

## Cheapest Price by Fuel Type and ZIP


GET /api/fuel-prices/cheapest?fuelType=REGULAR&zipCode=76010


---

# Automated Testing

FuelFinder now uses:


JUnit 5


Mockito

Spring Boot Test

Command:


.\mvnw.cmd test


Current automated test result:


Tests run: 6


Failures: 0

Errors: 0

BUILD SUCCESS

---

# BackendApplicationTests

The default Spring test uses:


@SpringBootTest


and:


@Test


void contextLoads() {

}

This is a smoke test.

It checks whether the Spring application context can start successfully.

---

# FuelPriceService Unit Tests

FuelPriceServiceTest uses Mockito.

Example setup:


@ExtendWith(MockitoExtension.class)


class FuelPriceServiceTest {

    @Mock

private FuelPriceRepository fuelPriceRepository;

    @Mock

private StationRepository stationRepository;

    @InjectMocks

private FuelPriceService fuelPriceService;

}

---

# @Mock

@Mock creates fake dependencies.

Instead of:


FuelPriceService


↓

Real Repository

↓

MySQL

the unit test uses:


FuelPriceService


↓

Mock Repository

The test controls what the mock returns.

---

# @InjectMocks

@InjectMocks creates the real class being tested and gives it the mocked dependencies.

Therefore:


REAL FuelPriceService


↓

MOCK FuelPriceRepository

MOCK StationRepository

This isolates the business logic.

---

# Arrange, Act, Assert

The service tests follow:


ARRANGE


ACT

ASSERT

## Arrange

Prepare data and mock behavior.

## Act

Call the method being tested.

## Assert

Verify the result.

---

# Current Service Test Coverage

FuelFinder currently tests these important behaviors:


Create a new fuel price


Update an existing fuel price

Throw StationNotFoundException for missing station

Return all tied cheapest prices

Return cheapest prices for a ZIP code

Together with the Spring context test:


6 automated tests currently pass


---

# Testing INSERT Behavior

The test configures:


findByStation_IdAndFuelType(...)


to return:


Optional.empty()


This simulates:


price does not exist


The Service should create a new FuelPrice.

---

# Testing UPDATE Behavior

The mock instead returns:


Optional.of(existingFuelPrice)


This simulates:


price already exists


The Service updates the existing entity.

The test also verifies that the same object is passed to:


repository.save(...)


---

# Testing Exceptions

JUnit provides:


assertThrows(...)


Example concept:


station ID 999


↓

repository returns Optional.empty()

↓

Service throws StationNotFoundException

Mockito can also verify that:


repository.save(...)


was never called.

---

# verify()

Mockito's:


verify(...)


checks whether a dependency was called.

Example:


verify(fuelPriceRepository, times(1))


.save(existingFuelPrice);

means:


save() must have been called exactly once


---

# Manual API Testing

FuelFinder has also been tested manually using:

-  Browser GET requests 
-  PowerShell GET/POST requests 
-  MySQL Workbench queries 

This helps verify the full application rather than only isolated unit logic.

---

# Git and GitHub

The project is stored in Git.

Important files such as:


target/


.env

.vscode/

are ignored.

The real database password is not stored in the repository.

application.properties remains tracked because it contains configuration rather than the actual secret.

---

# Important Debugging Example: Ambiguous Controller Mapping

During development, Spring Boot failed to start with:


Ambiguous mapping


The problem was that two Controller methods both used:


@GetMapping("/api/fuel-prices")


Spring could not determine which method should handle the same URL.

The old method was removed and one Controller method now supports optional request parameters.

Lesson:

Java method overloading does not automatically create different HTTP routes.

Spring routes requests based on mappings such as:


HTTP method


URL path

mapping conditions

not simply the number of Java parameters.

---

# Current Backend Status

Completed:

-  Spring Boot setup 
-  MySQL connection 
-  JPA entities 
-  Station API 
-  FuelPrice API 
-  Controller / Service / Repository architecture 
-  Station-FuelPrice relationship 
-  FuelType enum 
-  Station filtering by ZIP 
-  Fuel-price filtering by fuel type 
-  Fuel-price filtering by fuel type + ZIP 
-  Sorted fuel-price results 
-  Cheapest-price search 
-  Cheapest-price tie handling 
-  Cheapest price by ZIP 
-  Station request/response DTOs 
-  FuelPrice request/response DTOs 
-  Latest-price update behavior 
-  Validation 
-  Custom error responses 
-  Custom exceptions 
-  Invalid enum handling 
-  Database uniqueness protection 
-  JUnit testing 
-  Mockito testing 
-  Manual API testing 

The backend MVP is essentially complete.

---

# Next Phase: React Frontend

The next major phase is building the React frontend.

Initial frontend goals:

1.  Search form 
2.  Enter ZIP code 
3.  Select fuel type 
4.  Call the Spring Boot API 
5.  Display matching stations 
6.  Sort/display prices from cheapest to most expensive 
7.  Show station information 
8.  Allow users to submit updated prices 

After the basic frontend works, later versions may introduce:

-  Real station/location APIs 
-  Maps 
-  Distance calculations 
-  User-submitted price freshness 
-  Personalized savings calculations 
-  Authentication 
-  Deployment 
-  Production database 
-  Real users 

---

# Long-Term FuelFinder Vision

The eventual goal is not only:


Which station has the cheapest gas?


but:


Is driving farther to that cheaper station actually worth it?


A future version could consider:


fuel price difference


distance

vehicle MPG

fuel required for the trip

estimated savings

Example:


Station A


$3.10

1 mile away

Station B

$2.95

8 miles away

The cheapest price does not automatically mean the cheapest overall trip.

That calculation can become one of FuelFinder's main distinguishing features.

---

# Key Concepts Applied in FuelFinder

FuelFinder currently demonstrates:

-  Java 
-  Object-oriented programming 
-  Spring Boot 
-  Spring MVC 
-  REST APIs 
-  HTTP GET and POST 
-  JSON 
-  MySQL 
-  Relational databases 
-  SQL 
-  Primary keys 
-  Foreign keys 
-  Database relationships 
-  Spring Data JPA 
-  Hibernate 
-  Repository pattern 
-  Service layer 
-  Controller layer 
-  Dependency injection 
-  DTOs 
-  Java enums 
-  Java Optional 
-  Java Streams 
-  Method references 
-  Bean Validation 
-  Exception handling 
-  Custom exceptions 
-  HTTP status codes 
-  JPQL 
-  Subqueries 
-  MIN() 
-  Derived queries 
-  Sorting 
-  Environment variables 
-  Git 
-  GitHub 
-  JUnit 
-  Mockito 
-  Unit testing 
-  Mocking 
-  Debugging 
-  Layered application architecture 



---