# RabbitMQ Email Refactoring Tutorial

A step-by-step guide explaining how the profile activation email flow was refactored from direct SMTP sending to an asynchronous, event-driven architecture using RabbitMQ.

---

## Table of Contents

1. [What Was the Problem?](#1-what-was-the-problem)
2. [What is RabbitMQ and Why Use It?](#2-what-is-rabbitmq-and-why-use-it)
3. [Architecture: Before vs After](#3-architecture-before-vs-after)
4. [Step-by-Step Refactoring Process](#4-step-by-step-refactoring-process)
5. [How to Run Everything](#5-how-to-run-everything)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. What Was the Problem?

### Original Flow (Synchronous)

When a user signed up, the application would:

1. Save the user to the database
2. **Immediately** call `emailService.sendEmail()` to send the activation link
3. Wait for the SMTP server to respond (could take 2–5 seconds or more)
4. Only then return the response to the user

**Issues with this approach:**

- **Slow response**: The user had to wait for the email to be sent before seeing a success message.
- **Failure sensitivity**: If the email server was down or slow, registration could fail even though the user was saved.
- **No retries**: If email sending failed, there was no automatic retry.
- **Tight coupling**: The registration logic was directly tied to the email service.

---

## 2. What is RabbitMQ and Why Use It?

**RabbitMQ** is a **message broker**. Think of it as a post office:

- **Producer** (sender): The app that creates a message (e.g., "send activation email to user@example.com") and puts it in a **queue**.
- **Queue**: A buffer that holds messages until someone processes them.
- **Consumer** (receiver): Another service that reads from the queue and performs the actual work (sending the email).

**Benefits:**

1. **Asynchronous**: The registration API returns quickly. Email sending happens in the background.
2. **Reliability**: Messages stay in the queue until processed. If the consumer fails, the message can be retried.
3. **Decoupling**: Registration doesn’t depend on the email service. They communicate only through messages.
4. **Scalability**: You can run multiple consumer instances to handle more emails.

---

## 3. Architecture: Before vs After

### Before (Direct Call)

```
┌─────────────────┐     sendEmail()      ┌──────────────┐
│ ProfileService  │ ──────────────────►  │ EmailService │
│ (register user) │                      │ (SMTP call)  │
└─────────────────┘                      └──────────────┘
        │                                        │
        │    User waits for both to complete     │
        └────────────────────────────────────────┘
```

### After (RabbitMQ)

```
┌─────────────────┐    publish event     ┌─────────────┐    consume     ┌──────────────┐
│ ProfileService  │ ─────────────────►  │  RabbitMQ   │ ◄────────────  │ EmailService │
│ (Money Manager) │                      │   Queue     │                │ (Consumer)   │
└─────────────────┘                      └─────────────┘                └──────────────┘
        │                                        │                              │
        │  Returns immediately                   │  Holds message                │  Sends email
        │  (user sees success fast)              │  until consumed               │  via SMTP
```

---

## 4. Step-by-Step Refactoring Process

### Step 1: Add RabbitMQ Dependency (Money Manager)

**File:** `moneymanager/pom.xml`

**What we did:** Added the Spring AMQP (RabbitMQ) dependency.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

**Why:** This gives us `RabbitTemplate` (to send messages) and the RabbitMQ client.

---

### Step 2: Create the Event Class

**File:** `moneymanager/src/main/java/.../event/ProfileActivationEvent.java`

**What we did:** Created a simple Java class (DTO) to represent the data we want to send.

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileActivationEvent {
    private String email;
    private String fullName;
    private String activationToken;
}
```

**Why:** We need a structured message. The producer will convert this to JSON and send it. The consumer will read the JSON and convert it back to this object.

---

### Step 3: Configure RabbitMQ in the Main App

**File:** `moneymanager/src/main/resources/application.yml`

**What we did:** Added RabbitMQ connection settings.

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

**Why:** The app must know where RabbitMQ is running to connect and send messages.

---

### Step 4: Create RabbitMQ Config (Producer Side)

**File:** `moneymanager/src/main/java/.../config/RabbitMQConfig.java`

**What we did:**

1. **Define the queue name** – A constant so both producer and consumer use the same queue.
2. **Declare the queue** – Tells RabbitMQ to create the queue if it doesn’t exist.
3. **JSON message converter** – Converts Java objects to JSON for the message body.
4. **Custom RabbitTemplate** – Ensures messages are sent as JSON, not Java serialization.

```java
@Bean
public Queue profileActivationQueue() {
    return new Queue(PROFILE_ACTIVATION_QUEUE, true);  // true = durable
}

@Bean
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}

@Bean
public RabbitTemplate rabbitTemplate(...) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter);
    return template;
}
```

**Why:** The queue must exist before we send. JSON makes the format portable and easy for any consumer to parse. The custom `RabbitTemplate` ensures our events are serialized as JSON.

---

### Step 5: Refactor ProfileService (Producer)

**File:** `moneymanager/src/main/java/.../service/ProfileService.java`

**Before:**
```java
private void sendActivationEmail(ProfileEntity profile) {
    String activationLink = activationURL + "/api/v1.0/activate?token=" + profile.getActivationToken();
    String subject = "Activate your Money Manager account";
    String body = "Hi " + profile.getFullName() + ", ...";
    emailService.sendEmail(profile.getEmail(), subject, body);  // BLOCKING call
}
```

**After:**
```java
private void publishActivationEvent(ProfileEntity profile) {
    ProfileActivationEvent event = ProfileActivationEvent.builder()
            .email(profile.getEmail())
            .fullName(profile.getFullName())
            .activationToken(profile.getActivationToken())
            .build();

    rabbitTemplate.convertAndSend(RabbitMQConfig.PROFILE_ACTIVATION_QUEUE, event);
    log.info("Activation event published successfully for email: {}", profile.getEmail());
}
```

**Changes:**
1. Removed `EmailService` from this flow.
2. Injected `RabbitTemplate` instead.
3. Build the event from the profile data.
4. Call `convertAndSend(queueName, event)` – converts to JSON and sends to the queue.

**Why:** The registration API no longer waits for SMTP. It only publishes a message and returns. The consumer handles the email later.

---

### Step 6: Create the Email Service Module (Consumer)

We added a separate Spring Boot application: **email-service**. It only listens to the queue and sends emails.

**Why a separate app?** To keep responsibilities separate. Money Manager handles registration; Email Service handles email. They share nothing except the queue and the event format.

---

### Step 7: Email Service Dependencies

**File:** `email-service/pom.xml`

**Dependencies:**
- `spring-boot-starter-amqp` – For RabbitMQ consumer.
- `spring-boot-starter-mail` – For sending emails via SMTP.
- `spring-retry` – For retrying failed messages.
- `lombok` – For boilerplate reduction.

---

### Step 8: Email Service Event Class

**File:** `email-service/src/main/java/.../event/ProfileActivationEvent.java`

**What we did:** Duplicated the same event class (or use a shared library in a real project).

**Why:** The consumer must deserialize the JSON into an object. The structure must match exactly what the producer sends.

---

### Step 9: Email Service Configuration

**File:** `email-service/src/main/resources/application.yml`

**What we did:** Configured:
1. RabbitMQ connection (same host/port as producer).
2. SMTP settings (Brevo, etc.).
3. `app.activation.url` – Base URL for activation links (Money Manager API).
4. Server port `8081` so it doesn’t conflict with Money Manager on `8080`.

---

### Step 10: RabbitMQ Config (Consumer Side)

**File:** `email-service/src/main/java/.../config/RabbitMQConfig.java`

**What we did:**

1. **Declare the same queue** – Both apps use `profile-activation-queue`.
2. **JSON message converter** – So we receive Java objects, not raw JSON.
3. **Listener container factory** – Sets up how we listen to the queue.
4. **Retry template** – 3 retries with 2-second delay between attempts.

```java
@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(...) {
    // ...
    RetryTemplate retryTemplate = new RetryTemplate();
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(2000);  // 2 seconds between retries
    retryTemplate.setBackOffPolicy(backOffPolicy);
    retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));  // 3 attempts
    factory.setRetryTemplate(retryTemplate);
    return factory;
}
```

**Why:** If sending the email fails (e.g. SMTP timeout), the message is retried automatically. After 3 failures, it is rejected (and not requeued, to avoid infinite loops).

---

### Step 11: The Consumer (ProfileActivationEventConsumer)

**File:** `email-service/src/main/java/.../consumer/ProfileActivationEventConsumer.java`

**What we did:**

```java
@RabbitListener(queues = RabbitMQConfig.PROFILE_ACTIVATION_QUEUE)
public void consumeProfileActivationEvent(ProfileActivationEvent event) {
    log.info("Consumed profile activation event for email: {}", event.getEmail());

    String activationLink = activationUrl + "/api/v1.0/activate?token=" + event.getActivationToken();
    String subject = "Activate your Money Manager account";
    String body = "Hi " + event.getFullName() + ", ...";

    emailService.sendEmail(event.getEmail(), subject, body);
    log.info("Activation email sent successfully to: {}", event.getEmail());
}
```

**What happens:**
1. `@RabbitListener` makes this method listen to `profile-activation-queue`.
2. When a message arrives, Spring deserializes it into `ProfileActivationEvent`.
3. We build the activation link using `activationUrl` from config.
4. We call `emailService.sendEmail()` – the actual SMTP call.
5. If an exception is thrown, the RetryTemplate retries up to 3 times.

**Why:** All email logic is centralized here. Money Manager does not need to know how emails are sent.

---

### Step 12: Email Service (SMTP Sender)

**File:** `email-service/src/main/java/.../service/EmailService.java`

**What we did:** Implemented a simple service that uses `JavaMailSender` to send emails.

**Why:** Keeps the actual SMTP logic in one place, separate from RabbitMQ concerns.

---

### Step 13: Docker Compose for RabbitMQ

**File:** `moneymanager/docker-compose.yml`

**What we did:** Added a RabbitMQ service.

```yaml
rabbitmq:
  image: rabbitmq:3-management
  ports:
    - "5672:5672"   # AMQP protocol
    - "15672:15672" # Management UI
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

