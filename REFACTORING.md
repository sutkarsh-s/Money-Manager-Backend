# Architectural Refactoring: Email-Service & Core-Service

This document describes the architectural review and refactoring applied to both microservices to align with **SOLID principles** and **industry best practices**. Behaviour is unchanged; the focus is on **maintainability**, **extensibility**, and **testability**.

---

## 1. Before vs After Package Structure

### Email-Service

| Before | After |
|--------|--------|
| `config/` | `config/` (unchanged) |
| `consumer/` | `consumer/` (slimmed: only receives and delegates) |
| `entity/` | `entity/` (unchanged) |
| `enums/` | `enums/` (unchanged) |
| `event/` | `event/` (unchanged) |
| `listener/` | `listener/` (slimmed: only listens and delegates) |
| `repository/` | `repository/` (unchanged – used by adapters) |
| `service/` (EmailService, EmailOutboxProcessor) | **Removed** – replaced by ports, use cases, adapters |
| — | **`domain/`** – `EmailMessage` (value object), `ActivationEmailContentBuilder` |
| — | **`port/`** – `EmailSender`, `EmailOutboxStore`, `ProcessedMessageStore`, `DomainEventPublisher` |
| — | **`usecase/`** – `HandleProfileActivationEventUseCase`, `SendOutboxEmailUseCase` |
| — | **`adapter/smtp/`** – `SmtpEmailSender` |
| — | **`adapter/persistence/`** – `EmailOutboxStoreAdapter`, `ProcessedMessageStoreAdapter` |
| — | **`adapter/event/`** – `SpringDomainEventPublisher` |

**Layering (clean architecture):**  
Consumer/Listener (inbound) → **Use cases** → **Ports (interfaces)** ← **Adapters** (outbound: SMTP, JPA, Spring events).

---

### Core-Service

| Before | After |
|--------|--------|
| `config/` | `config/` (unchanged) |
| `controller/` | `controller/` (unchanged) |
| `dto/`, `entity/`, `enums/`, `event/` | Unchanged |
| `repository/` | Unchanged (used by adapters) |
| `service/` (ProfileService used OutboxRepository + ObjectMapper directly) | **ProfileService** now depends on **ports** and **domain** only |
| `publisher/` (OutboxPublisher had all publish + retry logic) | **OutboxPublisher** only schedules; **PublishPendingOutboxEventsUseCase** holds logic |
| — | **`port/`** – `OutboxEventStore`, `EventPublisher` |
| — | **`usecase/`** – `PublishPendingOutboxEventsUseCase` |
| — | **`domain/`** – `OutboxEventFactory` |
| — | **`adapter/persistence/`** – `OutboxEventStoreAdapter` |
| — | **`adapter/messaging/`** – `RabbitEventPublisher` |

**Layering:**  
Controller → **Service (ProfileService)** → **Ports** ← **Adapters**.  
Scheduler (**OutboxPublisher**) → **Use case** → **Ports** ← **Adapters**.

---

## 2. SOLID Principles – Where They Are Applied

### Single Responsibility Principle (SRP)

- **Email-service**
  - **ActivationEmailContentBuilder**: Only builds activation email content (subject/body). No sending or persistence.
  - **HandleProfileActivationEventUseCase**: Only orchestrates “receive activation event → idempotency → outbox → event”.
  - **SendOutboxEmailUseCase**: Only “load outbox by id → send → update status”.
  - **ProfileActivationEventConsumer**: Only receives AMQP message, calls use case, acks/rejects. No business rules.
  - **SmtpEmailSender**: Only sends one email via SMTP. No outbox or event logic.
- **Core-service**
  - **OutboxEventFactory**: Only creates `OutboxEvent` from a profile activation event and payload.
  - **PublishPendingOutboxEventsUseCase**: Only “load pending → publish each → update status/retry”.
  - **OutboxPublisher**: Only scheduling; no business logic.
  - **ProfileService**: Registration, login, profile; outbox creation delegated to factory + store.

### Open/Closed Principle (OCP)

- **Email content**: New email types (e.g. password reset) can be added by new builder classes (e.g. `PasswordResetEmailContentBuilder`) and new use cases or handlers **without changing** `ActivationEmailContentBuilder` or `HandleProfileActivationEventUseCase` (extend by adding new code, not editing existing).
- **Sending**: New transport (e.g. SendGrid) = new adapter implementing `EmailSender`; callers stay unchanged.
- **Events**: New event types in core-service can be added via new methods on `EventPublisher` or new use cases without modifying existing publish logic.

### Liskov Substitution Principle (LSP)

- Any implementation of a **port** can replace another without breaking callers:
  - `SmtpEmailSender` and any other `EmailSender` implementation are interchangeable.
  - `EmailOutboxStoreAdapter`, `ProcessedMessageStoreAdapter`, `SpringDomainEventPublisher`, `OutboxEventStoreAdapter`, `RabbitEventPublisher` can be replaced with test doubles (mocks) or alternative implementations (e.g. different queue or DB) and the use cases still behave correctly.

### Interface Segregation Principle (ISP)

- **Small, role-specific ports** instead of one big “email service”:
  - **EmailSender**: only `send(EmailMessage)`.
  - **EmailOutboxStore**: only `save`, `findById`.
  - **ProcessedMessageStore**: only `existsByEventId`, `save`.
  - **DomainEventPublisher**: only `publish(OutboxCreatedEvent)`.
  - **OutboxEventStore**: only `save`, `findTopByStatusOrderByCreatedAtAsc`.
  - **EventPublisher**: only `publishProfileActivation(event, eventId)`.
- No class is forced to depend on methods it does not use.

### Dependency Inversion Principle (DIP)

