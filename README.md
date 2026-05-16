# Cinema Tickets Service

A Java 21 implementation of a cinema ticket purchasing service, built as part of the DWP Engineering recruitment exercise.

## Approach

The implementation follows SOLID principles with a clear separation of concerns across three classes:

- **`CinemaTicketsServiceImpl`** — orchestrates the purchase flow: validate, calculate, pay, reserve
- **`TicketRequestValidator`** — encapsulates all validation rules in one place
- **`TicketPriceCalculator`** — encapsulates all pricing and seat calculation logic

This separation makes each class independently testable and easy to reason about.

## Assumptions Made

- An account ID of zero or below is invalid, consistent with the stated rule that valid IDs are greater than zero
- A ticket request with a count of zero or below is rejected as it represents no meaningful purchase
- The number of infants cannot exceed the number of adults, since each infant must sit on an adult lap
- Infants are excluded from seat reservations entirely as they occupy no seat
- The `PaymentService` and `SeatReservationService` are treated as reliable external providers — no retry or error handling logic has been added, as per the assumptions in the brief
- The `CinemaTicketsController` has not been implemented as the brief explicitly states this is not required

## Business Rules Implemented

| Rule | Implementation |
|---|---|
| Max 25 tickets per purchase | Validated in `TicketRequestValidator` |
| INFANT tickets are free | `TicketPriceCalculator` applies £0 price |
| CHILD tickets cost £15 | `TicketPriceCalculator` applies £15 price |
| ADULT tickets cost £25 | `TicketPriceCalculator` applies £25 price |
| INFANT and CHILD require an ADULT | Validated in `TicketRequestValidator` |
| INFANTs get no seat | `TicketPriceCalculator` excludes INFANTs from seat count |
| INFANTs cannot exceed ADULTs | Validated in `TicketRequestValidator` |
| Account ID must be greater than zero | Validated in `TicketRequestValidator` |

## Tech Stack

- Java 21
- Spring Boot 4
- JUnit 5
- Mockito
- Cucumber (BDD)
- PIT Mutation Testing
- SpotBugs (Static Analysis)
- Docker
- GitHub Actions (CI)

## How to Build and Run

### Prerequisites
- Java 21
- Maven 3.9+

### Build
```bash
mvn compile
```

### Run all tests
```bash
mvn test
```

### Run SpotBugs static analysis
```bash
mvn spotbugs:check
```

### Run PIT mutation testing
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```
Mutation report is generated at `target/pit-reports/index.html`

### Build JAR
```bash
mvn package -DskipTests
```

### Build and run Docker image
```bash
docker build -t cinema-tickets .
docker run -p 8080:8080 cinema-tickets
```

## Test Coverage Summary

| Type | Tool | Result |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | All passing |
| BDD Scenarios | Cucumber | 8 scenarios, all passing |
| Line Coverage | PIT | 97% |
| Mutation Score | PIT | 93% |
| Static Analysis | SpotBugs | 0 bugs in application code |

## Project Structure

```
src/
├── main/java/uk/gov/dwp/engineering/recruitment/
│   ├── CinemaTicketsService.java          ← interface (not modified)
│   ├── CinemaTicketsServiceImpl.java      ← main implementation
│   ├── TicketRequestValidator.java        ← validation logic
│   ├── TicketPriceCalculator.java         ← pricing and seat logic
│   ├── domain/                            ← domain classes (not modified)
│   ├── exception/                         ← exception classes (not modified)
│   └── thirdparty/                        ← external services (not modified)
└── test/java/uk/gov/dwp/engineering/recruitment/
    ├── CinemaTicketsServiceImplTest.java  ← service integration tests
    ├── TicketRequestValidatorTest.java    ← validator unit tests
    ├── TicketPriceCalculatorTest.java     ← calculator unit tests
    └── bdd/
        ├── CucumberRunnerTest.java        ← Cucumber runner
        ├── TicketStepDefs.java            ← step definitions
        └── resources/features/
            └── ticket_purchasing.feature  ← BDD scenarios
```