**Why:** RabbitMQ must be running for both apps. The management UI on port 15672 lets you inspect queues and messages.

---

## 5. How to Run Everything

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose

### Steps

1. **Start infrastructure**
   ```bash
   cd moneymanager
   docker-compose up -d
   ```
   This starts MySQL and RabbitMQ.

2. **Run Money Manager (producer)**
   ```bash
   cd moneymanager
   mvn spring-boot:run
   ```
   Runs on http://localhost:8080

3. **Run Email Service (consumer)**
   ```bash
   cd email-service
   mvn spring-boot:run
   ```
   Runs on http://localhost:8081

4. **Test the flow**
   - Register a new user via the API or frontend.
   - The registration should return quickly.
   - Check the email-service logs for the activation email being sent.
   - Optionally check RabbitMQ UI: http://localhost:15672 (guest/guest)

---

## 6. Troubleshooting

| Issue | Cause | Solution |
|-------|--------|----------|
| Connection refused to localhost:5672 | RabbitMQ not running | Run `docker-compose up -d` in the moneymanager folder |
| Email not received | SMTP config wrong, or consumer not running | Check email-service logs and SMTP settings in `application.yml` |
| Message in queue but not consumed | Consumer not running or wrong queue name | Ensure email-service is running and uses the same queue name (`profile-activation-queue`) |
| JSON deserialization error | Event class mismatch between producer and consumer | Ensure both apps use the same fields: `email`, `fullName`, `activationToken` |

---

## Summary

1. **Producer (Money Manager):** On registration, it publishes a `ProfileActivationEvent` to RabbitMQ instead of calling the email service directly.
2. **Queue:** RabbitMQ stores the message until a consumer processes it.
3. **Consumer (Email Service):** Listens on the queue, receives the event, builds the activation link, and sends the email via SMTP.
4. **Benefits:** Faster registration response, decoupled services, automatic retries, and better reliability.