- **High-level modules depend on abstractions (ports), not concretions:**
  - Use cases depend only on **port interfaces** (`EmailSender`, `EmailOutboxStore`, etc.). They do not import RabbitMQ, JPA, or SMTP.
  - **Adapters** (in infrastructure) implement these interfaces and depend on Spring/Rabbit/JPA.
- **Constructor injection** is used everywhere (via `@RequiredArgsConstructor`), so dependencies are explicit and easy to mock in tests.

---

## 3. Design Patterns Used and Why

| Pattern | Where | Why |
|--------|--------|-----|
| **Ports & Adapters (Hexagonal)** | Both services | Clear boundary between application logic (use cases) and infrastructure (messaging, DB, SMTP). Easy to test and swap implementations. |
| **Use Case / Application Service** | HandleProfileActivationEvent, SendOutboxEmail, PublishPendingOutboxEvents | One place per “action”. Orchestrates ports only; no framework or infra details. |
| **Adapter** | SmtpEmailSender, *StoreAdapter, RabbitEventPublisher, SpringDomainEventPublisher | Implements a port and talks to one external system. Keeps infra changes in one place. |
| **Factory** | OutboxEventFactory | Single place to create outbox events with correct defaults. Keeps entity construction out of services. |
| **Value Object** | EmailMessage | Immutable DTO for email content. Reduces coupling to SMTP or other transport details. |
| **Strategy (implicit)** | EmailSender, EventPublisher | Different strategies (SMTP, SendGrid; Rabbit, Kafka) can be plugged in without changing use cases. |

---

## 4. Improvements in Simple Words

### What was wrong before?

- **Email-service**: One class did sending + one class did “outbox + send + status”. Repositories and SMTP were used directly inside logic, so testing and swapping implementations was harder. Content for the activation email was built inside the consumer.
- **Core-service**: Outbox publishing logic (retry, status updates) lived inside the scheduler. Profile registration created outbox events and used the repository directly, so the same code mixed “what to do” with “how to store/publish”.

### What we did (in simple terms)?

1. **Defined clear “contracts” (ports)**  
   The application says: “I need something that can send an email” or “I need something that can save/load outbox events.” Implementations (SMTP, Rabbit, JPA) live behind these contracts.

2. **One job per class**  
   Each class has one clear job: build content, send one email, handle one event type, store outbox, or schedule a job. No class does “everything.”

3. **Use cases in the middle**  
   Use cases only know about the contracts (ports). They don’t know about RabbitMQ, JPA, or SMTP. That makes behaviour easy to test with mocks and easy to extend with new adapters.

4. **Adapters at the edges**  
   SMTP sender, JPA repositories (via store adapters), Rabbit publisher, and Spring event publisher are “adapters” that implement the ports. Changing SMTP provider or message broker only touches these classes.

5. **Immutability and value objects**  
   `EmailMessage` is an immutable value object so that email content is passed around without accidental changes.

6. **Exception handling**  
   Use cases still let exceptions propagate; the consumer/listener or scheduler catches them and handles ack/reject or retries. No change to existing exception strategy; boundaries are clearer.

7. **Transactional boundaries**  
   Transactions stay in use cases (or in adapters where needed). The scheduler only triggers the use case; the use case owns the transaction for “load pending → publish → update.”

8. **Logging**  
   Logging remains in use cases and adapters at key steps (e.g. “Event published”, “Email sent”). No new frameworks; same practices, clearer places.

9. **No behaviour change**  
   From the outside (API, messages, emails), everything works as before. Only the internal structure and dependencies were improved.

---

## 5. Summary Table

| Area | Before | After |
|------|--------|--------|
| **Layering** | Mixed: services used repositories and templates directly | Controller/Consumer → Use case → Ports ← Adapters |
| **Testability** | Harder to unit-test (needed real DB/Rabbit/SMTP or heavy mocking) | Use cases testable with port mocks only |
| **Extensibility** | New email type or transport required editing existing classes | New builders/adapters; existing use cases unchanged (OCP) |
| **Duplication** | Content building and send logic spread in consumer/processor | Content in one builder; send in one use case + one sender adapter |
| **Dependencies** | ProfileService → OutboxRepository, ObjectMapper; Publisher → Repository, RabbitTemplate | ProfileService → OutboxEventStore, OutboxEventFactory; Publisher → Use case → Ports |
| **Naming** | “Service” for both application and infrastructure roles | “Use case” for application; “Adapter” for infrastructure |

---

## 6. How to Test After Refactoring

- **Use cases**: Inject mock implementations of the ports (e.g. `EmailSender`, `EmailOutboxStore`). Assert calls and state changes. No Spring, no JPA, no Rabbit.
- **Adapters**: Test with embedded DB or testcontainers if you need integration tests; or mock the JPA repository / RabbitTemplate and assert correct calls.
- **Consumer/Listener**: Test that they call the use case and ack/reject as expected; mock the use case.

This refactoring keeps your business behaviour the same while making the codebase easier to maintain, extend, and test in line with SOLID and clean architecture.



ABOVE CODE REFACTORING is Hexagonal Architecture (Ports & Adapters), also called Clean Architecture, commonly used in well-designed Spring Boot microservices.

This structure helps you:

Follow SOLID naturally

Reduce coupling

Improve testability

Swap infrastructure easily

Big Picture Architecture

           ┌──────────────────────┐
           │      Adapters        │  ← Controllers, DB, Kafka, Email, etc.
           └──────────┬───────────┘
                      │
           ┌──────────▼───────────┐
           │        Ports         │  ← Interfaces (contracts)
           └──────────┬───────────┘
                      │
           ┌──────────▼───────────┐
           │        UseCase       │  ← Application business logic
           └──────────┬───────────┘
                      │
           ┌──────────▼───────────┐
           │        Domain        │  ← Core business model
           └──────────────────────┘


