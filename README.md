# Tazkara - Event Ticketing & Reservation Platform

**Tazkara** is a microservices-based event ticketing platform built with **Spring Boot 4**, **Spring Security (JWT)**, and **Docker**. It provides a complete event lifecycle: identity management, event creation, real-time ticket inventory with concurrency protection (zero overselling), booking holds with automatic expiration, and payment simulation.

---

## 🏛 Architecture Diagram

The system uses the **Database-per-Service** pattern for loose coupling and service independence.

![Architecture Diagram](https://lh3.googleusercontent.com/d/1Whf7iiDTUOCAPmtWSYk4m4zKKkhyVhmY)

---

## 🔌 Ports & Swagger Links

| Service | Port | Database (H2) | Swagger UI |
| :--- | :---: | :--- | :--- |
| **identity-service** | `8081` | `jdbc:h2:file:./data/identitydb` | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) |
| **event-service** | `8082` | `jdbc:h2:file:./data/eventdb` | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) |
| **booking-service** | `8083` | `jdbc:h2:file:./data/bookingdb` | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) |

---

## 🚀 How to Run

### Option 1: Docker Compose (Recommended)
```bash
docker compose up --build
```
*To run in the background, add `-d`. To stop: `docker compose down`.*

### Option 2: Run Locally (Maven)
Make sure you have **Java 17+** installed:
```bash
# Terminal 1: Identity Service
cd identity-service && ./mvnw clean spring-boot:run

# Terminal 2: Event Service
cd event-service && ./mvnw clean spring-boot:run

# Terminal 3: Booking Service
cd booking-service && ./mvnw clean spring-boot:run
```

---

## 👥 Demo Accounts & Main-Flow Walkthrough

### Pre-seeded Accounts
| Role | Username | Email | Password |
| :--- | :--- | :--- | :--- |
| **Organizer** | `organizer_demo` | `organizer@tazkara.com` | `Password123!` |
| **Attendee** | `attendee_demo` | `attendee@tazkara.com` | `Password123!` |

*(A sample event `"Tazkara Tech Summit 2026"` is pre-seeded and published automatically on startup).*

### Main-Flow Walkthrough

