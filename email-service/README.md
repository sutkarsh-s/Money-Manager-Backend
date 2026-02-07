# Email Service

RabbitMQ-based asynchronous email consumer for Money Manager. Consumes from `profile-activation-queue` and sends activation emails via SMTP.

## Prerequisites

- RabbitMQ running on `localhost:5672`
- Java 21

## Running Locally

1. Start RabbitMQ (e.g., via `docker-compose up -d` in moneymanager folder)
2. Run the application:
   ```bash
   mvn spring-boot:run
   ```
3. Service runs on port 8081
4. RabbitMQ Management UI: http://localhost:15672 (guest/guest)

## Configuration

See `src/main/resources/application.yml`:

- `spring.rabbitmq.*`: RabbitMQ connection (host, port, credentials)
- `app.activation.url`: Money Manager API base URL for activation links
- `spring.mail.*`: SMTP configuration

## Flow

1. Money Manager publishes `ProfileActivationEvent` to `profile-activation-queue`
2. Email Service consumes the event
3. Constructs activation link and sends email via SMTP
4. Retries up to 3 times on failure (2s backoff) via @Retryable