![Main-Flow Walkthrough](https://lh3.googleusercontent.com/d/1_lwB6RsQs56x0cr3wFFHxTUPwz8AQHMW)

1. **Authenticate**: Log in via `POST /api/auth/login` to receive a JWT token. Pass this token in the `Authorization: Bearer <token>` header for subsequent requests.
2. **Create & Publish Event (Organizer)**: Create an event draft (`POST /api/events`), add ticket types, and publish it (`PATCH /api/events/{id}/publish`).
3. **Browse Catalog (Attendee)**: Search public events with dynamic filters (`GET /api/events`).
4. **Reserve Tickets**: Reserve tickets (`POST /api/bookings/event/{id}`). If valid, a 10-minute hold is placed, inventory is decremented, and the booking is set to `PENDING_PAYMENT`.
5. **Pay & Confirm**: Pay within 10 minutes (`POST /api/payments/pay/{bookingId}`). The booking transitions to `CONFIRMED`.

---

## 💡 Design Decisions

### 1. Synchronous vs. Asynchronous Interactions
- **Synchronous (REST via `RestClient`)**:
  - Used for user-facing flows (login, event search, placing bookings, making payments).
  - Used for inter-service data consistency: `booking-service` synchronously reserves inventory in `event-service` during booking creation, and `event-service` calls `booking-service` when an event is cancelled to trigger refunds.
- **Asynchronous / Decoupled**:
  - **Background Expiry Task**: Overdue holds are cleaned up in the background without blocking users.
  - **Domain Events (Spring Events)**: Booking status updates (`CONFIRMED`, `CANCELLED`, `EXPIRED`) fire `BookingStatusChangedEvent` handled by decoupled listeners for audit logging and email notifications.

### 2. Inventory Cooperation (Event & Booking)
- `event-service` is the single source of truth for event data and ticket quantities.
- When an attendee books, `booking-service` calls `PUT /api/ticket-types/reserve` on `event-service`.
- `booking-service` captures a **price snapshot** at creation time. Any future price changes by the organizer do not affect existing bookings.
- If a booking expires or is cancelled, `booking-service` calls `PUT /api/ticket-types/release` to return tickets to inventory.

### 3. Overselling Prevention
- High concurrency can cause overselling if multiple requests read the same ticket count at once.
- Solved using **Pessimistic Write Locking (`SELECT ... FOR UPDATE`)** in `event-service`.
- Concurrent reservation requests are serialized at the database row level: the first request decrements stock and commits; subsequent requests see updated stock and fail cleanly with HTTP 400 if tickets are sold out.

### 4. Service-to-Service Security
- End-user requests validate stateless JWT tokens.
- When a service makes a user-driven call to another service (e.g., event cancellation calling booking cancellation), the incoming JWT `Bearer` token is forwarded in the HTTP `Authorization` header.
- Internal service-to-service endpoints (like ticket reservation/release) run within the internal network perimeter.

### 5. Partial Failures
- If the call from `booking-service` to `event-service` to reserve tickets fails, no booking is created and the client gets an immediate error.
- During hold expiration, if `booking-service` fails to reach `event-service` to release tickets, the database transaction rolls back. The booking remains in `PENDING_PAYMENT` and the scheduler safely retries on the next tick.

### 6. Expiry Job (Multi-Threading Conflict Resolution)
- A background scheduler runs every 60 seconds looking for bookings where `expiresAt < now()` and status is `PENDING_PAYMENT`.
- **Race Condition Handling**: An attendee might click "Pay" at the exact same second the expiry job runs. Both the payment endpoint and the expiry job acquire a row-level lock (`findByIdForUpdate`).
  - If **payment wins**: status becomes `CONFIRMED`; the expiry job sees the status change and skips it.
  - If **expiry wins**: status becomes `EXPIRED` and tickets are freed; the payment endpoint sees `EXPIRED` and rejects the charge.

---

## 🏗 Patterns Used & Where

| Pattern | Where Used | Purpose |
| :--- | :--- | :--- |
| **Database-per-Service** | All 3 services | Independent schemas (`identitydb`, `eventdb`, `bookingdb`) ensuring data isolation and loose coupling. |
| **Pessimistic Locking** | `event-service`, `booking-service` | `SELECT ... FOR UPDATE` prevents overselling and payment/expiry race conditions under high concurrency. |
| **Domain Events / Observer** | `booking-service` | `ApplicationEventPublisher` triggers audit logging and notification listeners without blocking the main transaction. |
| **Specification Pattern** | `event-service` | Dynamic filtering of events by keyword, city, and dates using Spring Data JPA `Specification`. |
| **Builder & DTO Pattern** | All services | Immutable transfer of data between layers and structured entity construction. |
| **Scheduled Task** | `booking-service` | `@Scheduled` cron/fixed-rate job for automatic ticket release upon hold expiration. |
| **Strategy & Factory** | `booking-service` | Strategy pattern is used for payment processing and factory pattern is used for creating payment processors. |

---

## 📌 Assumptions

- **Currency**: System operates exclusively in **USD ($)** with 2 decimal places (`BigDecimal`).
- **Ticket Limits**: Hard limit of **6 tickets per attendee per event** across active bookings.
- **Hold Duration**: Ticket hold duration is 10 minutes before unpaid bookings expire.
- **Event Lead Time**: Events must be scheduled at least 24 hours in advance.
- **Payment Gateway**: Payment processing is simulated internally (no real external card processor).

---

## ⚠️ Known Limitations

- **In-Memory Message Bus**: Domain events use Spring's in-process event publisher instead of an external message broker (Kafka/RabbitMQ).
- **H2 File Database**: Uses embedded H2 databases suitable for development and demos rather than production-grade external databases (PostgreSQL/MySQL).
